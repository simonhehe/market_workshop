export function parseProductImageUrl(product) {
  if (!product?.images) return null
  try {
    const imgs = JSON.parse(product.images)
    if (Array.isArray(imgs) && imgs.length > 0) {
      return imgs[0]
    }
  } catch {
    /* invalid JSON */
  }
  return null
}

export function productThumbChar(product) {
  if (!product) return '\u5546'
  return product.name?.[0] || '\u5546'
}

export function isStockWarn(product, quantity) {
  return Boolean(product && product.stock < quantity)
}
