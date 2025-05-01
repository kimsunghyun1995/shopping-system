package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.mapper.CategoryMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.CategoryRepository;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter
		implements SelectCategoryPort {

	private final CategoryRepository categoryRepository;
	private final CategoryMapper categoryMapper;

	@Override
	public Optional<Category> findByName(String name) {
		return categoryRepository.findByName(name)
				.map(categoryMapper::toDomain);
	}

	@Override
	public Optional<Category> findById(Long categoryId) {
		return categoryRepository.findById(categoryId)
				.map(categoryMapper::toDomain);
	}

}
