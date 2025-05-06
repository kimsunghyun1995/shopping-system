package com.ksh.shopping_system.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

	/**
	 *   MinPriceCacheValue = {categoryName, brandName, price}
	 */
	@Bean
	public Cache<String, MinPriceCacheValue> minPriceByCategoryCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(10, TimeUnit.MINUTES)
				// 브랜드 개수 최대 100_000
				.maximumSize(100_000)
				.build();
	}

	/**
	 *   MaxPriceCacheValue = {categoryName, brandName, price}
	 */
	@Bean
	public Cache<String, MaxPriceCacheValue> maxPriceByCategoryCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(100_000)
				.build();
	}

	@Bean
	public Cache<String, Long> brandTotalPriceCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(50_000)
				.build();
	}

}
