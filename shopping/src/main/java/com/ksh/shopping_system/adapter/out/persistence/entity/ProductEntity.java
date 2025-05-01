package com.ksh.shopping_system.adapter.out.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 엔티티
 * - Brand, Category를 참조하는 다대일(ManyToOne) 관계
 */
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
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

	public ProductEntity(BrandEntity brand, CategoryEntity category, Long price) {
		this.brand = brand;
		this.category = category;
		this.price = price;
	}

	public void changePrice(Long newPrice) {
		this.price = newPrice;
	}

	public void update(ProductEntity oldEntity) {
	}


}