package com.ksh.shopping_system.domain;

import com.ksh.shopping_system.common.type.Title;

public class Category {
	private final Title name;

	public Category(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
		}
		this.name = new Title(name);
	}

	public String getName() {
		return name.getTitle();
	}

}
