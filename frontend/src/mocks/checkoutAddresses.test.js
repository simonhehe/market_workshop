import { describe, it, expect } from 'vitest'
import {
  MOCK_ADDRESS_SCENARIO_EMPTY,
  MOCK_ADDRESS_SCENARIO_NORMAL,
  MOCK_CHECKOUT_ADDRESSES,
  MOCK_ADDRESS_SCENARIO_OPTIONS,
  getMockAddressesForScenario
} from './checkoutAddresses.js'

describe('checkoutAddresses mock', () => {
  it('returns two cloned addresses for normal scenario', () => {
    const list = getMockAddressesForScenario(MOCK_ADDRESS_SCENARIO_NORMAL)
    expect(list).toHaveLength(2)
    expect(list[0].isDefault).toBe(1)
    expect(list[1].id).toBe(2)
    expect(list).not.toBe(MOCK_CHECKOUT_ADDRESSES)
    expect(MOCK_CHECKOUT_ADDRESSES[0].receiver).toBe('\u5f20\u4e09')
  })

  it('returns empty list for empty scenario', () => {
    expect(getMockAddressesForScenario(MOCK_ADDRESS_SCENARIO_EMPTY)).toEqual([])
  })

  it('falls back to normal addresses for unknown scenario', () => {
    expect(getMockAddressesForScenario('unknown')).toHaveLength(2)
  })

  it('exposes scenario options including empty label', () => {
    expect(MOCK_ADDRESS_SCENARIO_OPTIONS).toHaveLength(2)
    expect(MOCK_ADDRESS_SCENARIO_OPTIONS[1].value).toBe(MOCK_ADDRESS_SCENARIO_EMPTY)
    expect(MOCK_ADDRESS_SCENARIO_OPTIONS[1].label).toContain('\u7a7a')
  })
})
