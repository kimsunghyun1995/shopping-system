package com.ksh.shopping_system.application.service;

import com.ksh.shopping_system.application.port.in.brand.CreateBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.DeleteBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.UpdateBrandUseCase;
import com.ksh.shopping_system.application.port.out.brand.DeleteBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SaveBrandPort;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.domain.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BrandService
		implements CreateBrandUseCase, UpdateBrandUseCase, DeleteBrandUseCase {

	private final SelectBrandPort selectBrandPort;
	private final SaveBrandPort saveBrandPort;
	private final DeleteBrandPort deleteBrandPort;

	@Override
	@Transactional
	public Brand createBrand(String brandName) {
		if (selectBrandPort.existsByName(brandName)) {
			throw new IllegalArgumentException("이미 존재하는 브랜드입니다: " + brandName);
		}
		Brand brand = new Brand(brandName);
		return saveBrandPort.saveBrand(brand);
	}

	@Override
	@Transactional
	public Brand updateBrand(Long brandId, String newName) {
		var brandOpt = selectBrandPort.findById(brandId);
		if (brandOpt.isEmpty()) {
			throw new NoSuchElementException("존재하지 않는 브랜드. ID=" + brandId);
		}
		// 삭제 후 새로 저장하는 예시
		deleteBrandPort.deleteBrand(brandId);

		Brand newBrand = new Brand(newName);
		return saveBrandPort.saveBrand(newBrand);
	}

	@Override
	@Transactional
	public void deleteBrand(Long brandId) {
		deleteBrandPort.deleteBrand(brandId);
	}

}
