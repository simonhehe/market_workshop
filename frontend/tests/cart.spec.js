import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

vi.mock('../src/api/exports', () => ({
  cartApi: {
    getCart: vi.fn(),
    addToCart: vi.fn(),
    updateQuantity: vi.fn(),
    removeFromCart: vi.fn(),
    clearCart: vi.fn()
  },
  productApi: { getProductById: vi.fn() }
}))

import Cart from '../src/views/Cart.vue'
import { cartApi, productApi } from '../src/api/exports'

const LayoutStub = { name: 'LayoutStub', template: '<div class="layout-stub"><slot /></div>' }

const routerPush = vi.fn()

function mountCart() {
  return mount(Cart, {
    global: {
      stubs: { Layout: LayoutStub },
      mocks: { $route: { query: {} }, $router: { push: routerPush } }
    }
  })
}

const cartPayload = {
  success: true,
  data: {
    cartItems: [{ id: 1, productId: 10, quantity: 2, isSelected: 1 }],
    summary: { selectedCount: 1, totalPoints: 200 }
  }
}

beforeEach(() => {
  vi.clearAllMocks()
  vi.spyOn(console, 'error').mockImplementation(() => {})
  cartApi.getCart.mockResolvedValue(cartPayload)
  productApi.getProductById.mockResolvedValue({
    success: true,
    data: { id: 10, name: '降噪蓝牙耳机', pointsRequired: 100 }
  })
  cartApi.updateQuantity.mockResolvedValue({ success: true })
  cartApi.removeFromCart.mockResolvedValue({ success: true })
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('Cart.vue', () => {
  it('加载并渲染购物车条目、数量与汇总，关键按钮存在', async () => {
    const wrapper = mountCart()
    await flushPromises()

    expect(cartApi.getCart).toHaveBeenCalledTimes(1)
    expect(productApi.getProductById).toHaveBeenCalledWith(10)

    const items = wrapper.findAll('.cart-item')
    expect(items).toHaveLength(1)
    expect(items[0].text()).toContain('降噪蓝牙耳机')
    expect(items[0].text()).toContain('100 积分 × 2')

    const summary = wrapper.find('.cart-summary')
    expect(summary.text()).toContain('已选 1 件商品')
    expect(summary.text()).toContain('总计：200 积分')

    const actionButtons = items[0].findAll('.item-actions button').map((button) => button.text())
    expect(actionButtons).toEqual(['-', '+', '删除'])
    expect(wrapper.find('.checkout-btn').exists()).toBe(true)
    wrapper.unmount()
  })

  it('购物车为空时显示空态且不显示结算按钮', async () => {
    cartApi.getCart.mockResolvedValue({
      success: true,
      data: { cartItems: [], summary: { selectedCount: 0, totalPoints: 0 } }
    })

    const wrapper = mountCart()
    await flushPromises()

    expect(wrapper.find('.empty-cart').text()).toBe('购物车是空的')
    expect(wrapper.findAll('.cart-item')).toHaveLength(0)
    expect(wrapper.find('.checkout-btn').exists()).toBe(false)
    wrapper.unmount()
  })

  it('点击数量 + 调用更新接口并刷新购物车', async () => {
    const wrapper = mountCart()
    await flushPromises()

    const plusButton = wrapper
      .findAll('.item-actions button')
      .find((button) => button.text() === '+')
    await plusButton.trigger('click')
    await flushPromises()

    expect(cartApi.updateQuantity).toHaveBeenCalledWith(1, 3)
    expect(cartApi.getCart).toHaveBeenCalledTimes(2)
    wrapper.unmount()
  })

  it('点击删除调用移除接口，点击结算跳转下单页', async () => {
    const wrapper = mountCart()
    await flushPromises()

    const removeButton = wrapper
      .findAll('.item-actions button')
      .find((button) => button.text() === '删除')
    await removeButton.trigger('click')
    await flushPromises()

    expect(cartApi.removeFromCart).toHaveBeenCalledWith(1)

    await wrapper.find('.checkout-btn').trigger('click')
    expect(routerPush).toHaveBeenCalledWith('/orders/create')
    wrapper.unmount()
  })
})
