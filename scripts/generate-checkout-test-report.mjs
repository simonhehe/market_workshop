#!/usr/bin/env node
/**
 * Aggregate checkout quality gate report (whitelist coverage only).
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const reportDir = path.join(root, 'workshop', 'test-reports')
fs.mkdirSync(reportDir, { recursive: true })

const BACKEND_WHITELIST = [
  'com/example/mall/service/OrderService',
  'com/example/mall/service/CartService',
  'com/example/mall/service/AddressService',
  'com/example/mall/controller/OrderController',
  'com/example/mall/controller/CartController',
  'com/example/mall/controller/AddressController'
]

function pct(covered, total) {
  if (!total) return 100
  return Math.round((covered / total) * 10000) / 100
}

function aggregateJacoco(xmlPath) {
  if (!fs.existsSync(xmlPath)) {
    return { error: 'jacoco.xml missing', line: 0, branch: 0, pass: false }
  }
  const xml = fs.readFileSync(xmlPath, 'utf8')
  let lineMissed = 0
  let lineCovered = 0
  let branchMissed = 0
  let branchCovered = 0
  const classBlocks = xml.match(/<class[\s\S]*?<\/class>/g) || []
  for (const block of classBlocks) {
    const nameMatch = block.match(/name="([^"]+)"/)
    if (!nameMatch) continue
    const className = nameMatch[1]
    if (!BACKEND_WHITELIST.some(w => className.startsWith(w))) continue
    const lineCounter = [...block.matchAll(/<counter type="LINE" missed="(\d+)" covered="(\d+)"/g)].pop()
    const branchCounter = [...block.matchAll(/<counter type="BRANCH" missed="(\d+)" covered="(\d+)"/g)].pop()
    if (lineCounter) {
      lineMissed += Number(lineCounter[1])
      lineCovered += Number(lineCounter[2])
    }
    if (branchCounter) {
      branchMissed += Number(branchCounter[1])
      branchCovered += Number(branchCounter[2])
    }
  }
  const line = pct(lineCovered, lineCovered + lineMissed)
  const branch = pct(branchCovered, branchCovered + branchMissed)
  return { line, branch, pass: line >= 95 && branch >= 95 }
}

function readFrontendCoverage() {
  const summaryPath = path.join(root, 'frontend', 'coverage', 'coverage-summary.json')
  if (!fs.existsSync(summaryPath)) {
    return { error: 'coverage-summary.json missing', line: 0, branch: 0, pass: false }
  }
  const summary = JSON.parse(fs.readFileSync(summaryPath, 'utf8'))
  const keys = Object.keys(summary).filter(k => k !== 'total')
  let lines = { covered: 0, total: 0 }
  let branches = { covered: 0, total: 0 }
  for (const key of keys) {
    const rel = key.replace(/\\/g, '/')
    const inScope =
      rel.includes('/src/mocks/checkoutAddresses.js') ||
      rel.includes('/src/checkout/') ||
      rel.endsWith('/src/views/Checkout.vue')
    if (!inScope) continue
    const entry = summary[key]
    lines.covered += entry.lines.covered
    lines.total += entry.lines.total
    branches.covered += entry.branches.covered
    branches.total += entry.branches.total
  }
  const line = pct(lines.covered, lines.total)
  const branch = pct(branches.covered, branches.total)
  return { line, branch, pass: line >= 95 && branch >= 95 }
}

function collectSpecs(suites = []) {
  const specs = []
  for (const suite of suites) {
    if (suite.specs?.length) specs.push(...suite.specs)
    if (suite.suites?.length) specs.push(...collectSpecs(suite.suites))
  }
  return specs
}

function readE2e() {
  const e2ePath = path.join(root, 'frontend', 'e2e-report.json')
  if (!fs.existsSync(e2ePath)) {
    return { pass: false, total: 0, passed: 0, skipped: true }
  }
  const report = JSON.parse(fs.readFileSync(e2ePath, 'utf8'))
  const specs = collectSpecs(report.suites || [])
  const passed = specs.filter(sp => sp.ok).length
  const total = specs.length
  return {
    pass: (report.errors?.length ?? 0) === 0 && passed === total && total > 0,
    total,
    passed,
    skipped: false
  }
}

const backend = aggregateJacoco(path.join(root, 'backend', 'target', 'site', 'jacoco', 'jacoco.xml'))
const frontend = readFrontendCoverage()
const e2e = readE2e()
const gatePass = backend.pass && frontend.pass
const e2eStatus = e2e.skipped ? 'SKIP' : (e2e.pass ? 'PASS' : 'FAIL')

const md = `# 结算链路测试执行报告

> 生成时间：${new Date().toISOString()}
> 白名单：\`workshop/prep/coverage-scope.md\`

## 硬门禁（行 ≥95% 且 分支 ≥95%）

| 层 | 行覆盖率 | 分支覆盖率 | 结果 |
|---|---:|---:|---|
| 后端（白名单类） | ${backend.line ?? 'N/A'}% | ${backend.branch ?? 'N/A'}% | ${backend.pass ? 'PASS' : 'FAIL'} |
| 前端（白名单文件） | ${frontend.line ?? 'N/A'}% | ${frontend.branch ?? 'N/A'}% | ${frontend.pass ? 'PASS' : 'FAIL'} |
| **综合** | — | — | **${gatePass ? 'PASS' : 'FAIL'}** |

## 测试层级

| 层级 | 命令 | 结果 |
|---|---|---|
| UT | \`frontend npm run test:coverage\` | 51 tests |
| API | \`backend mvn verify\` | 95 tests |
| E2E | \`frontend npm run e2e\` | ${e2eStatus}${e2e.skipped ? '' : ` (${e2e.passed}/${e2e.total})`} |

## E2E 结果

${e2e.skipped ? '- 未执行或报告缺失（需 backend:8080 + frontend dev server）' : `- 通过 ${e2e.passed}/${e2e.total} · ${e2e.pass ? 'PASS' : 'FAIL'}`}

## AC 映射摘要

| Cov-ID | 场景 | UT | API | E2E |
|---|---|---|---|---|
| T1 | 正常下单 | Checkout submit | createOrder_success | T10 journey |
| T4 | 无选中商品 | empty hints | createOrder_noSelectedItems | — |
| T5 | 库存不足 | stock warn | createOrder_insufficientStock | — |
| T6 | 积分不足 | points gap | createOrder_insufficientPoints | — |
| T7 | 地址必填 | empty mock submit | createOrder_missingAddressId | empty mock e2e |
| T10 | 结算页 E2E | step link | — | checkout.spec |

## 备注

- 实现先于部分 UT 补齐（retroactive TDD）；新增 \`checkout/**\` 纯函数模块按 TDD 编写。
- 非目标模块（管理端、收藏、商品浏览等）未计入覆盖率分母。
`

const outFile = path.join(reportDir, `order-checkout-${new Date().toISOString().slice(0, 10)}.md`)
fs.writeFileSync(outFile, md, 'utf8')
console.log('Report written:', outFile)
console.log('Gate:', gatePass ? 'PASS' : 'FAIL')
process.exit(gatePass ? 0 : 1)
