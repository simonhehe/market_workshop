export function computeItemCount(selectedItems) {
  return selectedItems.reduce((sum, item) => sum + item.quantity, 0)
}

export function computeSubtotalPoints(selectedItems, products) {
  return selectedItems.reduce((sum, item) => {
    const product = products[item.productId]
    return sum + (product ? product.pointsRequired * item.quantity : 0)
  }, 0)
}

export function computePointsGap(subtotalPoints, userPoints) {
  const gap = subtotalPoints - userPoints
  return gap > 0 ? gap : 0
}
