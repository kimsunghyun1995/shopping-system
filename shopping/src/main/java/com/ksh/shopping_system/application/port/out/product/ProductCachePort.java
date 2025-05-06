package com.ksh.shopping_system.application.port.out.product;

import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
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
	 * 카데고리 최고가 put(key, value)
	 */
	void putMaxPrice(String categoryName, MaxPriceCacheValue value);

	/**
	 * 카테고리 최고가 get(key)
	 */
	MaxPriceCacheValue getMaxPrice(String categoryName);

	/**
	 * 카테고리 최고가 삭제
	 */
	void removeMaxPrice(String categoryName);

}
