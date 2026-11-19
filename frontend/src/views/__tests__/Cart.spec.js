import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Cart from '../Cart.vue'

vi.mock('../../api/exports', () => ({
  cartApi: {
    getCart: vi.fn(),
    selectItem: vi.fn(),
    updateQuantity: vi.fn(),
    removeFromCart: vi.fn()
  },
  productApi: { getProductById: vi.fn() }
}))

import { cartApi, productApi } from '../../api/exports'

function mountCart(query = {}) {
  const push = vi.fn()
  const wrapper = mount(Cart, {
    global: {
      stubs: {
        Layout: { name: 'Layout', template: '<div><slot /></div>' }
      },
      mocks: {
        $router: { push },
        $route: { query }
      }
    }
  })
  return { wrapper, push }
}

function primeCart({ selectedCount = 2, items } = {}) {
  cartApi.getCart.mockResolvedValue({
    success: true,
    data: {
      cartItems: items || [
        { id: 11, productId: 101, quantity: 2, isSelected: 1 },
        { id: 12, productId: 102, quantity: 1, isSelected: 1 }
      ],
      summary: { selectedCount, totalPoints: 250 }
    }
  })
  productApi.getProductById.mockImplementation(id =>
    Promise.resolve({
      success: true,
      data: {
        id,
        name: id === 101 ? '积分水杯' : '积分鼠标',
        pointsRequired: id === 101 ? 100 : 50
      }
    })
  )
}

describe('Cart.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    primeCart()
  })

  it('persists checkbox changes through the selection API', async () => {
    cartApi.selectItem.mockResolvedValue({ success: true, data: { id: 11, isSelected: 0 } })
    const { wrapper } = mountCart()
    await flushPromises()

    await wrapper.find('input[type="checkbox"]').setValue(false)
    await flushPromises()

    expect(cartApi.selectItem).toHaveBeenCalledWith(11, 0)
    expect(cartApi.getCart).toHaveBeenCalledTimes(2)
  })

  it('shows a notice and stays on the cart when nothing is selected', async () => {
    primeCart({ selectedCount: 0 })
    const { wrapper, push } = mountCart()
    await flushPromises()

    await wrapper.find('.checkout-btn').trigger('click')

    expect(wrapper.text()).toContain('请先选择要兑换的商品')
    expect(push).not.toHaveBeenCalled()
  })

  it('shows the empty-selection notice passed from checkout', async () => {
    const { wrapper } = mountCart({ notice: 'empty' })
    await flushPromises()

    expect(wrapper.text()).toContain('请先选择要兑换的商品')
  })

  it('shows a business error when selection is forbidden', async () => {
    cartApi.selectItem.mockRejectedValue({
      response: {
        data: { success: false, message: '无权操作此购物车商品', error: { code: 'CART_ITEM_FORBIDDEN' } }
      }
    })
    const { wrapper } = mountCart()
    await flushPromises()

    await wrapper.find('input[type="checkbox"]').setValue(false)
    await flushPromises()

    expect(wrapper.text()).toContain('无权操作此购物车商品')
  })

  it('shows a message when quantity update fails', async () => {
    cartApi.updateQuantity.mockRejectedValue({
      response: { data: { message: '库存不足，无法调整数量' } }
    })
    const { wrapper } = mountCart()
    await flushPromises()

    await wrapper.findAll('.cart-item')[0].findAll('.item-actions button')[1].trigger('click')
    await flushPromises()

    expect(cartApi.updateQuantity).toHaveBeenCalledWith(11, 3)
    expect(wrapper.text()).toContain('库存不足，无法调整数量')
  })

  it('shows a message when removal fails', async () => {
    cartApi.removeFromCart.mockRejectedValue({
      response: { data: { message: '无权操作此购物车商品' } }
    })
    const { wrapper } = mountCart()
    await flushPromises()

    await wrapper.findAll('.cart-item')[0].find('.remove-btn').trigger('click')
    await flushPromises()

    expect(cartApi.removeFromCart).toHaveBeenCalledWith(11)
    expect(wrapper.text()).toContain('无权操作此购物车商品')
  })
})
