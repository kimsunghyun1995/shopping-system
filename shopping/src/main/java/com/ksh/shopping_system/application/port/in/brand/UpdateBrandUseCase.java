package com.ksh.shopping_system.application.port.in.brand;

import com.ksh.shopping_system.domain.Brand;

public interface UpdateBrandUseCase {
	Brand updateBrand(Long brandId, String newName);
}
