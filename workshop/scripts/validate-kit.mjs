#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(__dirname, '..');

const REQUIRED = [
  'README.md',
  'compass.md',
  'prep/env-setup.md',
  'prep/coverage-scope.md',
  'prep/gitnexus-queries.md',
  'prep/canary/README.md',
  'prep/canary/sample-quality-report.md',
  'prep/jacoco-include.txt',
  'prep/prompt-cards/P0.md',
  'prep/prompt-cards/P1.md',
  'prep/prompt-cards/P2.md',
  'prep/prompt-cards/P3.md',
  'prep/prompt-cards/P4.md',
  'prep/prompt-cards/P5.md',
  'prep/prompt-cards/P6.md',
  'prep/prompt-cards/recovery-cards.md',
  'prep/gate-scripts/P0.md',
  'prep/gate-scripts/P1.md',
  'prep/gate-scripts/P2.md',
  'prep/gate-scripts/P3.md',
  'prep/gate-scripts/P4.md',
  'prep/gate-scripts/P5.md',
  'product/order.html',
  'product/PRD-order-checkout.md',
  'curriculum/01-讲师手册.md',
  'curriculum/02-学员手册.md',
  'curriculum/03-提示词卡册.md',
  'curriculum/04-评分表.md',
  'curriculum/05-示范产物索引.md',
  'curriculum/06-踩坑-playbook.md',
];

const COMPASS_MARKERS = ['## P0', '## P1', '## P2', '## P3', '## P4', '## P5', '## P6'];

function main() {
  const missing = [];
  for (const rel of REQUIRED) {
    const abs = path.join(root, rel);
    if (!fs.existsSync(abs)) missing.push(rel);
  }
  const compassPath = path.join(root, 'compass.md');
  let compassIssues = [];
  if (fs.existsSync(compassPath)) {
    const text = fs.readFileSync(compassPath, 'utf8');
    for (const m of COMPASS_MARKERS) {
      if (!text.includes(m)) compassIssues.push(`compass.md missing marker: ${m}`);
    }
    for (const field of ['目标', 'Prompt 卡', '人闸结论', '耗时', 'degraded']) {
      if (!text.includes(field)) compassIssues.push(`compass.md missing field hint: ${field}`);
    }
  }
  if (missing.length || compassIssues.length) {
    console.error('validate-kit FAILED');
    for (const m of missing) console.error('  missing:', m);
    for (const c of compassIssues) console.error(' ', c);
    process.exit(1);
  }
  console.log('validate-kit OK', REQUIRED.length, 'files');
}

main();
