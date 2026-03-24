describe('smoke', () => {
  beforeEach(() => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        success: true,
        data: {
          token: 'cypress-test-token',
          userId: 1,
          username: 'user1',
          userType: 'user',
        },
      },
    }).as('loginApi')
    cy.intercept('GET', '**/api/user/info', {
      statusCode: 200,
      body: { success: true, data: { points: 100 } },
    }).as('userInfo')
    cy.intercept('GET', '**/api/categories', {
      statusCode: 200,
      body: { success: true, data: [] },
    }).as('categories')
    cy.intercept('GET', '**/api/products**', {
      statusCode: 200,
      body: { success: true, data: [] },
    }).as('products')
    cy.intercept('GET', '**/api/orders**', {
      statusCode: 200,
      body: { success: true, data: [] },
    }).as('ordersApi')
  })

  it('登录后订单列表可见', () => {
    cy.visit('/login')
    cy.contains('用户登录').should('be.visible')
    cy.get('input[type="text"]').type('user1')
    cy.get('input[type="password"]').type('password1')
    cy.get('button.login-btn').click()
    cy.wait('@loginApi')
    cy.url().should('eq', `${Cypress.config('baseUrl')}/`)
    cy.visit('/orders')
    cy.wait('@ordersApi')
    cy.contains('我的订单').should('be.visible')
    cy.contains('暂无订单').should('be.visible')
  })
})
