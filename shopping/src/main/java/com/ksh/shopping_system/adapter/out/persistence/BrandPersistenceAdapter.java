package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.entity.BrandEntity;
import com.ksh.shopping_system.adapter.out.persistence.mapper.BrandMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.BrandRepository;
import com.ksh.shopping_system.application.port.out.brand.DeleteBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SaveBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.domain.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandPersistenceAdapter
		implements SelectBrandPort, SaveBrandPort, DeleteBrandPort {

	private final BrandRepository brandRepository;
	private final BrandMapper brandMapper;

	@Override
	public Brand findByName(String name) {
		BrandEntity brandEntity = brandRepository.findByName(name)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

		return brandMapper.toDomain(brandEntity);
	}

	@Override
	public Brand findById(Long brandId) {
		BrandEntity brandEntity = brandRepository.findById(brandId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));

		return brandMapper.toDomain(brandEntity);
	}

	@Override
	public boolean existsByName(String name) {
		return brandRepository.findByName(name).isPresent();
	}

	@Override
	public Brand saveBrand(Brand Brand) {
		var brandEntity = brandMapper.toEntity(Brand);
		var savedEntity = brandRepository.save(brandEntity);
		return brandMapper.toDomain(savedEntity);
	}

	@Override
	public void deleteBrand(Long brandId) {
		var brandEntity = brandRepository.findById(brandId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));
		brandRepository.delete(brandEntity);
	}
}
