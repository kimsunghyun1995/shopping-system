package com.ksh.shopping_system.domain;

import java.util.Objects;

public class Brand {
	private final String name;

	public Brand(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("브랜드 이름은 필수입니다.");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isSameBrand(Brand other) {
		return this.name.equalsIgnoreCase(other.name);
	}

}
