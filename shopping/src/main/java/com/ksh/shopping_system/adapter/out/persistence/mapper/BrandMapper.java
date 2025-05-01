package com.ksh.shopping_system.adapter.out.persistence.mapper;

import com.ksh.shopping_system.adapter.out.persistence.entity.BrandEntity;
import com.ksh.shopping_system.domain.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

	public Brand toDomain(BrandEntity entity) {
		if (entity == null) return null;
		return new Brand(entity.getName());
	}

	public BrandEntity toEntity(Brand domain) {
		if (domain == null) return null;
		return new BrandEntity(domain.getName());
	}
}
