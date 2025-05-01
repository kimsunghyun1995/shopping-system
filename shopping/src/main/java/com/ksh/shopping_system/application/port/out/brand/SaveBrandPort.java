package com.ksh.shopping_system.application.port.out.brand;

import com.ksh.shopping_system.domain.Brand;

public interface SaveBrandPort {
	Brand saveBrand(Brand brandDomain);
}
