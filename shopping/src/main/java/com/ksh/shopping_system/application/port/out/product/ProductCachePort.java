package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;

public interface ProductCachePort {
	/**
	 * 카테고리 최저가 put(key, value)
	 */
	void putMinPrice(String categoryName, MinPriceCacheValue value);

	/**
	 * 카테고리 최저가 get(key)
	 */
	MinPriceCacheValue getMinPrice(String categoryName);

	/**
	 * 카테고리 최저가 삭제
	 */
	void removeMinPrice(String categoryName);

	/**
	 * 전체 캐시 Clear
	 */
	void clearAll();

}
