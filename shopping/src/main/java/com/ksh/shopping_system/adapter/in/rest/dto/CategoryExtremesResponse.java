package com.ksh.shopping_system.adapter.in.rest.dto;

import com.ksh.shopping_system.common.response.BaseResponse;
import com.ksh.shopping_system.domain.CategoryExtremesResult;

public class CategoryExtremesResponse extends BaseResponse {

	private String category;
	private BrandPrice minPrice;
	private BrandPrice maxPrice;

	public CategoryExtremesResponse(String category, BrandPrice minPrice, BrandPrice maxPrice) {
		this.category = category;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
	}

	public static CategoryExtremesResponse of(CategoryExtremesResult categoryExtremes) {
		return new CategoryExtremesResponse(
				categoryExtremes.categoryName(),
				new CategoryExtremesResponse.BrandPrice(
						categoryExtremes.minBrandName(),
						categoryExtremes.minPrice()
				),
				new CategoryExtremesResponse.BrandPrice(
						categoryExtremes.maxBrandName(),
						categoryExtremes.maxPrice()
				)
		);
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
