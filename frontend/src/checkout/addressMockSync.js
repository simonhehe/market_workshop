export function pickDefaultAddressId(addresses) {
  if (!addresses?.length) return null
  const def = addresses.find(a => a.isDefault === 1)
  return def?.id ?? addresses[0]?.id ?? null
}

export function buildBackendAddressIdMap(mockList, backendList) {
  const map = {}
  mockList.forEach((mock, index) => {
    const backend = backendList[index]
    if (backend?.id != null) {
      map[mock.id] = backend.id
    }
  })
  return map
}

export function resolveAddressId(selectedId, backendMap) {
  if (selectedId == null) return null
  return backendMap[selectedId] ?? selectedId
}

export function formatFullAddress(addr) {
  return `${addr.province} ${addr.city} ${addr.district} ${addr.detailAddress}`
}

export function needsAddressSeed(listLength, requiredLength) {
  return listLength < requiredLength
}
