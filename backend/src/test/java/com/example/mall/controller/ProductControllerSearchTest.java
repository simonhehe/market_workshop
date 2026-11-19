package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Product;
import com.example.mall.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductControllerSearchTest {

    private final ProductService productService = mock(ProductService.class);
    private final ProductController controller = new ProductController(productService);

    @Test
    void combinesCategoryAndKeywordThroughSearchProducts() {
        Product iphone = product(1L, "iPhone 15 Pro");
        when(productService.searchProducts("iPhone", 10L, null)).thenReturn(List.of(iphone));

        ResponseEntity<ApiResponse<List<Product>>> response =
                controller.getProducts(10L, "  iPhone  ", null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).extracting(Product::getId).containsExactly(1L);
        verify(productService).searchProducts("iPhone", 10L, null);
        verify(productService, never()).getProductsByCategory(anyLong());
    }

    @Test
    void categoryOnlyUsesSearchProductsWithNullKeyword() {
        Product mouse = product(2L, "无线鼠标");
        when(productService.searchProducts(null, 10L, null)).thenReturn(List.of(mouse));

        ResponseEntity<ApiResponse<List<Product>>> response =
                controller.getProducts(10L, null, null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).extracting(Product::getId).containsExactly(2L);
        verify(productService).searchProducts(null, 10L, null);
        verify(productService, never()).getProductsByCategory(anyLong());
    }

    @Test
    void blankKeywordIsTreatedAsNoFilter() {
        when(productService.searchProducts(null, null, null)).thenReturn(List.of());

        controller.getProducts(null, "   ", null);

        verify(productService).searchProducts(null, null, null);
    }

    @Test
    void keywordOnlySearchIsTrimmed() {
        when(productService.searchProducts("耳机", null, "sales")).thenReturn(List.of());

        controller.getProducts(null, " 耳机 ", "sales");

        verify(productService).searchProducts("耳机", null, "sales");
    }

    @Test
    void noMatchReturnsEmptyListWithSuccessTrue() {
        when(productService.searchProducts("不存在", 10L, null)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<Product>>> response =
                controller.getProducts(10L, "不存在", null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).isNotNull().isEmpty();
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }
}