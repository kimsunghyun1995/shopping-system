package com.ksh.shopping_system.application.port.out.brand;

import com.ksh.shopping_system.domain.Brand;

public interface SelectBrandPort {
	Brand findByName(String name);

	Brand findById(Long brandId);

	boolean existsByName(String name);
}

