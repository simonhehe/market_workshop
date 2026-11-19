import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Checkout from '../Checkout.vue'

vi.mock('../../api/exports', () => ({
  cartApi: { getCart: vi.fn() },
  addressApi: { getAddresses: vi.fn() },
  productApi: { getProductById: vi.fn() },
  orderApi: { createOrder: vi.fn() },
  userApi: { getUserInfo: vi.fn() }
}))

import { cartApi, addressApi, productApi, orderApi, userApi } from '../../api/exports'

const products = {
  101: { id: 101, name: '积分水杯', pointsRequired: 100, stock: 5, images: '[]' },
  102: { id: 102, name: '积分鼠标', pointsRequired: 50, stock: 3, images: '[]' },
  103: { id: 103, name: '未选商品', pointsRequired: 60, stock: 5, images: '[]' }
}

const addresses = [
  {
    id: 1,
    receiver: '张三',
    phone: '13800000000',
    province: '浙江省',
    city: '杭州市',
    district: '西湖区',
    detailAddress: '文一西路 1 号'
  },
  {
    id: 2,
    receiver: '李四',
    phone: '13900000000',
    province: '上海市',
    city: '上海市',
    district: '浦东新区',
    detailAddress: '世纪大道 100 号'
  }
]

function mountCheckout(query = {}) {
  const push = vi.fn()
  const replace = vi.fn()
  const wrapper = mount(Checkout, {
    global: {
      stubs: {
        Layout: { name: 'Layout', template: '<div><slot /></div>' }
      },
      mocks: {
        $router: { push, replace },
        $route: { query }
      }
    }
  })
  return { wrapper, push, replace }
}

function primeApi({ stock101 = 5, points = 1000, selectedItems } = {}) {
  cartApi.getCart.mockResolvedValue({
    success: true,
    data: {
      cartItems: selectedItems || [
        { id: 11, productId: 101, quantity: 2, isSelected: 1 },
        { id: 12, productId: 102, quantity: 1, isSelected: 1 },
        { id: 13, productId: 103, quantity: 1, isSelected: 0 }
      ],
      summary: {}
    }
  })
  addressApi.getAddresses.mockResolvedValue({ success: true, data: addresses })
  userApi.getUserInfo.mockResolvedValue({ success: true, data: { points } })
  productApi.getProductById.mockImplementation(id =>
    Promise.resolve({
      success: true,
      data: {
        ...products[id],
        stock: id === 101 ? stock101 : products[id].stock
      }
    })
  )
}

describe('Checkout.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    primeApi()
  })

  it('loads only selected items, addresses and server-side current points', async () => {
    const { wrapper } = mountCheckout()

    await flushPromises()

    expect(wrapper.text()).toContain('积分水杯')
    expect(wrapper.text()).toContain('积分鼠标')
    expect(wrapper.text()).not.toContain('未选商品')
    expect(wrapper.findAll('.address-card')).toHaveLength(2)
    expect(wrapper.find('.address-card.selected').exists()).toBe(false)
    expect(wrapper.find('.payable-points').text()).toContain('250')
    expect(wrapper.find('.balance-after').text()).toContain('750')
    expect(productApi.getProductById).toHaveBeenCalledTimes(2)
  })

  it('redirects to cart when there are no selected items', async () => {
    primeApi({
      selectedItems: [{ id: 13, productId: 103, quantity: 1, isSelected: 0 }]
    })
    const { wrapper, replace } = mountCheckout()

    await flushPromises()

    expect(replace).toHaveBeenCalledWith({ path: '/cart', query: { notice: 'empty' } })
    expect(wrapper.find('.checkout-form').exists()).toBe(false)
  })

  it('blocks submit without an address and does not call the order API', async () => {
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-banner').text()).toContain('请选择收货地址后再提交订单')
    expect(orderApi.createOrder).not.toHaveBeenCalled()
  })

  it('selects an address and submits only addressId and remark', async () => {
    orderApi.createOrder.mockResolvedValue({
      success: true,
      data: { id: 9, orderNo: 'ORD20261120001', totalPoints: 250, status: 'pending' }
    })
    const { wrapper } = mountCheckout()
    await flushPromises()

    const cards = wrapper.findAll('.address-card')
    await cards[1].trigger('click')
    expect(wrapper.findAll('.address-card')[1].classes()).toContain('selected')

    await wrapper.find('.remark-input').setValue('尽快发货')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(orderApi.createOrder).toHaveBeenCalledTimes(1)
    expect(orderApi.createOrder).toHaveBeenCalledWith({ addressId: 2, remark: '尽快发货' })
    expect(wrapper.find('.success-card').text()).toContain('ORD20261120001')
    expect(wrapper.find('.success-card').text()).toContain('250')
    expect(wrapper.find('.success-card').text()).toContain('待处理')
  })

  it('updates totals when quantity changes and caps at stock', async () => {
    const { wrapper } = mountCheckout()
    await flushPromises()

    const firstRow = wrapper.findAll('.checkout-item')[0]
    await firstRow.find('.increase-btn').trigger('click')
    expect(wrapper.find('.payable-points').text()).toContain('350')

    for (let i = 0; i < 5; i++) {
      await wrapper.findAll('.checkout-item')[0].find('.increase-btn').trigger('click')
    }
    const cappedRow = wrapper.findAll('.checkout-item')[0]
    expect(cappedRow.find('.quantity-value').text()).toBe('5')
    expect(cappedRow.find('.increase-btn').attributes('disabled')).toBeDefined()
    expect(wrapper.find('.payable-points').text()).toContain('550')
  })

  it('prevents duplicate submissions while a request is in flight', async () => {
    let resolveOrder
    orderApi.createOrder.mockImplementation(
      () => new Promise(resolve => {
        resolveOrder = resolve
      })
    )
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    const button = wrapper.find('.submit-btn')
    await button.trigger('click')
    await button.trigger('click')

    expect(orderApi.createOrder).toHaveBeenCalledTimes(1)
    expect(wrapper.find('.submit-btn').attributes('disabled')).toBeDefined()

    resolveOrder({ success: true, data: { id: 9, orderNo: 'ORD-X', totalPoints: 250, status: 'pending' } })
    await flushPromises()
  })

  it('shows stock errors and marks the affected row before calling the API', async () => {
    primeApi({ stock101: 1 })
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-banner').text()).toContain('库存不足')
    expect(wrapper.findAll('.checkout-item')[0].classes()).toContain('stock-warning')
    expect(orderApi.createOrder).not.toHaveBeenCalled()
  })

  it('blocks submission when points are insufficient', async () => {
    primeApi({ points: 100 })
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-banner').text()).toContain('积分不足')
    expect(orderApi.createOrder).not.toHaveBeenCalled()
  })

  it('maps server business errors to a visible message', async () => {
    orderApi.createOrder.mockRejectedValue({
      response: {
        data: {
          success: false,
          message: '收货地址无效，请重新选择',
          error: { code: 'ADDR_NOT_FOUND', message: '收货地址无效，请重新选择' }
        }
      }
    })
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-banner').text()).toContain('收货地址无效，请重新选择')
  })

  it('shows the server error when the create response is not successful', async () => {
    orderApi.createOrder.mockResolvedValue({
      success: false,
      message: '请先选择要兑换的商品',
      error: { code: 'CART_EMPTY', message: '请先选择要兑换的商品' }
    })
    const { wrapper } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-banner').text()).toContain('请先选择要兑换的商品')
    expect(wrapper.find('.success-card').exists()).toBe(false)
  })

  it('navigates from the success card to the order or products', async () => {
    orderApi.createOrder.mockResolvedValue({
      success: true,
      data: { id: 9, orderNo: 'ORD20261120002', totalPoints: 250, status: 'pending' }
    })
    const { wrapper, push } = mountCheckout()
    await flushPromises()

    await wrapper.findAll('.address-card')[0].trigger('click')
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    await wrapper.find('.detail-btn').trigger('click')
    expect(push).toHaveBeenCalledWith('/orders/9')

    await wrapper.find('.continue-btn').trigger('click')
    expect(push).toHaveBeenCalledWith('/products')
  })
})
