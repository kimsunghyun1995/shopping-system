package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.domain.Product;
import java.util.List;
import java.util.Optional;

public interface SelectProductPort {
	List<Product> findAllProducts();
	List<Product> findByCategoryName(String categoryName);
	List<Product> findByBrandName(String brandName);
	Optional<Product> findById(Long productId);
}
