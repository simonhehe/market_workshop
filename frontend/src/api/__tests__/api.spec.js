import { describe, it, expect, vi, beforeEach } from 'vitest'
import api from '../index'
import { cartApi } from '../cart'
import { orderApi } from '../order'

vi.mock('../index', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

describe('cartApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('calls the cart endpoints with expected payloads', () => {
    cartApi.getCart()
    cartApi.addToCart(3, 2)
    cartApi.selectItem(5, 0)
    cartApi.updateQuantity(5, 4)
    cartApi.removeFromCart(5)
    cartApi.clearCart()

    expect(api.get).toHaveBeenCalledWith('/cart')
    expect(api.post).toHaveBeenCalledWith('/cart', { productId: 3, quantity: 2 })
    expect(api.put).toHaveBeenCalledWith('/cart/5/select', { isSelected: 0 })
    expect(api.put).toHaveBeenCalledWith('/cart/5', { quantity: 4 })
    expect(api.delete).toHaveBeenCalledWith('/cart/5')
    expect(api.delete).toHaveBeenCalledWith('/cart/clear')
  })
})

describe('orderApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('calls the order endpoints with expected payloads', () => {
    orderApi.getOrders()
    orderApi.getOrderById(9)
    orderApi.createOrder({ addressId: 2, remark: '尽快' })
    orderApi.cancelOrder(9)

    expect(api.get).toHaveBeenCalledWith('/orders')
    expect(api.get).toHaveBeenCalledWith('/orders/9')
    expect(api.post).toHaveBeenCalledWith('/orders', { addressId: 2, remark: '尽快' })
    expect(api.put).toHaveBeenCalledWith('/orders/9/cancel')
  })
})
