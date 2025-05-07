package com.ksh.shopping_system.application.port.in.product;

import com.ksh.shopping_system.domain.Product;

import java.util.List;

public interface GetMinPriceByCategoryUseCase {
	List<Product> getMinPriceByCategory();
}
