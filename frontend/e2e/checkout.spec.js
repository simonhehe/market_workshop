import { test, expect } from '@playwright/test'

const USER = { username: 'user1', password: 'password1' }

async function login(page) {
  await page.goto('/login')
  await page.fill('input[type="text"], input[name="username"], input[placeholder*="用户"]', USER.username)
  await page.fill('input[type="password"]', USER.password)
  await page.click('button[type="submit"], button:has-text("登录")')
  await page.waitForURL(url => !url.pathname.includes('/login'), { timeout: 15000 })
}

async function seedSelectedCart(page, request) {
  const token = await page.evaluate(() => localStorage.getItem('token'))
  const headers = { Authorization: `Bearer ${token}` }

  await request.delete('/api/cart/clear', { headers })

  const productsResp = await request.get('/api/products', { headers })
  expect(productsResp.ok()).toBeTruthy()
  const productsBody = await productsResp.json()
  const productId = productsBody.data?.[0]?.id
  expect(productId).toBeTruthy()

  const addResp = await request.post('/api/cart', {
    headers: { ...headers, 'Content-Type': 'application/json' },
    data: { productId, quantity: 1 }
  })
  expect(addResp.ok()).toBeTruthy()
}

test.describe('checkout e2e', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('T10 navigate cart -> checkout -> submit -> order detail', async ({ page, request }) => {
    await seedSelectedCart(page, request)
    await page.goto('/orders/create')
    await expect(page.locator('.address-list .address-item').first()).toBeVisible({ timeout: 15000 })
    await page.locator('.address-item').first().click()
    await page.click('.btn-submit')
    await page.waitForURL(/\/orders\/\d+/, { timeout: 15000 })
    await expect(page.locator('body')).toContainText(/pending|待处理|订单/i)
  })

  test('empty mock address blocks submit with visible error', async ({ page, request }) => {
    await seedSelectedCart(page, request)
    await page.goto('/orders/create?mockAddress=empty')
    await expect(page.locator('.address-empty')).toBeVisible({ timeout: 15000 })
    await page.click('.btn-submit')
    await expect(page.locator('.overlay.show')).toBeVisible()
    await expect(page.locator('.error-banner.show')).toBeVisible()
  })

  test('step link returns to cart page', async ({ page }) => {
    await page.goto('/orders/create')
    await expect(page.locator('.step-link')).toBeVisible({ timeout: 15000 })
    await page.click('.step-link')
    await page.waitForURL('**/cart', { timeout: 10000 })
    await expect(page.locator('body')).toContainText(/购物车|购物/i)
  })
})
