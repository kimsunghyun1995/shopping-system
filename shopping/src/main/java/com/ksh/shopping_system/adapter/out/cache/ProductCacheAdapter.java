package com.ksh.shopping_system.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.application.port.out.product.ProductCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCacheAdapter implements ProductCachePort {

	private final Cache<String, MinPriceCacheValue> cache;

	@Override
	public void putMinPrice(String categoryName, MinPriceCacheValue value) {
		cache.put(categoryName, value);
	}

	@Override
	public MinPriceCacheValue getMinPrice(String categoryName) {
		return cache.getIfPresent(categoryName);
	}

	@Override
	public void removeMinPrice(String categoryName) {
		cache.invalidate(categoryName);
	}

	@Override
	public void clearAll() {
		cache.invalidateAll();
	}

}
