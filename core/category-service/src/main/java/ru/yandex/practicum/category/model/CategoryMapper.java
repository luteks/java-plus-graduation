package ru.yandex.practicum.category.model;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.dto.CategoryDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategoryByNew(NewCategoryDto newCategoryDto);
}