package com.example.mall.controller;

import com.example.mall.common.ApiResponse;
import com.example.mall.entity.Category;
import com.example.mall.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        List<Category> categories = categoryRepository.findByStatusAndDeletedAtIsNullOrderBySortAsc("active");
        return ResponseEntity.ok(ApiResponse.success("获取分类列表成功", categories));
    }
}
