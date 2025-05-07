package com.ksh.shopping_system.domain;

import com.ksh.shopping_system.common.type.Price;
import lombok.Getter;


@Getter
public class Product {
	private long id;
	private Brand brand;
	private Category category;
	private Price price;

	public Product(long id, Brand brand, Category category, Price price) {
		if (brand == null || category == null) {
			throw new IllegalArgumentException("브랜드와 카테고리는 필수입니다.");
		}
		this.id = id;
		this.brand = brand;
		this.category = category;
		this.price = price;
	}

	public Product(Brand brand, Category category, Price price) {
		if (brand == null || category == null) {
			throw new IllegalArgumentException("브랜드와 카테고리는 필수입니다.");
		}
		this.brand = brand;
		this.category = category;
		this.price = price;
	}

	public void changePrice(Price newPrice) {
		this.price = newPrice;
	}

	public long getPriceValue() {
		return price.getPrice();
	}

	public boolean isSameProduct(long productId) {
		return this.id == productId;
	}

}
