package com.ksh.shopping_system.adapter.in.rest.dto;

import com.ksh.shopping_system.common.response.BaseResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class BrandMinCombinationResponse extends BaseResponse {

	private String brand;
	private List<CategoryPrice> categoryPrices;
	private long totalPrice;

	public BrandMinCombinationResponse(String brand,
									   List<CategoryPrice> categoryPrices,
									   long totalPrice) {
		this.brand = brand;
		this.categoryPrices = categoryPrices;
		this.totalPrice = totalPrice;
	}

	@Getter
	public static class CategoryPrice {
		private String category;
		private long price;

		public CategoryPrice(String category, long price) {
			this.category = category;
			this.price = price;
		}
	}

}
