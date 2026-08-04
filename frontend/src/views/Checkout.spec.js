import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Checkout from './Checkout.vue'
import {
  MOCK_ADDRESS_SCENARIO_EMPTY,
  MOCK_ADDRESS_SCENARIO_NORMAL
} from '../mocks/checkoutAddresses.js'

vi.mock('../components/Layout.vue', () => ({
  default: { template: '<div><slot /></div>' }
}))

const push = vi.fn()
const route = { query: {} }

vi.mock('../api/exports', () => ({
  userApi: { getUserInfo: vi.fn() },
  cartApi: { getCart: vi.fn() },
  productApi: { getProductById: vi.fn() },
  addressApi: { getAddresses: vi.fn(), createAddress: vi.fn() },
  orderApi: { createOrder: vi.fn() }
}))

import {
  userApi,
  cartApi,
  productApi,
  addressApi,
  orderApi
} from '../api/exports'

function mountCheckout() {
  return mount(Checkout, {
    global: {
      stubs: {
        'router-link': {
          props: ['to'],
          template: '<a :href="to"><slot /></a>'
        }
      },
      mocks: {
        $router: { push },
        $route: route
      }
    }
  })
}

describe('Checkout.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    route.query = {}
    push.mockReset()
    userApi.getUserInfo.mockResolvedValue({ success: true, data: { points: 500 } })
    cartApi.getCart.mockResolvedValue({
      success: true,
      data: {
        cartItems: [{ id: 10, productId: 1, quantity: 1, isSelected: 1 }],
        summary: { selectedCount: 1, totalPoints: 50 }
      }
    })
    productApi.getProductById.mockResolvedValue({
      success: true,
      data: { id: 1, name: '\u6d4b\u8bd5\u5546\u54c1', pointsRequired: 50, stock: 5, images: '[]' }
    })
    addressApi.getAddresses.mockResolvedValue({ success: true, data: [] })
    addressApi.createAddress.mockImplementation(async (payload) => ({
      success: true,
      data: { id: 100 + payload.receiver.length, ...payload }
    }))
    orderApi.createOrder.mockResolvedValue({ success: true, data: { id: 9001 } })
  })

  it('renders mock addresses and default selection after load', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.find('.address-list .address-item').exists()).toBe(true)
    expect(wrapper.findAll('.address-item')).toHaveLength(2)
    const vm = wrapper.vm
    expect(vm.selectedAddressId).toBe(1)
  })

  it('shows empty cart hint when cart is empty', async () => {
    cartApi.getCart.mockResolvedValueOnce({ success: true, data: { cartItems: [] } })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.text()).toContain('\u8d2d\u7269\u8f66\u4e3a\u7a7a')
  })

  it('shows unselected hint when nothing selected', async () => {
    cartApi.getCart.mockResolvedValueOnce({
      success: true,
      data: { cartItems: [{ id: 1, productId: 1, quantity: 1, isSelected: 0 }] }
    })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.text()).toContain('\u52fe\u9009')
  })

  it('shows empty address state when mock scenario is empty', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    const select = wrapper.find('#addressMockSelect')
    await select.setValue(MOCK_ADDRESS_SCENARIO_EMPTY)
    await flushPromises()
    expect(wrapper.find('.address-empty').exists()).toBe(true)
    expect(wrapper.vm.selectedAddressId).toBeNull()
  })

  it('reads empty mock scenario from route query on mount', async () => {
    route.query = { mockAddress: MOCK_ADDRESS_SCENARIO_EMPTY }
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.vm.addressMockScenario).toBe(MOCK_ADDRESS_SCENARIO_EMPTY)
    expect(wrapper.find('.address-empty').exists()).toBe(true)
  })

  it('blocks submit and opens modal when no address selected', async () => {
    route.query = { mockAddress: MOCK_ADDRESS_SCENARIO_EMPTY }
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(orderApi.createOrder).not.toHaveBeenCalled()
    expect(wrapper.find('.overlay.show').exists()).toBe(true)
    expect(wrapper.vm.errorBanner).toContain('\u6536\u8d27\u5730\u5740')
  })

  it('submits order with resolved backend address id', async () => {
    addressApi.getAddresses.mockResolvedValueOnce({
      success: true,
      data: [{ id: 501, receiver: 'a' }, { id: 502, receiver: 'b' }]
    })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(orderApi.createOrder).toHaveBeenCalledWith(
      expect.objectContaining({ addressId: 501 })
    )
    expect(push).toHaveBeenCalledWith('/orders/9001')
  })

  it('shows api failure in banner and modal', async () => {
    orderApi.createOrder.mockResolvedValueOnce({ success: false, message: '\u79ef\u5206\u4e0d\u8db3' })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(wrapper.vm.errorBanner).toBe('\u79ef\u5206\u4e0d\u8db3')
    expect(wrapper.find('.overlay.show').exists()).toBe(true)
  })

  it('step link navigates back to cart', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    const link = wrapper.find('.step-link')
    expect(link.attributes('href')).toBe('/cart')
  })

  it('seeds backend addresses when list is short', async () => {
    addressApi.getAddresses.mockResolvedValueOnce({ success: true, data: [] })
    addressApi.createAddress
      .mockResolvedValueOnce({ success: true, data: { id: 801 } })
      .mockResolvedValueOnce({ success: true, data: { id: 802 } })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(addressApi.createAddress).toHaveBeenCalledTimes(2)
    expect(wrapper.vm.backendAddressIds[1]).toBe(801)
  })

  it('shows points gap when user points insufficient', async () => {
    userApi.getUserInfo.mockResolvedValueOnce({ success: true, data: { points: 10 } })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.find('.points-gap.show').exists()).toBe(true)
  })

  it('returns to normal addresses when scenario switched back', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('#addressMockSelect').setValue(MOCK_ADDRESS_SCENARIO_EMPTY)
    await flushPromises()
    await wrapper.find('#addressMockSelect').setValue(MOCK_ADDRESS_SCENARIO_NORMAL)
    await flushPromises()
    expect(wrapper.findAll('.address-item')).toHaveLength(2)
  })

  it('handles loadData failure with emptyHint', async () => {
    userApi.getUserInfo.mockRejectedValueOnce(new Error('network'))
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.vm.emptyHint).toContain('\u52a0\u8f7d\u5931\u8d25')
  })

  it('handles submitOrder network error', async () => {
    orderApi.createOrder.mockRejectedValueOnce({
      response: { data: { message: '\u7f51\u7edc\u5f02\u5e38' } }
    })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(wrapper.vm.modalMessage).toBe('\u7f51\u7edc\u5f02\u5e38')
  })

  it('continues when getAddresses throws during seed', async () => {
    addressApi.getAddresses.mockRejectedValueOnce(new Error('addr fail'))
    addressApi.createAddress
      .mockResolvedValueOnce({ success: true, data: { id: 901 } })
      .mockResolvedValueOnce({ success: true, data: { id: 902 } })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(addressApi.createAddress).toHaveBeenCalled()
    expect(wrapper.vm.backendAddressIds[1]).toBe(901)
  })

  it('stops seeding when createAddress fails', async () => {
    addressApi.getAddresses.mockResolvedValueOnce({ success: true, data: [] })
    addressApi.createAddress.mockResolvedValueOnce({ success: false })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(addressApi.createAddress).toHaveBeenCalledTimes(1)
    expect(wrapper.vm.backendAddressIds).toEqual({})
  })

  it('selectAddress clears error banner', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    wrapper.vm.errorBanner = 'err'
    await wrapper.findAll('.address-item')[1].trigger('click')
    expect(wrapper.vm.selectedAddressId).toBe(2)
    expect(wrapper.vm.errorBanner).toBe('')
  })

  it('shows stock warning when inventory low', async () => {
    productApi.getProductById.mockResolvedValueOnce({
      success: true,
      data: { id: 1, name: 'x', pointsRequired: 50, stock: 0, images: '[]' }
    })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.find('.stock-warn.show').exists()).toBe(true)
  })

  it('submits remark when provided', async () => {
    const wrapper = mountCheckout()
    await flushPromises()
    wrapper.vm.remark = 'leave at door'
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(orderApi.createOrder).toHaveBeenCalledWith(
      expect.objectContaining({ remark: 'leave at door' })
    )
  })

  it('uses default message when submit response lacks message', async () => {
    orderApi.createOrder.mockResolvedValueOnce({ success: false })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(wrapper.vm.modalMessage).toBe('\u4e0b\u5355\u5931\u8d25')
  })

  it('uses err.message when submit throws without response body', async () => {
    orderApi.createOrder.mockRejectedValueOnce(new Error('boom'))
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(wrapper.vm.modalMessage).toBe('boom')
  })

  it('closes modal via overlay self click', async () => {
    orderApi.createOrder.mockResolvedValueOnce({ success: false, message: 'x' })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(wrapper.vm.showModal).toBe(true)
    await wrapper.find('.overlay.show').trigger('click')
    expect(wrapper.vm.showModal).toBe(false)
  })

  it('handles unsuccessful cart response as empty cart', async () => {
    cartApi.getCart.mockResolvedValueOnce({ success: false })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.vm.emptyHint).toContain('\u8d2d\u7269\u8f66\u4e3a\u7a7a')
  })

  it('keeps zero points when user info unsuccessful', async () => {
    userApi.getUserInfo.mockResolvedValueOnce({ success: false })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.vm.userPoints).toBe(0)
  })

  it('skips product when product api unsuccessful', async () => {
    productApi.getProductById.mockResolvedValueOnce({ success: false })
    const wrapper = mountCheckout()
    await flushPromises()
    expect(wrapper.vm.products[1]).toBeUndefined()
  })

  it('shows failure when order success without id', async () => {
    orderApi.createOrder.mockResolvedValueOnce({ success: true, data: {} })
    const wrapper = mountCheckout()
    await flushPromises()
    await wrapper.find('.btn-submit').trigger('click')
    await flushPromises()
    expect(push).not.toHaveBeenCalled()
    expect(wrapper.vm.showModal).toBe(true)
  })
})
