package com.ksh.shopping_system.application.port.in.product;

import com.ksh.shopping_system.domain.Product;

public interface CreateProductUseCase {
	Product createProduct(String brandName, String categoryName, long priceValue);
}
