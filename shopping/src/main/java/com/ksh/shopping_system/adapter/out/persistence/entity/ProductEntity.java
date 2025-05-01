package com.ksh.shopping_system.adapter.out.persistence.entity;


import jakarta.persistence.*;

/**
 * 상품 엔티티
 * - Brand, Category를 참조하는 다대일(ManyToOne) 관계
 */
@Entity
@Table(name = "products")
public class ProductEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "brand_id")
	private BrandEntity brand;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id")
	private CategoryEntity category;

	@Column(nullable = false)
	private Long price;

	public void changePrice(Long newPrice) {
		this.price = newPrice;
	}

}