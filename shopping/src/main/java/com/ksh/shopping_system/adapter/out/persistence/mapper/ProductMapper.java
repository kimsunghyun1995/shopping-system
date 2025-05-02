package com.ksh.shopping_system.adapter.out.persistence.mapper;


import com.ksh.shopping_system.adapter.out.persistence.entity.ProductEntity;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.Brand;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

	private final BrandMapper brandMapper;
	private final CategoryMapper categoryMapper;

	public Product toDomain(ProductEntity entity) {
		if (entity == null)
			return null;

		Brand Brand = brandMapper.toDomain(entity.getBrand());
		Category Category = categoryMapper.toDomain(entity.getCategory());
		Price price = new Price(entity.getPrice().longValue()); // BigDecimal -> long 변환 등 정책에 맞게

		return new Product(entity.getId(), Brand, Category, price);
	}

	public ProductEntity toEntity(Product domain) {
		if (domain == null)
			return null;

		var brandEntity = brandMapper.toEntity(domain.getBrand());
		var categoryEntity = categoryMapper.toEntity(domain.getCategory());
		return new ProductEntity(
				brandEntity,
				categoryEntity,
				domain.getPriceValue()
		);
	}
}
