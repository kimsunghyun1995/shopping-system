package com.ksh.shopping_system.application.port.in.product;

import com.ksh.shopping_system.application.service.ProductService.CategoryExtremesResult;

import java.util.Optional;

public interface GetCategoryExtremesUseCase {
	Optional<CategoryExtremesResult> getCategoryExtremes(String categoryName);
}
