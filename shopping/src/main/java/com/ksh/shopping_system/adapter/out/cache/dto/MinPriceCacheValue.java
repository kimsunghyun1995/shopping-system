package com.ksh.shopping_system.adapter.out.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MinPriceCacheValue {
	private long prdouctId;
	private String brandName;
	private long price;
}
