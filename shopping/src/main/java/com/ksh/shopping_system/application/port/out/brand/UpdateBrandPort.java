package com.ksh.shopping_system.application.port.out.brand;

import com.ksh.shopping_system.domain.Brand;

public interface UpdateBrandPort {
	Brand updateBrand(Long brandId, String brandName);
}

