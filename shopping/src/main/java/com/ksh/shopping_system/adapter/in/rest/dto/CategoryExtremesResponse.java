package com.ksh.shopping_system.adapter.in.rest.dto;

import com.ksh.shopping_system.common.response.BaseResponse;

public class CategoryExtremesResponse extends BaseResponse {

	private String category;
	private BrandPrice minPrice;
	private BrandPrice maxPrice;

	public CategoryExtremesResponse(String category, BrandPrice minPrice, BrandPrice maxPrice) {
		this.category = category;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
	}

	public static class BrandPrice {
		private String brand;
		private long price;

		public BrandPrice(String brand, long price) {
			this.brand = brand;
			this.price = price;
		}
	}

}
