package ru.yandex.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.service.CategoryService;
import ru.yandex.practicum.dto.CategoryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") Integer from,
                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение категорий: from:{}, size:{}", from, size);
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        log.info("Запрос на получение категории по id: {}", id);
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/map")
    public Map<Long, CategoryDto> getCategoryById(@RequestBody Set<Long> categoriesId) {
        log.info("Запрос на получение категорий по id: {}", categoriesId);
        return categoryService.getCategoryById(categoriesId);
    }
}