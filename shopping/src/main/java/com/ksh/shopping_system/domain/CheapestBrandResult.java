package com.ksh.shopping_system.domain;

import com.ksh.shopping_system.common.type.Price;

import java.util.List;

public record CheapestBrandResult(
		String brandName,
		Price totalPrice,
		List<Product> products
) {
}