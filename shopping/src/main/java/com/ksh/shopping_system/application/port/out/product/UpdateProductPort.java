package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.domain.Product;

public interface UpdateProductPort {
	Product updateProduct(Product productDomain);
	Product updateProductPrice(Long productId, Long price);
}
