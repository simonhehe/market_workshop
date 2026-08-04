import { describe, it, expect } from 'vitest'
import { computeItemCount, computeSubtotalPoints, computePointsGap } from './cartSummary.js'

describe('cartSummary', () => {
  const items = [
    { productId: 1, quantity: 2 },
    { productId: 2, quantity: 1 }
  ]
  const products = {
    1: { pointsRequired: 50 },
    2: { pointsRequired: 30 }
  }

  it('computeItemCount sums quantities', () => {
    expect(computeItemCount(items)).toBe(3)
  })

  it('computeSubtotalPoints sums selected product points', () => {
    expect(computeSubtotalPoints(items, products)).toBe(130)
  })

  it('computeSubtotalPoints ignores missing products', () => {
    expect(computeSubtotalPoints([{ productId: 99, quantity: 1 }], products)).toBe(0)
  })

  it('computePointsGap returns positive gap only', () => {
    expect(computePointsGap(200, 100)).toBe(100)
    expect(computePointsGap(80, 100)).toBe(0)
  })
})
