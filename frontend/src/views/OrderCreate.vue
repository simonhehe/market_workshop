<template>
  <Layout>
    <div class="order-create">
      <h2>确认下单</h2>

      <div v-if="loading" class="card muted">加载中...</div>

      <div v-else-if="!canSubmit" class="card warn">
        <p>购物车中没有已选中的商品，请先返回购物车勾选商品。</p>
        <button type="button" class="link-btn" @click="$router.push('/cart')">返回购物车</button>
      </div>

      <div v-else class="card">
        <p class="hint">已选 <strong>{{ summary.selectedCount }}</strong> 件，共 <strong>{{ summary.totalPoints }}</strong> 积分</p>

        <div class="field">
          <label for="addr">收货地址（可选）</label>
          <select id="addr" v-model.number="addressId" class="control">
            <option :value="null">不指定地址</option>
            <option v-for="a in addresses" :key="a.id" :value="a.id">
              {{ formatAddressLine(a) }}
            </option>
          </select>
        </div>

        <div class="field">
          <label for="remark">备注（可选）</label>
          <textarea id="remark" v-model="remark" rows="3" class="control" placeholder="备注信息" />
        </div>

        <div class="actions">
          <button type="button" class="secondary" @click="$router.push('/cart')">取消</button>
          <button type="button" class="primary" :disabled="submitting" @click="submit">
            {{ submitting ? '提交中…' : '提交订单' }}
          </button>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'
import { addressApi, cartApi, orderApi } from '../api/exports'

export default {
  name: 'OrderCreate',
  components: { Layout },
  data() {
    return {
      loading: true,
      submitting: false,
      summary: { selectedCount: 0, totalPoints: 0 },
      addresses: [],
      addressId: null,
      remark: ''
    }
  },
  computed: {
    canSubmit() {
      return this.summary.selectedCount > 0 && this.summary.totalPoints > 0
    }
  },
  async mounted() {
    await this.loadData()
  },
  methods: {
    formatAddressLine(a) {
      if (!a) return ''
      const region = [a.province, a.city, a.district].filter(Boolean).join(' ')
      const tail = a.detailAddress || ''
      const who = [a.receiver, a.phone].filter(Boolean).join(' ')
      return `${who} ${region} ${tail}`.trim()
    },
    async loadData() {
      this.loading = true
      try {
        const cartRes = await cartApi.getCart()
        if (cartRes.success) {
          this.summary = cartRes.data.summary || {}
        }
        const addrRes = await addressApi.getAddresses()
        if (addrRes.success) {
          this.addresses = addrRes.data || []
          const def = this.addresses.find((x) => x.isDefault === 1)
          if (def) this.addressId = def.id
        }
      } catch (e) {
        console.error('加载下单页失败', e)
        alert('加载失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    async submit() {
      if (!this.canSubmit || this.submitting) return
      this.submitting = true
      try {
        const body = {}
        if (this.addressId != null) body.addressId = this.addressId
        if (this.remark && this.remark.trim()) body.remark = this.remark.trim()
        const res = await orderApi.createOrder(body)
        if (res.success && res.data && res.data.id != null) {
          this.$router.replace(`/orders/${res.data.id}`)
        } else {
          alert(res.message || '下单失败')
        }
      } catch (err) {
        console.error('下单失败', err)
        alert(err.response?.data?.message || err.message || '下单失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.order-create {
  padding: 20px 0;
}
.order-create h2 {
  font-size: 24px;
  margin-bottom: 20px;
}
.card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  max-width: 560px;
}
.card.muted {
  color: #666;
}
.card.warn {
  color: #856404;
  background: #fff3cd;
  border: 1px solid #ffeeba;
}
.hint {
  margin-bottom: 20px;
  color: #444;
}
.field {
  margin-bottom: 16px;
}
.field label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}
.control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}
.actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  justify-content: flex-end;
}
.actions button {
  padding: 10px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 15px;
}
.actions .primary {
  background: #667eea;
  color: #fff;
}
.actions .primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.actions .secondary {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}
.link-btn {
  margin-top: 12px;
  padding: 8px 0;
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 15px;
  text-decoration: underline;
}
</style>
