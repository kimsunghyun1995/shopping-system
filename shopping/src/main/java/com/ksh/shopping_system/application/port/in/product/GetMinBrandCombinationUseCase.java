package com.ksh.shopping_system.application.port.in.product;

import com.ksh.shopping_system.application.service.ProductService.CheapestBrandResult;

public interface GetMinBrandCombinationUseCase {
	CheapestBrandResult getMinBrandCombination();
}
