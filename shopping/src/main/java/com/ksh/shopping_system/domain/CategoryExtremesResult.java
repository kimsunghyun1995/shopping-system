package com.ksh.shopping_system.domain;

public record CategoryExtremesResult(
		String categoryName,
		String minBrandName,
		long minPrice,
		String maxBrandName,
		long maxPrice
) {
}