package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.entity.CategoryEntity;
import com.ksh.shopping_system.adapter.out.persistence.mapper.CategoryMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.CategoryRepository;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter
		implements SelectCategoryPort {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Override
	public Category findByName(String name) {
		CategoryEntity categoryEntity = categoryRepository.findByName(name)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.CATEGORY_NOT_FOUND,
						"category not found: " + name
				));
		return categoryMapper.toDomain(categoryEntity);
	}

	@Override
	public Category findById(Long categoryId) {
		CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.CATEGORY_NOT_FOUND,
						"category not found: ID=" + categoryId
				));
		return categoryMapper.toDomain(categoryEntity);
	}

	@Override
	public List<Category> findAllCategoryNames() {
		List<CategoryEntity> all = categoryRepository.findAll();
		return all.stream().map(categoryMapper::toDomain).toList();
	}

}
