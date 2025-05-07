package com.ksh.shopping_system.adapter.out.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BrandCacheAdapter implements BrandCachePort {

	private final Cache<String, Long> brandTotalPriceCache;

	@Override
	public void putBrandTotal(String brandName, Long totalPrice) {
		brandTotalPriceCache.put(brandName, totalPrice);
	}

	@Override
	public Long getBrandTotal(String brandName) {
		return brandTotalPriceCache.getIfPresent(brandName);
	}

	@Override
	public void removeBrand(String brandName) {
		brandTotalPriceCache.invalidate(brandName);
	}

	@Override
	public void clearAll() {
		brandTotalPriceCache.invalidateAll();
	}

	@Override
	public Map<String, Long> asMap() {
		// read-only map
		return Collections.unmodifiableMap(brandTotalPriceCache.asMap());
	}

}
