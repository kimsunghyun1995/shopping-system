package com.ksh.shopping_system.adapter.out.persistence;

import com.ksh.shopping_system.adapter.out.persistence.entity.BrandEntity;
import com.ksh.shopping_system.adapter.out.persistence.mapper.BrandMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.BrandRepository;
import com.ksh.shopping_system.application.port.out.brand.DeleteBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SaveBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.application.port.out.brand.UpdateBrandPort;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.domain.Brand;
import com.ksh.shopping_system.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandPersistenceAdapter
		implements SelectBrandPort, SaveBrandPort, DeleteBrandPort, UpdateBrandPort {

	private final BrandRepository brandRepository;
	private final BrandMapper brandMapper;

	@Override
	public Brand findByName(String name) {
		BrandEntity brandEntity = brandRepository.findByName(name)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.BRAND_NOT_FOUND,
						"brand not found: " + name
				));
		return brandMapper.toDomain(brandEntity);
	}

	@Override
	public Brand findById(Long brandId) {
		BrandEntity brandEntity = brandRepository.findById(brandId)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.BRAND_NOT_FOUND,
						"brand not found ID=" + brandId
				));
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
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.BRAND_NOT_FOUND,
						"brand not found ID=" + brandId
				));
		brandRepository.delete(brandEntity);
	}

	@Override
	public Brand updateBrand(Long brandId, String newBrandName) {
		var brandEntity = brandRepository.findById(brandId)
				.orElseThrow(() -> new DataNotFoundException(
						ErrorCode.BRAND_NOT_FOUND,
						"brand not found ID=" + brandId
				));

		brandEntity.updateName(newBrandName);
		return brandMapper.toDomain(brandEntity);
	}

}
