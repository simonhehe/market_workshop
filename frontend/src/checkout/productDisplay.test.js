import { describe, it, expect } from 'vitest'
import { parseProductImageUrl, productThumbChar, isStockWarn } from './productDisplay.js'

describe('productDisplay', () => {
  it('parseProductImageUrl returns first image url', () => {
    const url = parseProductImageUrl({ images: '["https://img/a.png","https://img/b.png"]' })
    expect(url).toBe('https://img/a.png')
  })

  it('parseProductImageUrl returns null for invalid json', () => {
    expect(parseProductImageUrl({ images: 'not-json' })).toBeNull()
  })

  it('parseProductImageUrl returns null for empty array', () => {
    expect(parseProductImageUrl({ images: '[]' })).toBeNull()
  })

  it('parseProductImageUrl returns null when images missing', () => {
    expect(parseProductImageUrl({})).toBeNull()
  })

  it('productThumbChar falls back when name empty', () => {
    expect(productThumbChar({ name: '' })).toBe('\u5546')
  })

  it('productThumbChar uses first character of name', () => {
    expect(productThumbChar(null)).toBe('\u5546')
    expect(productThumbChar({ name: '\u8033\u673a' })).toBe('\u8033')
  })

  it('isStockWarn detects insufficient stock', () => {
    expect(isStockWarn({ stock: 1 }, 2)).toBe(true)
    expect(isStockWarn({ stock: 3 }, 2)).toBe(false)
    expect(isStockWarn(null, 1)).toBe(false)
  })
})
