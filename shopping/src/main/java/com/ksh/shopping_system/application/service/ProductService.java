package com.ksh.shopping_system.application.service;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.application.port.out.product.*;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.*;
import com.ksh.shopping_system.exception.DataNotFoundException;
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
		Category category = selectCategoryPort.findByName(categoryName);
		Product product = saveProductPort.saveProduct(brandName, categoryName, priceValue);

		updateMinPriceCache(category, product);
		updateMaxPriceCache(category, product);

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
		String brandName = product.getBrand().getName();

		updateMinPriceCache(product.getCategory(), product);
		updateMaxPriceCache(product.getCategory(), product);

		// 브랜드 총합 캐시 가격 업데이트
		Long oldTotal = brandCachePort.getBrandTotal(brandName);
		if (oldTotal == null) {
			throw new DataNotFoundException(
					ErrorCode.BRAND_CACHE_NOT_FOUND,
					"브랜드 캐시에 해당 브랜드가 없습니다: " + brandName
			);
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

		Product minPriceProduct = productCachePort.getMinPrice(product.getCategory());
		Product maxPriceProduct = productCachePort.getMaxPrice(product.getCategory());

		if (minPriceProduct.getId() == productId) {
			productCachePort.removeMinPrice(categoryName);
		}

		if (maxPriceProduct.getId() == productId) {
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
			Product minPriceProduct = productCachePort.getMinPrice(category);
			if (minPriceProduct != null) {
				result.add(minPriceProduct);
			} else {
				// 캐시에 없으면 DB조회(MIN 찾기) 후 put
				Product minProduct = selectProductPort.findLowestPriceByCategory(categoryName);
				result.add(minProduct);
				// put cache
				productCachePort.putMinPrice(categoryName, minProduct);
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

	@Override
	public CategoryExtremesResult getCategoryExtremes(String categoryName) {
		Category category = selectCategoryPort.findByName(categoryName);

		Product maxPriceProduct = productCachePort.getMaxPrice(category);
		Product minPriceProduct = productCachePort.getMinPrice(category);

		// 캐시 값이 없다면 DB fallback
		if (maxPriceProduct == null || minPriceProduct == null) {
			// DB에서 최저/최고가 조회
			Product dbMinProduct = selectProductPort.findLowestPriceByCategory(categoryName);
			Product dbMaxProduct = selectProductPort.findHighestPriceByCategory(categoryName);

			if (dbMinProduct == null || dbMaxProduct == null) {
				// DB에서도 상품이 없으면 -> 카테고리에 상품이 없는 상황
				throw new DataNotFoundException(
						ErrorCode.PRODUCT_NOT_FOUND,
						"No products found for category: " + categoryName
				);
			}

			// 3-2) 캐시에 put
			productCachePort.putMinPrice(categoryName, dbMinProduct);
			productCachePort.putMaxPrice(categoryName, dbMaxProduct);

			// 3-3) 결과 객체 반환
			return new CategoryExtremesResult(
					categoryName,
					dbMinProduct.getBrand().getName(), dbMinProduct.getPriceValue(),
					dbMaxProduct.getBrand().getName(), dbMaxProduct.getPriceValue()
			);
		}

		return new CategoryExtremesResult(
				categoryName,
				minPriceProduct.getBrand().getName(),
				minPriceProduct.getPriceValue(),
				maxPriceProduct.getBrand().getName(),
				maxPriceProduct.getPriceValue());
	}

	private void updateMinPriceCache(Category category, Product product) {
		// 현재 캐시값
		Product minPriceProduct = productCachePort.getMinPrice(category);
		// 비교
		if (minPriceProduct == null || product.getId() == minPriceProduct.getId() || product.getPriceValue() < minPriceProduct.getPriceValue()) {
			productCachePort.putMinPrice(category.getName(), product);
		}
	}

	private void updateMaxPriceCache(Category category, Product product) {
		// 현재 캐시값
		Product maxPriceProduct = productCachePort.getMaxPrice(category);
		// 비교
		if (maxPriceProduct == null || product.getId() == maxPriceProduct.getId() || product.getPriceValue() > maxPriceProduct.getPriceValue()) {
			productCachePort.putMaxPrice(category.getName(), product);
		}
	}

}
