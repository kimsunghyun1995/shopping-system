package com.ksh.shopping_system.application.port.out.brand;

import java.util.Map;

public interface BrandCachePort {
	void putBrandTotal(String brandName, Long totalPrice);

	Long getBrandTotal(String brandName);

	void removeBrand(String brandName);

	void clearAll();

	/**
	 * 캐시에 저장된 (brandName -> totalPrice) 전체 Map
	 */
	Map<String, Long> asMap();
}
