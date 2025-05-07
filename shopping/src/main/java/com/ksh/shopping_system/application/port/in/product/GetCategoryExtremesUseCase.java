package com.ksh.shopping_system.application.port.in.product;


import com.ksh.shopping_system.domain.CategoryExtremesResult;

public interface GetCategoryExtremesUseCase {
	CategoryExtremesResult getCategoryExtremes(String categoryName);
}
