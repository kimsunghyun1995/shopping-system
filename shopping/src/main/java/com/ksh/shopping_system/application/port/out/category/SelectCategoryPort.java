package com.ksh.shopping_system.application.port.out.category;

import com.ksh.shopping_system.domain.Category;

import java.util.List;
import java.util.Optional;

public interface SelectCategoryPort {
	Optional<Category> findByName(String name);

	Optional<Category> findById(Long categoryId);

	List<Category> findAllCategoryNames();

}

