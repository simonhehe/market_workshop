// Temporary mock for checkout until address management is implemented.

export const MOCK_ADDRESS_SCENARIO_NORMAL = 'normal'
export const MOCK_ADDRESS_SCENARIO_EMPTY = 'empty'

export const MOCK_CHECKOUT_ADDRESSES = [
  {
    id: 1,
    receiver: '\u5f20\u4e09',
    phone: '13800138001',
    province: '\u5317\u4eac\u5e02',
    city: '\u5317\u4eac\u5e02',
    district: '\u671d\u9633\u533a',
    detailAddress: '\u5efa\u56fd\u8def 88 \u53f7 SOHO \u73b0\u4ee3\u57ce A \u5ea7 1201',
    isDefault: 1
  },
  {
    id: 2,
    receiver: '\u5f20\u4e09',
    phone: '13800138001',
    province: '\u4e0a\u6d77\u5e02',
    city: '\u4e0a\u6d77\u5e02',
    district: '\u6d66\u4e1c\u65b0\u533a',
    detailAddress: '\u4e16\u7eaa\u5927\u9053 1000 \u53f7 25 \u5c42',
    isDefault: 0
  }
]

/** @param {'normal'|'empty'} scenario */
export function getMockAddressesForScenario(scenario) {
  if (scenario === MOCK_ADDRESS_SCENARIO_EMPTY) {
    return []
  }
  return MOCK_CHECKOUT_ADDRESSES.map(a => ({ ...a }))
}

export const MOCK_ADDRESS_SCENARIO_OPTIONS = [
  { value: MOCK_ADDRESS_SCENARIO_NORMAL, label: '2\u6761\u5730\u5740\uff08\u9ed8\u8ba4\uff09' },
  { value: MOCK_ADDRESS_SCENARIO_EMPTY, label: '\u3010\u7a7a\u3011\u65e0\u6536\u8d27\u5730\u5740' }
]
