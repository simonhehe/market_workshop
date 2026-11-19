import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Layout from '../src/components/Layout.vue'

vi.mock('../src/api/user', () => ({
  userApi: {
    getUserInfo: vi.fn().mockResolvedValue({ success: true, data: { points: 0 } })
  }
}))

describe('frontend test harness', () => {
  it('compiles and mounts a Vue SFC in happy-dom without real requests', () => {
    const wrapper = mount(Layout, {
      global: {
        stubs: { 'router-link': true }
      }
    })

    expect(wrapper.text()).toContain('企业内部福利商城')
    wrapper.unmount()
  })
})
