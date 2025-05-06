package com.ksh.shopping_system.adapter.in.rest.controller;

import com.ksh.shopping_system.adapter.in.rest.dto.BrandMinCombinationResponse;
import com.ksh.shopping_system.adapter.in.rest.dto.CategoryExtremesResponse;
import com.ksh.shopping_system.adapter.in.rest.dto.MinPriceByCategoryResponse;
import com.ksh.shopping_system.application.port.in.brand.CreateBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.DeleteBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.UpdateBrandUseCase;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.common.response.BaseResponse;
import com.ksh.shopping_system.domain.CategoryExtremesResult;
import com.ksh.shopping_system.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	// Brand Use Cases
	private final CreateBrandUseCase createBrandUseCase;
	private final UpdateBrandUseCase updateBrandUseCase;
	private final DeleteBrandUseCase deleteBrandUseCase;

	// Product Use Cases
	private final CreateProductUseCase createProductUseCase;
	private final UpdateProductUseCase updateProductUseCase;
	private final DeleteProductUseCase deleteProductUseCase;

	private final GetMinPriceByCategoryUseCase getMinPriceByCategoryUseCase;
	private final GetMinBrandCombinationUseCase getMinBrandCombinationUseCase;
	private final GetCategoryExtremesUseCase getCategoryExtremesUseCase;

	/**
	 * 브랜드 생성 API
	 */
	@PostMapping("/brand")
	public BaseResponse createBrand(@RequestParam String brandName) {
		var createdBrand = createBrandUseCase.createBrand(brandName);
		return new BaseResponse("브랜드 생성 완료: " + createdBrand.getName());
	}

	/**
	 * 브랜드 수정 API
	 */
	@PutMapping("/brand/{brandId}")
	public BaseResponse updateBrand(@PathVariable Long brandId,
									@RequestParam String newName) {
		var updatedBrand = updateBrandUseCase.updateBrand(brandId, newName);
		return new BaseResponse("브랜드명 수정 완료: " + updatedBrand.getName());
	}

	/**
	 * 브랜드 삭제 API
	 */
	@DeleteMapping("/brand/{brandId}")
	public BaseResponse deleteBrand(@PathVariable Long brandId) {
		deleteBrandUseCase.deleteBrand(brandId);
		return new BaseResponse("브랜드 삭제 완료");
	}

	/**
	 * 상품 생성 API
	 */
	@PostMapping
	public BaseResponse createProduct(@RequestParam String brandName,
									  @RequestParam String categoryName,
									  @RequestParam long price) {
		var product = createProductUseCase.createProduct(brandName, categoryName, price);
		return new BaseResponse("상품 생성 완료: " + product.getBrand().getName()
				+ "/" + product.getCategory().getName() + "/" + product.getPriceValue());
	}

	/**
	 * 상품 수정(가격 변경) API
	 */
	@PutMapping("/{productId}")
	public BaseResponse updateProduct(@PathVariable Long productId,
									  @RequestParam long newPrice) {
		var updated = updateProductUseCase.updateProduct(productId, newPrice);
		return new BaseResponse("상품 가격 수정 완료: " + updated.getPriceValue());
	}

	/**
	 * 상품 삭제 API
	 */
	@DeleteMapping("/{productId}")
	public BaseResponse deleteProduct(@PathVariable Long productId) {
		deleteProductUseCase.deleteProduct(productId);
		return new BaseResponse("상품 삭제 완료");
	}

	/**
	 * 1. 카테고리별 최저가 상품 조회 API
	 */
	@GetMapping("/min-price-by-category")
	public List<MinPriceByCategoryResponse> getMinPriceByCategory() {
		List<Product> minProducts = getMinPriceByCategoryUseCase.getMinPriceByCategory();

		long total = 0;
		List<MinPriceByCategoryResponse> result = new ArrayList<>();
		for (Product p : minProducts) {
			result.add(new MinPriceByCategoryResponse(
					p.getCategory().getName(),
					p.getBrand().getName(),
					p.getPriceValue()
			));
			total += p.getPriceValue();
		}
		// "총액"
		result.add(new MinPriceByCategoryResponse("총액", "", total));
		return result;
	}

	/**
	 * 2. 단일 브랜드 기준 최저가 상품 조합 조회 API
	 */
	@GetMapping("/cheapest-brand")
	public BrandMinCombinationResponse getMinBrandCombination() {
		var domainResult = getMinBrandCombinationUseCase.getMinBrandCombination();
		var catPrices = domainResult.products().stream()
				.map(p -> new BrandMinCombinationResponse.CategoryPrice(
						p.getCategory().getName(),
						p.getPriceValue()
				))
				.toList();

		return new BrandMinCombinationResponse(
				domainResult.brandName(),
				catPrices,
				domainResult.totalPrice().getPrice()
		);
	}

	/**
	 * 3. 특정 카테고리 최저가/최고가 조회 API
	 */
	@GetMapping("/category-extremes")
	public CategoryExtremesResponse getCategoryExtremes(@RequestParam String categoryName) {
		CategoryExtremesResult categoryExtremes = getCategoryExtremesUseCase.getCategoryExtremes(categoryName);
		return CategoryExtremesResponse.of(categoryExtremes);
	}

}
