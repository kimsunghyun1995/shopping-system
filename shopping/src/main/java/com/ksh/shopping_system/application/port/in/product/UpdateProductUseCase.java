package com.ksh.shopping_system.application.port.in.product;

import com.ksh.shopping_system.domain.Product;

public interface UpdateProductUseCase {
	Product updateProduct(Long productId, long newPriceValue);
}
