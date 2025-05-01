package com.ksh.shopping_system.domain;

import lombok.EqualsAndHashCode;

public class Category {
	private final String name;

	public Category(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
