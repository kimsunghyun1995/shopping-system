package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.domain.Product;

public interface SaveProductPort {
	Product saveProduct(Product productDomain);
	Product saveProduct(String brandName, String categoryName, long priceValue);
}
