package com.ksh.shopping_system.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class BrandEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	public void updateName(String newName) {
		this.name = newName;
	}

}
