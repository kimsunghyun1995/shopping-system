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

	/**
	 * 최고가 비교를 위한 메서드
	 * 가격이 같은 경우 알파벳 순으로 뒤에 오는 브랜드가 우선됨
	 */
	public boolean isPreferredForMaxPrice(Product other) {
		if (this.getPriceValue() != other.getPriceValue()) {
			return this.getPriceValue() > other.getPriceValue();
		}
		// 가격이 같으면 알파벳 순으로 뒤에 오는 브랜드가 우선
		return this.getBrand().getName().compareTo(other.getBrand().getName()) > 0;
	}

	/**
	 * 최저가 비교를 위한 메서드
	 * 가격이 같은 경우 알파벳 순으로 앞에 오는 브랜드가 우선됨
	 */
	public boolean isPreferredForMinPrice(Product other) {
		if (this.getPriceValue() != other.getPriceValue()) {
			return this.getPriceValue() < other.getPriceValue();
		}
		// 가격이 같으면 알파벳 순으로 앞에 오는 브랜드가 우선
		return this.getBrand().getName().compareTo(other.getBrand().getName()) < 0;
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
