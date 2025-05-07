package com.ksh.shopping_system.application.service;

import com.ksh.shopping_system.adapter.out.persistence.dto.BrandSumProjection;
import com.ksh.shopping_system.application.event.ProductCreatedEvent;
import com.ksh.shopping_system.application.event.ProductDeletedEvent;
import com.ksh.shopping_system.application.event.ProductUpdatedEvent;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.application.port.out.brand.BrandCachePort;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.application.port.out.product.*;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.domain.CategoryExtremesResult;
import com.ksh.shopping_system.domain.CheapestBrandResult;
import com.ksh.shopping_system.domain.Product;
import com.ksh.shopping_system.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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

	private final BrandCachePort brandCachePort;

	private final SelectCategoryPort selectCategoryPort;

	private final ApplicationEventPublisher eventPublisher;

	@Override
	@Transactional
	public Product createProduct(String brandName, String categoryName, long priceValue) {
		Product product = saveProductPort.saveProduct(brandName, categoryName, priceValue);

		eventPublisher.publishEvent(new ProductCreatedEvent(this, product));

		return product;
	}

	@Override
	@Transactional
	public Product updateProduct(Long productId, long newPriceValue) {
		Product oldProduct = selectProductPort.findById(productId);
		Product product = updateProductPort.updateProductPrice(productId, newPriceValue);

		eventPublisher.publishEvent(new ProductUpdatedEvent(this, oldProduct, product));
		return product;
	}

	@Override
	@Transactional
	public void deleteProduct(Long productId) {
		Product product = selectProductPort.findById(productId);

		deleteProductPort.deleteProduct(productId);

		eventPublisher.publishEvent(new ProductDeletedEvent(this, product));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> getMinPriceByCategory() {
		// 카테고리 목록
		List<Category> categories = selectCategoryPort.findAllCategoryNames();

		// 각 카테고리에 대해 캐시 조회
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
		// 캐시에서 모든 (brand, totalPrice) 조회
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

		// minTotal 찾기
		long minTotal = brandMap.values().stream()
				.min(Long::compare)
				.orElseThrow(() -> new IllegalStateException("No brands found"));

		// minTotal을 가진 브랜드들 추출 -> 알파벳 순 정렬 -> 첫 번째
		List<String> tiedBrands = brandMap.entrySet().stream()
				.filter(e -> e.getValue() == minTotal)
				.map(Map.Entry::getKey)
				.sorted() // 알파벳 순
				.toList();

		// 알파벳 순으로 첫 브랜드
		String cheapestBrand = tiedBrands.get(0);

		// 해당 브랜드 상품 목록 DB 조회
		List<Product> products = selectProductPort.findByBrandName(cheapestBrand);

		// 결과 객체 구성
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

			// 캐시에 put
			productCachePort.putMinPrice(categoryName, dbMinProduct);
			productCachePort.putMaxPrice(categoryName, dbMaxProduct);

			// 결과 객체 반환
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

}
