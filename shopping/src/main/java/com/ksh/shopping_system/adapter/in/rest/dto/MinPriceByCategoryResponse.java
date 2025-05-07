package com.ksh.shopping_system.adapter.in.rest.dto;

import com.ksh.shopping_system.common.response.BaseResponse;
import lombok.Getter;

@Getter
public class MinPriceByCategoryResponse extends BaseResponse {
	private String category;
	private String brand;
	private long price;

	public MinPriceByCategoryResponse(String category, String brand, long price) {
		this.category = category;
		this.brand = brand;
		this.price = price;
	}

}
