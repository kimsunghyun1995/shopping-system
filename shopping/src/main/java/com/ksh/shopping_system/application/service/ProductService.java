package com.ksh.shopping_system.application.service;

import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.application.port.out.product.*;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService implements
		CreateProductUseCase,
		UpdateProductUseCase,
		DeleteProductUseCase,
		GetMinPriceByCategoryUseCase,
		GetMinBrandCombinationUseCase,
		GetCategoryExtremesUseCase {

	private final SelectProductPort selectProductPort;
	private final SaveProductPort saveProductPort;
	private final UpdateProductPort updateProductPort;
	private final DeleteProductPort deleteProductPort;
	private final ProductCachePort productCachePort;

	private final SelectBrandPort selectBrandPort;
	private final BrandCachePort brandCachePort;

	private final SelectCategoryPort selectCategoryPort;

	@Override
	@Transactional
	public Product createProduct(String brandName, String categoryName, long priceValue) {
		// Brand, Category를 조회
		Brand brand = selectBrandPort.findByName(brandName)
				.orElseThrow(() -> new IllegalArgumentException("브랜드 없음: " + brandName));

		Category category = selectCategoryPort.findByName(categoryName)
				.orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + categoryName));

		Product product = new Product(brand, category, new Price(priceValue));

		saveProductPort.saveProduct(product);

		//
		updateMinPriceCache(categoryName, product);
		updateMaxPriceCache(categoryName, product);

		// 브랜드 총합 캐시 업데이트
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		long newTotal = (oldTotal == null ? 0 : oldTotal) + priceValue;
		brandCachePort.putBrandTotal(brandName, newTotal);

		return product;

	}

	@Override
	@Transactional
	public Product updateProduct(Long productId, long newPriceValue) {
		Product oldProduct = selectProductPort.findById(productId);
		Product product = updateProductPort.updateProductPrice(productId, newPriceValue);
		String categoryName = product.getCategory().getName();
		String brandName = product.getBrand().getName();

		updateMinPriceCache(categoryName, product);
		updateMaxPriceCache(categoryName, product);

		// 브랜드 총합 캐시 가격 업데이트
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		if (oldTotal == null) {
			// todo: 익셉션 구체화
			throw new IllegalArgumentException("브랜드 없음: " + brandName);
		}
		long newTotal = oldTotal - oldProduct.getPriceValue() + newPriceValue;
		brandCachePort.putBrandTotal(brandName, newTotal);

		return product;
	}

	@Override
	@Transactional
	public void deleteProduct(Long productId) {
		Product product = selectProductPort.findById(productId);
		long oldPrice = product.getPriceValue();
		String brandName = product.getBrand().getName();

		deleteProductPort.deleteProduct(productId);

		String categoryName = product.getCategory().getName();

		MinPriceCacheValue minPrice = productCachePort.getMinPrice(categoryName);
		MaxPriceCacheValue maxPrice = productCachePort.getMaxPrice(categoryName);

		if (minPrice.getPrdouctId() == productId) {
			productCachePort.removeMinPrice(categoryName);
		}

		if (maxPrice.getPrdouctId() == productId) {
			productCachePort.removeMaxPrice(categoryName);
		}

		// 브랜드 총합 캐시 갱신
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		if (oldTotal == null)
			oldTotal = 0L;
		long newTotal = oldTotal - oldPrice;
		if (newTotal <= 0) {
			// 더 이상 상품이 없다면 remove
			brandCachePort.removeBrand(brandName);
		} else {
			brandCachePort.putBrandTotal(brandName, newTotal);
		}

	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> getMinPriceByCategory() {
		// 1) 카테고리 목록
		List<Category> categories = selectCategoryPort.findAllCategoryNames();

		// 2) 각 카테고리에 대해 캐시 조회
		List<Product> result = new ArrayList<>();
		for (Category category : categories) {
			String categoryName = category.getName();
			MinPriceCacheValue cached = productCachePort.getMinPrice(categoryName);
			if (cached != null) {
				// 캐시 존재하면
				Product product = buildProductFromCacheValue(cached, category); // 변환 로직
				result.add(product);
			} else {
				// 캐시에 없으면 DB조회(MIN 찾기) 후 put
				Product minProduct = selectProductPort.findLowestPriceByCategory(categoryName);
				result.add(minProduct);
				// put cache
				MinPriceCacheValue value = new MinPriceCacheValue(
						minProduct.getId(),
						minProduct.getBrand().getName(),
						minProduct.getPriceValue()
				);
				productCachePort.putMinPrice(categoryName, value);
			}
		}
		return result;
	}

	@Transactional(readOnly = true)
	public CheapestBrandResult getMinBrandCombination() {
		// 1) 캐시에서 모든 (brand, totalPrice) 조회
		Map<String, Long> brandMap = brandCachePort.asMap();

		if (brandMap.isEmpty()) {
			List<BrandSumProjection> sums = selectProductPort.findBrandSum();

			// 캐시에 put
			for (BrandSumProjection row : sums) {
				brandCachePort.putBrandTotal(row.getBrandName(), row.getTotalPrice());
			}
			// 다시 가져오기
			brandMap = brandCachePort.asMap();
		}

		// 2) 최소값 찾기
		String cheapestBrand = null;
		long minTotal = Long.MAX_VALUE;
		for (Map.Entry<String, Long> e : brandMap.entrySet()) {
			if (e.getValue() < minTotal) {
				minTotal = e.getValue();
				cheapestBrand = e.getKey();
			}
		}

		// 3) 가장 저렴한 브랜드의 상품 목록 DB 조회
		List<Product> products = selectProductPort.findByBrandName(cheapestBrand);

		// 4) 결과 객체 구성
		return new CheapestBrandResult(
				cheapestBrand,
				new Price(minTotal), // Price VO
				products
		);
	}


	private void updateMinPriceCache(String categoryName, Product product) {
		// 1) 현재 캐시값
		MinPriceCacheValue existing = productCachePort.getMinPrice(categoryName);
		// 2) 비교
		if (existing == null || product.getPriceValue() < existing.getPrice()) {
			productCachePort.putMinPrice(categoryName,
					new MinPriceCacheValue(
							product.getId(),
							product.getBrand().getName(),
							product.getPriceValue()
					)
			);
		}
	}

	private void updateMaxPriceCache(String categoryName, Product product) {
		// 1) 현재 캐시값
		MaxPriceCacheValue existing = productCachePort.getMaxPrice(categoryName);
		// 2) 비교
		if (existing == null || product.getPriceValue() > existing.getPrice()) {
			productCachePort.putMaxPrice(categoryName,
					new MaxPriceCacheValue(
							product.getId(),
							product.getBrand().getName(),
							product.getPriceValue()
					)
			);
		}
	}

	@Override
	public CategoryExtremesResult getCategoryExtremes(String categoryName) {
		MaxPriceCacheValue maxPrice = productCachePort.getMaxPrice(categoryName);
		MinPriceCacheValue minPrice = productCachePort.getMinPrice(categoryName);

		// 둘중의 하나라도 null 이면 문제발생
		if (maxPrice == null || minPrice == null) {
			// todo: 익셉션 값 추가
			throw new IllegalArgumentException();
		}

		return new CategoryExtremesResult(
				categoryName,
				minPrice.getBrandName(),
				minPrice.getPrice(),
				maxPrice.getBrandName(),
				maxPrice.getPrice());

	}

	// todo: mapper 로 빠질 수 있을 듯
	private Product buildProductFromCacheValue(MinPriceCacheValue cached, Category category) {
		// 캐시에 저장된 정보 -> Domain 객체
		Brand brand = new Brand(cached.getBrandName());
		Price price = new Price(cached.getPrice());
		return new Product(brand, category, price);
	}

}
