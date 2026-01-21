package ru.yandex.practicum.category.service;


import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.dto.CategoryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getCategoryById(Long id);

    Map<Long, CategoryDto> getCategoryById(Set<Long> categoriesId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);
}