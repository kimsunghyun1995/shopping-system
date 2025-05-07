package com.ksh.shopping_system.adapter.out.persistence.mapper;

import com.ksh.shopping_system.adapter.out.persistence.entity.CategoryEntity;
import com.ksh.shopping_system.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

	public Category toDomain(CategoryEntity entity) {
		if (entity == null) return null;
		return new Category(entity.getName());
	}

	public CategoryEntity toEntity(Category domain) {
		if (domain == null) return null;
		return new CategoryEntity(domain.getName());
	}
}
