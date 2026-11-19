import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'

vi.mock('../src/api/exports', () => ({
  productApi: { getProducts: vi.fn(), getProductById: vi.fn() },
  categoryApi: { getCategories: vi.fn() }
}))

import Products from '../src/views/Products.vue'
import { productApi, categoryApi } from '../src/api/exports'

const LayoutStub = { name: 'LayoutStub', template: '<div class="layout-stub"><slot /></div>' }

const products = [
  { id: 1, name: '降噪蓝牙耳机', description: '主动降噪', pointsRequired: 300, stock: 10 },
  { id: 2, name: '机械键盘', description: '青轴', pointsRequired: 500, stock: 5 }
]

const routerPush = vi.fn()

function mountProducts(routeQuery = {}) {
  return mount(Products, {
    global: {
      stubs: { Layout: LayoutStub },
      mocks: { $route: { query: routeQuery }, $router: { push: routerPush } }
    }
  })
}

async function settle(ms = 0) {
  if (ms > 0) {
    await vi.advanceTimersByTimeAsync(ms)
  }
  for (let i = 0; i < 20; i += 1) {
    await Promise.resolve()
  }
}

beforeEach(() => {
  vi.useFakeTimers()
  vi.clearAllMocks()
  vi.spyOn(console, 'log').mockImplementation(() => {})
  vi.spyOn(console, 'warn').mockImplementation(() => {})
  vi.spyOn(console, 'error').mockImplementation(() => {})
  categoryApi.getCategories.mockResolvedValue({ success: true, data: [{ id: 2, name: '数码' }] })
  productApi.getProducts.mockResolvedValue({ success: true, data: products })
})

afterEach(() => {
  vi.useRealTimers()
  vi.restoreAllMocks()
})

describe('Products.vue', () => {
  it('输入关键词后经过 300ms 防抖才触发查询，连续输入只发一次请求', async () => {
    const wrapper = mountProducts()
    await settle()
    expect(productApi.getProducts).toHaveBeenCalledTimes(1)

    const input = wrapper.find('input[type="text"]')
    await input.setValue('手机')
    expect(productApi.getProducts).toHaveBeenCalledTimes(1)

    await settle(299)
    expect(productApi.getProducts).toHaveBeenCalledTimes(1)

    await input.setValue('降噪耳机')
    await settle(300)
    expect(productApi.getProducts).toHaveBeenCalledTimes(2)
    expect(productApi.getProducts).toHaveBeenLastCalledWith({ keyword: '降噪耳机' })
    wrapper.unmount()
  })

  it('渲染结果计数与商品卡片', async () => {
    const wrapper = mountProducts()
    await settle()
    expect(wrapper.find('.result-count').text()).toBe('共 2 件商品')
    expect(wrapper.findAll('.product-card')).toHaveLength(2)
    expect(wrapper.text()).toContain('降噪蓝牙耳机')
    wrapper.unmount()
  })

  it('无匹配时显示带关键词的空态与清除按钮，点击清除后恢复列表', async () => {
    productApi.getProducts.mockImplementation((params = {}) => {
      return Promise.resolve({ success: true, data: params.keyword ? [] : products })
    })

    const wrapper = mountProducts()
    await settle()

    const input = wrapper.find('input[type="text"]')
    await input.setValue('不存在的商品')
    await settle(300)

    const empty = wrapper.find('.empty')
    expect(empty.text()).toContain('未找到与“不存在的商品”相关的商品')
    const clearButton = empty.find('button')
    expect(clearButton.text()).toBe('清除搜索')
    expect(wrapper.findAll('.product-card')).toHaveLength(0)

    await clearButton.trigger('click')
    await settle()

    expect(productApi.getProducts).toHaveBeenLastCalledWith({})
    expect(wrapper.findAll('.product-card')).toHaveLength(2)
    expect(wrapper.find('.empty').exists()).toBe(false)
    expect(input.element.value).toBe('')
    wrapper.unmount()
  })

  it('分类与关键词组合时同时传递 categoryId 与 keyword', async () => {
    const wrapper = mountProducts()
    await settle()

    await wrapper.findAll('select')[0].setValue('2')
    await settle()

    const input = wrapper.find('input[type="text"]')
    await input.setValue('耳机')
    await settle(300)

    expect(productApi.getProducts).toHaveBeenLastCalledWith({ categoryId: 2, keyword: '耳机' })
    wrapper.unmount()
  })

  it('商品接口异常时不崩溃，保持空列表并显示暂无商品', async () => {
    productApi.getProducts.mockRejectedValue(new Error('network down'))

    const wrapper = mountProducts()
    await settle()

    expect(wrapper.findAll('.product-card')).toHaveLength(0)
    expect(wrapper.find('.empty').text()).toContain('暂无商品')
    wrapper.unmount()
  })
})
