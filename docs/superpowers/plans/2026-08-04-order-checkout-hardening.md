# order-checkout-hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成福利商城下单结算闭环——购物车勾选 API、结算页 L2、地址/库存/积分校验、集成测试与走查。

**Architecture:** 后端 Spring Boot 3.2 扩展 Cart select + OrderController 入参规范化（OrderService 地址校验已完成）；前端 Vue 3 新增 Checkout.vue 加载 addresses/cart/products/user/info，提交 POST /orders 成功后跳详情；MockMvc 集成测试覆盖 T1–T8。

**Tech Stack:** Java 17 · Spring Boot 3.2 · JPA · SQLite · Vue 3 · Vue Router 4 · Axios · Vite 5 · spring-boot-starter-test

**Spec:** `docs/superpowers/specs/order-checkout-hardening.md`  
**Est. total:** ~95 min（工作坊 L3-4）

---

## File Structure（锁定）

| 路径 | 动作 | 职责 |
|------|------|------|
| `backend/.../service/CartService.java` | Modify | `selectCartItem` |
| `backend/.../controller/CartController.java` | Modify | `PUT /{id}/select` |
| `backend/.../controller/OrderController.java` | Modify | `parseAddressId` D9 |
| `backend/src/test/resources/application-test.yml` | Create | 测试用 SQLite |
| `backend/src/test/java/.../OrderCheckoutIntegrationTest.java` | Create | T1–T8 |
| `backend/src/test/java/.../TestDataHelper.java` | Create | 种子 + JWT |
| `frontend/src/api/cart.js` | Modify | `selectCartItem` |
| `frontend/src/views/Cart.vue` | Modify | toggle + guard |
| `frontend/src/views/Checkout.vue` | Create | 结算页 |
| `frontend/src/router/index.js` | Modify | `/orders/create` |
| `frontend/src/views/OrderDetail.vue` | Modify | remark |
| `product/compass-order-checkout.md` | Modify | QG 更新 |

---

### Task 1: CartService.selectCartItem（~5 min）

**Files:**
- Modify: `backend/src/main/java/com/example/mall/service/CartService.java`
- Test: （Task 11 起测）

- [ ] **Step 1: 在 CartService 末尾添加 selectCartItem 方法**

```java
@Transactional
public Cart selectCartItem(Long cartId, Long userId, Integer isSelected) {
    Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new RuntimeException("购物车项不存在"));
    if (!cart.getUserId().equals(userId)) {
        throw new RuntimeException("无权操作此购物车项");
    }
    if (isSelected == null || (isSelected != 0 && isSelected != 1)) {
        throw new RuntimeException("无效的选中状态");
    }
    cart.setIsSelected(isSelected);
    return cartRepository.save(cart);
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn -q compile`  
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/example/mall/service/CartService.java
git commit -m "feat(cart): add selectCartItem with ownership check"
```

---

### Task 2: CartController PUT /cart/{id}/select（~4 min）

**Files:**
- Modify: `backend/src/main/java/com/example/mall/controller/CartController.java`

- [ ] **Step 1: 在 removeFromCart 方法之前添加 endpoint**

```java
@PutMapping("/{id}/select")
public ResponseEntity<ApiResponse<Cart>> selectCartItem(
        @PathVariable Long id,
        @RequestAttribute("userId") Long userId,
        @RequestBody Map<String, Object> request) {
    try {
        Integer isSelected = Integer.valueOf(request.get("isSelected").toString());
        Cart cart = cartService.selectCartItem(id, userId, isSelected);
        return ResponseEntity.ok(ApiResponse.success("购物车勾选状态更新成功", cart));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("CART_001", e.getMessage()));
    }
}
```

- [ ] **Step 2: 编译**

Run: `cd backend && mvn -q compile`  
Expected: BUILD SUCCESS

- [ ] **Step 3: 手工冒烟（可选，~2 min）**

启后端后：
```bash
# 先 login 取 token，再 PUT（替换 TOKEN 和 CART_ID）
curl -s -X PUT http://localhost:8080/api/cart/1/select \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"isSelected":0}'
```
Expected: `"success":true` 或 CART_001（id 不存在时）

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/example/mall/controller/CartController.java
git commit -m "feat(cart): expose PUT /cart/{id}/select endpoint"
```

---

### Task 3: frontend cart.js selectCartItem（~2 min）

**Files:**
- Modify: `frontend/src/api/cart.js`

- [ ] **Step 1: 在 clearCart 前添加方法**

```javascript
  selectCartItem(id, isSelected) {
    return api.put(`/cart/${id}/select`, { isSelected })
  },
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/cart.js
git commit -m "feat(cart): add selectCartItem API client"
```

---

### Task 4: OrderController addressId 规范化 D9（~5 min）

**Files:**
- Modify: `backend/src/main/java/com/example/mall/controller/OrderController.java`

- [ ] **Step 1: 添加 private 解析方法（类内末尾）**

```java
private Long parseAddressId(Map<String, Object> request) {
    if (request == null || !request.containsKey("addressId") || request.get("addressId") == null) {
        return null;
    }
    String raw = request.get("addressId").toString().trim();
    if (raw.isEmpty()) {
        return null;
    }
    try {
        return Long.valueOf(raw);
    } catch (NumberFormatException e) {
        return null;
    }
}
```

- [ ] **Step 2: 替换 createOrder 内 addressId 解析**

将：
```java
Long addressId = request.containsKey("addressId") 
        ? Long.valueOf(request.get("addressId").toString()) 
        : null;
```
替换为：
```java
Long addressId = parseAddressId(request);
```

- [ ] **Step 3: 编译**

Run: `cd backend && mvn -q compile`  
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/example/mall/controller/OrderController.java
git commit -m "fix(order): normalize addressId input per D9"
```

---

### Task 5: Cart.vue 勾选与结算守卫（~5 min）

**Files:**
- Modify: `frontend/src/views/Cart.vue`

- [ ] **Step 1: 替换 toggleSelect 方法**

```javascript
    async toggleSelect(item) {
      const nextSelected = item.isSelected === 1 ? 0 : 1
      try {
        const response = await cartApi.selectCartItem(item.id, nextSelected)
        if (response.success) {
          await this.loadCart()
        }
      } catch (err) {
        alert(err.response?.data?.message || '更新勾选状态失败')
      }
    },
```

- [ ] **Step 2: 替换 checkout 方法**

```javascript
    checkout() {
      if (this.summary.selectedCount === 0) {
        alert('请先勾选要结算的商品')
        return
      }
      this.$router.push('/orders/create')
    }
```

- [ ] **Step 3: 浏览器验证（~2 min）**

Run: `cd frontend && npm run dev`（后端已启）  
Expected: 勾选 checkbox 刷新后状态保持；全不选点结算弹出提示

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/Cart.vue
git commit -m "feat(cart): wire select API and block empty checkout"
```

---

### Task 6: Router + Checkout 骨架（~5 min）

**Files:**
- Create: `frontend/src/views/Checkout.vue`
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: 创建 Checkout.vue 最小骨架**

```vue
<template>
  <Layout>
    <div class="checkout-page">
      <div class="steps-bar">① 购物车 → ② 确认订单 → ③ 完成</div>
      <p v-if="loading">加载中...</p>
      <p v-else-if="emptyHint">{{ emptyHint }}</p>
    </div>
  </Layout>
</template>

<script>
import Layout from '../components/Layout.vue'

export default {
  name: 'Checkout',
  components: { Layout },
  data() {
    return {
      loading: true,
      emptyHint: ''
    }
  },
  async mounted() {
    this.loading = false
  }
}
</script>

<style scoped>
.checkout-page { max-width: 860px; margin: 0 auto; padding: 16px; }
.steps-bar { margin-bottom: 16px; color: #e4393c; font-weight: 600; }
</style>
```

- [ ] **Step 2: router 注册路由**

在 `import OrderDetail` 后添加：
```javascript
import Checkout from '../views/Checkout.vue'
```

在 `/orders` 路由**之前**插入（避免 `:id` 吞 `/create`）：
```javascript
  {
    path: '/orders/create',
    name: 'Checkout',
    component: Checkout,
    meta: { requiresAuth: true }
  },
```

- [ ] **Step 3: 验证路由**

浏览器访问 `/orders/create`（已登录）  
Expected: 显示步骤条，非 404

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/Checkout.vue frontend/src/router/index.js
git commit -m "feat(checkout): add route and page skeleton"
```

---

### Task 7: Checkout 数据加载（~8 min）

**Files:**
- Modify: `frontend/src/views/Checkout.vue`

- [ ] **Step 1: 扩展 script — imports 与 data**

```javascript
import Layout from '../components/Layout.vue'
import { cartApi, addressApi, productApi, userApi, orderApi } from '../api/exports'

export default {
  name: 'Checkout',
  components: { Layout },
  data() {
    return {
      loading: true,
      emptyHint: '',
      userPoints: 0,
      addresses: [],
      selectedAddressId: null,
      selectedItems: [],
      products: {},
      remark: ''
    }
  },
```

- [ ] **Step 2: 实现 mounted + loadData**

```javascript
  async mounted() {
    await this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      this.emptyHint = ''
      try {
        const [userRes, cartRes, addrRes] = await Promise.all([
          userApi.getUserInfo(),
          cartApi.getCart(),
          addressApi.getAddresses()
        ])
        if (userRes.success) {
          this.userPoints = userRes.data.points || 0
        }
        const cartItems = cartRes.success ? (cartRes.data.cartItems || []) : []
        this.selectedItems = cartItems.filter(i => i.isSelected === 1)
        if (cartItems.length === 0) {
          this.emptyHint = '购物车为空，请先选购商品'
          return
        }
        if (this.selectedItems.length === 0) {
          this.emptyHint = '未选择结算商品，请返回购物车勾选'
          return
        }
        for (const item of this.selectedItems) {
          const pr = await productApi.getProductById(item.productId)
          if (pr.success) {
            this.products[item.productId] = pr.data
          }
        }
        if (addrRes.success) {
          this.addresses = addrRes.data || []
          const def = this.addresses.find(a => a.isDefault === 1)
          if (def) this.selectedAddressId = def.id
          else if (this.addresses.length === 1) this.selectedAddressId = this.addresses[0].id
        }
      } catch (err) {
        console.error(err)
        this.emptyHint = '加载失败，请稍后重试'
      } finally {
        this.loading = false
      }
    },
```

- [ ] **Step 3: 添加 computed**

```javascript
  computed: {
    itemCount() {
      return this.selectedItems.reduce((s, i) => s + i.quantity, 0)
    },
    subtotalPoints() {
      return this.selectedItems.reduce((s, i) => {
        const p = this.products[i.productId]
        return s + (p ? p.pointsRequired * i.quantity : 0)
      }, 0)
    },
    pointsGap() {
      const gap = this.subtotalPoints - this.userPoints
      return gap > 0 ? gap : 0
    }
  },
```

- [ ] **Step 4: 验证加载**

浏览器：Cart 有选中项 → 点结算  
Expected: Network 出现 `/user/info`、`/cart`、`/addresses`、多条 `/products/{id}`

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/Checkout.vue
git commit -m "feat(checkout): load user, cart, addresses, products"
```

---

### Task 8: Checkout L2 模板与样式（~12 min）

**Files:**
- Modify: `frontend/src/views/Checkout.vue`

- [ ] **Step 1: 替换 template 为完整 L2 结构**

```vue
<template>
  <Layout>
    <div class="checkout-page">
      <div class="top-bar">
        <span>可用积分：<strong>{{ userPoints }}</strong></span>
      </div>
      <div class="steps-bar">
        <span class="step done">① 购物车</span>
        <span class="arrow">→</span>
        <span class="step active">② 确认订单</span>
        <span class="arrow">→</span>
        <span class="step">③ 完成</span>
      </div>

      <div v-if="errorBanner" class="error-banner">{{ errorBanner }}</div>

      <div v-if="loading" class="state">加载中...</div>
      <div v-else-if="emptyHint" class="state">
        {{ emptyHint }}
        <button class="link-btn" @click="$router.push('/cart')">返回购物车</button>
      </div>

      <div v-else class="layout-grid">
        <div class="main-col">
          <section ref="addressSection" class="card">
            <h3>收货地址</h3>
            <div v-if="addresses.length === 0" class="empty-addr">
              暂无收货地址，请先在个人中心添加地址
            </div>
            <div v-else class="address-list">
              <label
                v-for="addr in addresses"
                :key="addr.id"
                class="address-item"
                :class="{ selected: selectedAddressId === addr.id }"
              >
                <input type="radio" :value="addr.id" v-model="selectedAddressId" />
                <span v-if="addr.isDefault === 1" class="tag-default">默认</span>
                <div>{{ addr.receiver }} {{ addr.phone }}</div>
                <div class="addr-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                </div>
              </label>
            </div>
          </section>

          <section class="card">
            <h3>商品清单</h3>
            <div v-for="item in selectedItems" :key="item.id" class="product-row">
              <div class="thumb">{{ products[item.productId]?.name?.[0] || '?' }}</div>
              <div class="pinfo">
                <div>{{ products[item.productId]?.name || '商品' }}</div>
                <div>{{ products[item.productId]?.pointsRequired || 0 }} 积分 × {{ item.quantity }}</div>
                <div v-if="stockWarn(item)" class="stock-warn">库存不足，提交时以服务端校验为准</div>
              </div>
            </div>
          </section>

          <section class="card">
            <h3>订单备注</h3>
            <textarea v-model="remark" maxlength="100" placeholder="选填，100字以内" rows="3" />
          </section>
        </div>

        <aside class="sidebar card">
          <p>商品件数：{{ itemCount }}</p>
          <p>商品积分：{{ subtotalPoints }}</p>
          <p class="total">合计：<strong>{{ subtotalPoints }}</strong> 积分</p>
          <p>当前积分：{{ userPoints }}</p>
          <p v-if="pointsGap > 0" class="points-gap">还差 {{ pointsGap }} 积分</p>
          <button class="submit-btn" :disabled="submitting" @click="submitOrder">
            {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </aside>
      </div>

      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal">
          <h4>下单失败</h4>
          <p>{{ modalMessage }}</p>
          <button @click="showModal = false">知道了</button>
        </div>
      </div>
    </div>
  </Layout>
</template>
```

- [ ] **Step 2: 在 data 添加 UI 状态**

```javascript
      errorBanner: '',
      showModal: false,
      modalMessage: '',
      submitting: false,
```

- [ ] **Step 3: 添加 stockWarn 方法**

```javascript
    stockWarn(item) {
      const p = this.products[item.productId]
      return p && p.stock < item.quantity
    },
```

- [ ] **Step 4: 添加 scoped CSS（L2 token，参考 product/order.html）**

```css
.checkout-page { max-width: 860px; margin: 0 auto; padding: 16px; }
.top-bar { text-align: right; margin-bottom: 8px; }
.top-bar strong { color: #e4393c; font-size: 18px; }
.steps-bar { display: flex; gap: 8px; margin-bottom: 16px; font-size: 13px; }
.step.active { color: #e4393c; font-weight: 600; }
.step.done { color: #666; }
.layout-grid { display: flex; flex-direction: column; gap: 16px; }
@media (min-width: 861px) {
  .layout-grid { display: grid; grid-template-columns: 1fr 280px; }
}
.card { background: #fff; border-radius: 4px; box-shadow: 0 2px 12px rgba(0,0,0,.06); padding: 16px; margin-bottom: 12px; }
.address-item { display: block; border: 2px solid #e5e5e5; border-radius: 4px; padding: 12px; margin-bottom: 8px; cursor: pointer; }
.address-item.selected { border-color: #e4393c; background: #fff0f0; }
.tag-default { background: #e4393c; color: #fff; font-size: 12px; padding: 2px 6px; border-radius: 2px; margin-right: 8px; }
.product-row { display: flex; gap: 12px; padding: 8px 0; border-bottom: 1px solid #eee; }
.thumb { width: 48px; height: 48px; background: #f4f4f4; display: flex; align-items: center; justify-content: center; color: #999; }
.stock-warn { color: #faad14; font-size: 12px; }
.sidebar .total strong { color: #e4393c; font-size: 20px; }
.points-gap { color: #faad14; }
.submit-btn { width: 100%; margin-top: 12px; padding: 12px; background: #e4393c; color: #fff; border: none; border-radius: 4px; font-size: 16px; }
.error-banner { background: #fff0f0; color: #c81623; padding: 12px; margin-bottom: 12px; border: 1px solid #ffb4b4; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: #fff; padding: 24px; border-radius: 4px; min-width: 280px; }
.link-btn { margin-left: 8px; color: #e4393c; background: none; border: none; cursor: pointer; }
textarea { width: 100%; border: 1px solid #e5e5e5; border-radius: 4px; padding: 8px; }
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/Checkout.vue
git commit -m "feat(checkout): L2 layout blocks and styles"
```

---

### Task 9: Checkout 提交与错误双通道（~8 min）

**Files:**
- Modify: `frontend/src/views/Checkout.vue`

- [ ] **Step 1: 实现 submitOrder**

```javascript
    async submitOrder() {
      this.errorBanner = ''
      this.showModal = false
      if (!this.selectedAddressId) {
        this.errorBanner = '请选择收货地址后再提交订单'
        this.modalMessage = this.errorBanner
        this.showModal = true
        this.$refs.addressSection?.scrollIntoView({ behavior: 'smooth' })
        return
      }
      this.submitting = true
      try {
        const res = await orderApi.createOrder({
          addressId: this.selectedAddressId,
          remark: this.remark || undefined
        })
        if (res.success && res.data?.id) {
          this.$router.push('/orders/' + res.data.id)
        } else {
          const msg = res.message || '下单失败'
          this.errorBanner = msg
          this.modalMessage = msg
          this.showModal = true
        }
      } catch (err) {
        const msg = err.response?.data?.message || err.message || '下单失败'
        this.errorBanner = msg
        this.modalMessage = msg
        this.showModal = true
      } finally {
        this.submitting = false
      }
    }
```

- [ ] **Step 2: E2E 冒烟（~3 min）**

1. user1 登录 → 加购 → 勾选 → 结算 → 选地址 → 填备注 → 提交  
Expected: 跳转 `/orders/{id}`，status pending  
2. 积分故意不足（选贵商品）提交  
Expected: 页面同时出现 banner 与 modal

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/Checkout.vue
git commit -m "feat(checkout): submit order with banner and modal errors"
```

---

### Task 10: OrderDetail 展示 remark（~3 min）

**Files:**
- Modify: `frontend/src/views/OrderDetail.vue`

- [ ] **Step 1: 在 order-summary 内 receiver 行后添加**

```vue
          <p v-if="order.remark">备注：{{ order.remark }}</p>
          <p v-else>备注：无</p>
```

- [ ] **Step 2: 验证**

下单时填备注 → 详情页显示一致文本

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/OrderDetail.vue
git commit -m "feat(order): display remark on order detail"
```

---

### Task 11: 集成测试脚手架（~8 min）

**Files:**
- Create: `backend/src/test/resources/application-test.yml`
- Create: `backend/src/test/java/com/example/mall/support/TestDataHelper.java`
- Create: `backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java`（空壳）

- [ ] **Step 1: application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:sqlite:file:./target/test-mall.db?mode=memory&cache=shared
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false

jwt:
  secret: mall-secret-key-for-jwt-token-generation-minimum-256-bits-required-for-security
  expiration: 86400000
```

- [ ] **Step 2: TestDataHelper.java**

```java
package com.example.mall.support;

import com.example.mall.entity.*;
import com.example.mall.repository.*;
import com.example.mall.util.JwtUtil;
import org.springframework.stereotype.Component;

@Component
public class TestDataHelper {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final JwtUtil jwtUtil;

    public TestDataHelper(UserRepository userRepository, ProductRepository productRepository,
                          CartRepository cartRepository, AddressRepository addressRepository,
                          JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.jwtUtil = jwtUtil;
    }

    public User createUser(String username, int points) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pass");
        u.setPoints(points);
        u.setRole("user");
        u.setStatus("active");
        return userRepository.save(u);
    }

    public Product createProduct(String name, int points, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setPointsRequired(points);
        p.setStock(stock);
        p.setStatus("active");
        p.setSales(0);
        return productRepository.save(p);
    }

    public Cart addCartItem(Long userId, Long productId, int qty, int selected) {
        Cart c = new Cart();
        c.setUserId(userId);
        c.setProductId(productId);
        c.setQuantity(qty);
        c.setIsSelected(selected);
        return cartRepository.save(c);
    }

    public Address createAddress(Long userId, boolean isDefault) {
        Address a = new Address();
        a.setUserId(userId);
        a.setReceiver("张三");
        a.setPhone("13800000000");
        a.setProvince("北京市");
        a.setCity("北京市");
        a.setDistrict("朝阳区");
        a.setDetailAddress("测试路1号");
        a.setIsDefault(isDefault ? 1 : 0);
        return addressRepository.save(a);
    }

    public String bearerToken(User user) {
        return "Bearer " + jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }
}
```

- [ ] **Step 3: 测试类骨架**

```java
package com.example.mall;

import com.example.mall.support.TestDataHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderCheckoutIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;

    @Test
    void contextLoads() throws Exception {
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: 运行测试**

Run: `cd backend && mvn -q test -Dtest=OrderCheckoutIntegrationTest#contextLoads`  
Expected: Tests run: 1, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add backend/src/test
git commit -m "test: add integration test scaffold for order checkout"
```

---

### Task 12: T1 正常下单集成测试（~10 min）

**Files:**
- Modify: `backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java`

- [ ] **Step 1: 添加 T1 测试**

```java
import com.example.mall.entity.*;
import com.example.mall.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderCheckoutIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TestDataHelper helper;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartRepository cartRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired AddressRepository addressRepository;
    @Autowired ObjectMapper objectMapper;

    User user;
    Product product;
    Address address;
    String token;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        user = helper.createUser("t1user", 500);
        product = helper.createProduct("测试商品", 50, 10);
        helper.addCartItem(user.getId(), product.getId(), 2, 1);
        address = helper.createAddress(user.getId(), true);
        token = helper.bearerToken(user);
    }

    @Test
    void createOrder_success_pending() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("addressId", address.getId());
        body.put("remark", "测试备注");

        mockMvc.perform(post("/orders")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("pending"))
            .andExpect(jsonPath("$.data.totalPoints").value(100));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(400, refreshed.getPoints());
        Product refreshedProduct = productRepository.findById(product.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(8, refreshedProduct.getStock());
        org.junit.jupiter.api.Assertions.assertEquals(0, cartRepository.findByUserId(user.getId()).size());
    }
}
```

- [ ] **Step 2: 运行**

Run: `cd backend && mvn -q test -Dtest=OrderCheckoutIntegrationTest#createOrder_success_pending`  
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java
git commit -m "test: T1 happy path create order pending"
```

---

### Task 13: T2–T4 错误场景测试（~10 min）

**Files:**
- Modify: `OrderCheckoutIntegrationTest.java`

- [ ] **Step 1: test_noSelectedItems**

```java
    @Test
    void createOrder_noSelectedItems() throws Exception {
        cartRepository.deleteAll();
        helper.addCartItem(user.getId(), product.getId(), 1, 0);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("ORDER_001"))
            .andExpect(jsonPath("$.message", containsString("选中的商品")));
    }
```

- [ ] **Step 2: test_insufficientStock**

```java
    @Test
    void createOrder_insufficientStock() throws Exception {
        product.setStock(1);
        productRepository.save(product);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("ORDER_001"))
            .andExpect(jsonPath("$.message", containsString("库存不足")));
    }
```

- [ ] **Step 3: test_insufficientPoints**

```java
    @Test
    void createOrder_insufficientPoints() throws Exception {
        user.setPoints(10);
        userRepository.save(user);
        Map<String, Object> body = Map.of("addressId", address.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("ORDER_001"))
            .andExpect(jsonPath("$.message", containsString("积分不足")));
    }
```

- [ ] **Step 4: 运行**

Run: `cd backend && mvn -q test -Dtest=OrderCheckoutIntegrationTest`  
Expected: 4 tests pass

- [ ] **Step 5: Commit**

```bash
git add backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java
git commit -m "test: T2-T4 order rejection scenarios"
```

---

### Task 14: T5–T6 地址校验测试（~8 min）

**Files:**
- Modify: `OrderCheckoutIntegrationTest.java`

- [ ] **Step 1: test_missingAddressId**

```java
    @Test
    void createOrder_missingAddressId() throws Exception {
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("ORDER_001"))
            .andExpect(jsonPath("$.message").value("请选择收货地址"));
    }

    @Test
    void createOrder_emptyAddressIdString() throws Exception {
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content("{\"addressId\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("请选择收货地址"));
    }
```

- [ ] **Step 2: test_foreignAddress**

```java
    @Test
    void createOrder_foreignAddress() throws Exception {
        User other = helper.createUser("other", 100);
        Address foreign = helper.createAddress(other.getId(), true);
        Map<String, Object> body = Map.of("addressId", foreign.getId());
        mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("收货地址不存在"));
    }
```

- [ ] **Step 3: 运行全量**

Run: `cd backend && mvn -q test -Dtest=OrderCheckoutIntegrationTest`  
Expected: 全部 PASS

- [ ] **Step 4: Commit**

```bash
git add backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java
git commit -m "test: T5-T6 address validation scenarios"
```

---

### Task 15: T7–T8 取消订单测试（~8 min）

**Files:**
- Modify: `OrderCheckoutIntegrationTest.java`

- [ ] **Step 1: 添加 cancel 测试**

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

    @Test
    void cancelOrder_pending_success() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();

        mockMvc.perform(put("/orders/" + orderId + "/cancel").header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("cancelled"));

        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(500, refreshed.getPoints());
    }

    @Test
    void cancelOrder_rejectNonPending() throws Exception {
        Map<String, Object> body = Map.of("addressId", address.getId());
        String createJson = mockMvc.perform(post("/orders").header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(body)))
            .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createJson).path("data").path("id").asLong();
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus("shipped");
        orderRepository.save(order);

        mockMvc.perform(put("/orders/" + orderId + "/cancel").header("Authorization", token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("待处理")));
    }
```

- [ ] **Step 2: 运行**

Run: `cd backend && mvn -q test -Dtest=OrderCheckoutIntegrationTest`  
Expected: 全部 PASS（**不断言 balanceAfter**）

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/com/example/mall/OrderCheckoutIntegrationTest.java
git commit -m "test: T7-T8 cancel order scenarios"
```

---

### Task 16: 走查与 compass 更新（~10 min）

**Files:**
- Modify: `product/compass-order-checkout.md`

- [ ] **Step 1: 启服走查清单（AC-10/11/14/15）**

```bash
# 终端1
cd backend && mvn spring-boot:run
# 终端2
cd frontend && npm run dev
```

手工检查：
- [ ] 勾选 → 结算 → 选地址 → 备注 → 提交 → 详情 pending + remark
- [ ] 故意触发一次服务端错误 → banner **且** modal 同时出现
- [ ] 列表/详情 status 与 API 一致
- [ ] grep 无「积分已冻结」：`rg "积分已冻结" frontend/src`

- [ ] **Step 2: 更新 compass QG-07 等为 pass**

在 `product/compass-order-checkout.md` 记录 L3-4 完成项与 T9 defer。

- [ ] **Step 3: Commit**

```bash
git add product/compass-order-checkout.md
git commit -m "docs: update compass after order-checkout-hardening walkthrough"
```

---

## Self-Review

### Spec coverage

| Requirement | Task |
|-------------|------|
| cart-selection API | 1–3, 5 |
| addressId D9 | 4 |
| Checkout L2 UI | 6–9 |
| remark AC-13 | 10 |
| T1–T8 | 11–15 |
| walkthrough / compass | 16 |
| T9 并发 | Defer（spec 已标注） |
| §7 optional | 未纳入本 plan（by design） |

### Placeholder scan

无 TBD / implement later / 无代码测试步骤。

### Type consistency

- `isSelected` 前后端均为 `0|1` Integer
- `selectCartItem` API 路径 `/cart/{id}/select` 三处一致
- `parseAddressId` → `OrderService` null → 「请选择收货地址」

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-08-04-order-checkout-hardening.md`. Two execution options:

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
