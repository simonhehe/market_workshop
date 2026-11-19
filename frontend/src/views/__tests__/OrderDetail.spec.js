import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import OrderDetail from '../OrderDetail.vue'

vi.mock('../../api/exports', () => ({
  orderApi: {
    getOrderById: vi.fn(),
    cancelOrder: vi.fn()
  }
}))

import { orderApi } from '../../api/exports'

function orderDetail(status) {
  return {
    success: true,
    data: {
      order: {
        id: 1,
        orderNo: 'ORD-DETAIL',
        status,
        totalPoints: 100,
        receiver: '张三',
        receiverPhone: '13800000000',
        receiverAddress: '浙江省 杭州市 西湖区 文一西路 1 号'
      },
      orderItems: [
        { id: 21, productName: '积分水杯', pointsRequired: 50, quantity: 2, subtotalPoints: 100 }
      ]
    }
  }
}

function mountDetail() {
  const push = vi.fn()
  const wrapper = mount(OrderDetail, {
    global: {
      stubs: {
        Layout: { name: 'Layout', template: '<div><slot /></div>' }
      },
      mocks: {
        $router: { push },
        $route: { params: { id: '1' } }
      }
    }
  })
  return { wrapper, push }
}

describe('OrderDetail.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    globalThis.confirm = vi.fn(() => true)
  })

  it('shows a cancel action for pending orders and refreshes after cancelling', async () => {
    orderApi.getOrderById
      .mockResolvedValueOnce(orderDetail('pending'))
      .mockResolvedValueOnce(orderDetail('cancelled'))
    orderApi.cancelOrder.mockResolvedValue({ success: true, data: { id: 1, status: 'cancelled' } })

    const { wrapper } = mountDetail()
    await flushPromises()

    expect(wrapper.text()).toContain('待处理')
    expect(wrapper.find('.cancel-btn').exists()).toBe(true)

    await wrapper.find('.cancel-btn').trigger('click')
    await flushPromises()

    expect(orderApi.cancelOrder).toHaveBeenCalledWith(1)
    expect(wrapper.text()).toContain('已取消')
    expect(wrapper.find('.cancel-btn').exists()).toBe(false)
  })

  it('does not show a cancel action for non-pending orders', async () => {
    orderApi.getOrderById.mockResolvedValue(orderDetail('completed'))

    const { wrapper } = mountDetail()
    await flushPromises()

    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.find('.cancel-btn').exists()).toBe(false)
  })
})
