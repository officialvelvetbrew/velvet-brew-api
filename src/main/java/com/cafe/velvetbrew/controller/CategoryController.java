package com.cafe.velvetbrew.controller;

import com.cafe.velvetbrew.dto.ApiResponse;
import com.cafe.velvetbrew.dto.CategoryResponse;
import com.cafe.velvetbrew.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {

        return ApiResponse.success(categoryService.getAll());

    }
}
