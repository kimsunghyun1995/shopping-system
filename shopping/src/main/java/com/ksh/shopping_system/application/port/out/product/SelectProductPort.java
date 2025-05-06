package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.adapter.out.persistence.dto.CategoryMinPriceProjection;
import com.ksh.shopping_system.domain.Product;
import java.util.List;

public interface SelectProductPort {
	List<Product> findAllProducts();
	List<Product> findByCategoryName(String categoryName);
	Product findById(Long productId);
	List<CategoryMinPriceProjection> findCategoryMinPrice();
	Product findLowestPriceByCategory(String categoryName);
	List<Product> findByBrandName(String cheapestBrand);
	List<BrandSumProjection> findBrandSum();
}
