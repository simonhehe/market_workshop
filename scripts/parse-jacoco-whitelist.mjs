import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const xml = fs.readFileSync(path.join(root, 'backend/target/site/jacoco/jacoco.xml'), 'utf8')
const WL = [
  'com/example/mall/service/OrderService',
  'com/example/mall/service/CartService',
  'com/example/mall/service/AddressService',
  'com/example/mall/controller/OrderController',
  'com/example/mall/controller/CartController',
  'com/example/mall/controller/AddressController'
]

for (const prefix of WL) {
  const re = new RegExp(`<class name="${prefix.replace(/\//g, '\\/')}"[\\s\\S]*?</class>`)
  const m = xml.match(re)
  if (!m) {
    console.log(prefix.split('/').pop() + ': NOT FOUND')
    continue
  }
  const block = m[0]
  const lineCounter = [...block.matchAll(/<counter type="LINE" missed="(\d+)" covered="(\d+)"/g)].pop()
  const branchCounter = [...block.matchAll(/<counter type="BRANCH" missed="(\d+)" covered="(\d+)"/g)].pop()
  let lineM = 0, lineC = 0, brM = 0, brC = 0
  if (lineCounter) { lineM = Number(lineCounter[1]); lineC = Number(lineCounter[2]) }
  if (branchCounter) { brM = Number(branchCounter[1]); brC = Number(branchCounter[2]) }
  const lp = lineC + lineM ? ((100 * lineC) / (lineC + lineM)).toFixed(1) : '100'
  const bp = brC + brM ? ((100 * brC) / (brC + brM)).toFixed(1) : '100'
  console.log(`\n${prefix.split('/').pop()}: line ${lp}% branch ${bp}%`)
  const methods = [...block.matchAll(/<method name="([^"]+)"[\s\S]*?<counter type="LINE" missed="(\d+)" covered="(\d+)"/g)]
  for (const [, name, missed] of methods) {
    if (Number(missed) > 0) console.log(`  - ${name}: lines missed ${missed}`)
  }
}
