package com.ksh.shopping_system.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.adapter.out.persistence.mapper.ProductMapper;
import com.ksh.shopping_system.application.port.out.product.ProductCachePort;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCacheAdapter implements ProductCachePort {

	private final Cache<String, MinPriceCacheValue> minPriceCache;
	private final Cache<String, MaxPriceCacheValue> maxPriceCache;
	private final ProductMapper productMapper;

	@Override
	public void putMinPrice(String categoryName, MinPriceCacheValue value) {
		minPriceCache.put(categoryName, value);
	}

	@Override
	public Product getMinPrice(Category category) {
		MinPriceCacheValue minPriceCacheValue = minPriceCache.getIfPresent(category.getName());
		return productMapper.minPriceCacheToDomain(minPriceCacheValue, category);
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
	public Product getMaxPrice(Category category) {
		MaxPriceCacheValue maxPriceCacheValue = maxPriceCache.getIfPresent(category.getName());
		return productMapper.maxPriceCacheToDomain(maxPriceCacheValue, category);
	}

	@Override
	public void removeMaxPrice(String categoryName) {
		maxPriceCache.invalidate(categoryName);
	}

}
