package com.ksh.shopping_system.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.application.port.out.product.ProductCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCacheAdapter implements ProductCachePort {

	private final Cache<String, MinPriceCacheValue> minPriceCache;
	private final Cache<String, MaxPriceCacheValue> maxPriceCache;

	@Override
	public void putMinPrice(String categoryName, MinPriceCacheValue value) {
		minPriceCache.put(categoryName, value);
	}

	@Override
	public MinPriceCacheValue getMinPrice(String categoryName) {
		return minPriceCache.getIfPresent(categoryName);
	}

	@Override
	public void removeMinPrice(String categoryName) {
		minPriceCache.invalidate(categoryName);
	}

	@Override
	public void putMaxPrice(String categoryName, MaxPriceCacheValue value) {
		maxPriceCache.put(categoryName, value);
	}

	@Override
	public MaxPriceCacheValue getMaxPrice(String categoryName) {
		return maxPriceCache.getIfPresent(categoryName);
	}

	@Override
	public void removeMaxPrice(String categoryName) {
		maxPriceCache.invalidate(categoryName);
	}

}
