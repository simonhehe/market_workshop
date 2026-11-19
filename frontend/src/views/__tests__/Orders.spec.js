import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Orders from '../Orders.vue'

vi.mock('../../api/exports', () => ({
  orderApi: {
    getOrders: vi.fn(),
    cancelOrder: vi.fn()
  }
}))

import { orderApi } from '../../api/exports'

const orders = [
  {
    id: 1,
    orderNo: 'ORD-PENDING',
    status: 'pending',
    totalPoints: 100,
    createdAt: '2026-11-20T10:00:00'
  },
  {
    id: 2,
    orderNo: 'ORD-COMPLETED',
    status: 'completed',
    totalPoints: 50,
    createdAt: '2026-11-19T10:00:00'
  }
]

function mountOrders() {
  const push = vi.fn()
  const wrapper = mount(Orders, {
    global: {
      stubs: {
        Layout: { name: 'Layout', template: '<div><slot /></div>' }
      },
      mocks: {
        $router: { push },
        $route: { query: {} }
      }
    }
  })
  return { wrapper, push }
}

describe('Orders.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    orderApi.getOrders.mockResolvedValue({ success: true, data: orders })
    globalThis.confirm = vi.fn(() => true)
    globalThis.alert = vi.fn()
  })

  it('maps server status values and only offers cancel for pending orders', async () => {
    const { wrapper } = mountOrders()
    await flushPromises()

    const cards = wrapper.findAll('.order-card')
    expect(cards[0].text()).toContain('待处理')
    expect(cards[1].text()).toContain('已完成')
    expect(cards[0].find('.cancel-btn').exists()).toBe(true)
    expect(cards[1].find('.cancel-btn').exists()).toBe(false)
  })

  it('cancels a pending order and reloads the list', async () => {
    orderApi.cancelOrder.mockResolvedValue({ success: true, data: { id: 1, status: 'cancelled' } })
    const { wrapper } = mountOrders()
    await flushPromises()

    await wrapper.find('.cancel-btn').trigger('click')
    await flushPromises()

    expect(orderApi.cancelOrder).toHaveBeenCalledWith(1)
    expect(orderApi.getOrders).toHaveBeenCalledTimes(2)
  })
})
