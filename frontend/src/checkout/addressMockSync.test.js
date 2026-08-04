import { describe, it, expect } from 'vitest'
import {
  pickDefaultAddressId,
  buildBackendAddressIdMap,
  resolveAddressId,
  formatFullAddress,
  needsAddressSeed
} from './addressMockSync.js'

describe('addressMockSync', () => {
  const addresses = [
    { id: 2, isDefault: 0 },
    { id: 1, isDefault: 1 }
  ]

  it('pickDefaultAddressId prefers default flag', () => {
    expect(pickDefaultAddressId(addresses)).toBe(1)
  })

  it('pickDefaultAddressId falls back to first address', () => {
    expect(pickDefaultAddressId([{ id: 9, isDefault: 0 }])).toBe(9)
  })

  it('pickDefaultAddressId returns null for empty list', () => {
    expect(pickDefaultAddressId([])).toBeNull()
  })

  it('pickDefaultAddressId handles null input', () => {
    expect(pickDefaultAddressId(null)).toBeNull()
  })

  it('buildBackendAddressIdMap skips entries without backend id', () => {
    expect(buildBackendAddressIdMap([{ id: 1 }, { id: 2 }], [{ id: 101 }])).toEqual({ 1: 101 })
  })

  it('resolveAddressId uses backend map when present', () => {
    expect(resolveAddressId(1, { 1: 101 })).toBe(101)
  })

  it('resolveAddressId falls back to selected id', () => {
    expect(resolveAddressId(5, {})).toBe(5)
  })

  it('resolveAddressId returns null when selection missing', () => {
    expect(resolveAddressId(null, { 1: 101 })).toBeNull()
  })

  it('formatFullAddress joins province to detail', () => {
    expect(
      formatFullAddress({
        province: '\u5317\u4eac\u5e02',
        city: '\u5317\u4eac\u5e02',
        district: '\u671d\u9633\u533a',
        detailAddress: '\u6d4b\u8bd5\u8def 1 \u53f7'
      })
    ).toBe('\u5317\u4eac\u5e02 \u5317\u4eac\u5e02 \u671d\u9633\u533a \u6d4b\u8bd5\u8def 1 \u53f7')
  })

  it('needsAddressSeed compares lengths', () => {
    expect(needsAddressSeed(0, 2)).toBe(true)
    expect(needsAddressSeed(2, 2)).toBe(false)
  })
})
