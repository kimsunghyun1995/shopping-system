package com.ksh.shopping_system.common.type;

public class Price {
	private long price;

	public Price(long price) {
		if (price < 0) {
			throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
		}
		this.price = price;
	}

}
