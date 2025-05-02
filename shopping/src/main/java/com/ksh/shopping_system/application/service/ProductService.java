package com.ksh.shopping_system.application.service;

import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.application.port.out.brand.SelectBrandPort;
import com.ksh.shopping_system.application.port.out.category.SelectCategoryPort;
import com.ksh.shopping_system.application.port.out.product.*;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.Brand;
import com.ksh.shopping_system.domain.Category;
import com.ksh.shopping_system.domain.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

		updateMinPriceCache(categoryName, product);

		return product;

	}

	@Override
	@Transactional
	public Product updateProduct(Long productId, long newPriceValue) {
		Product product = updateProductPort.updateProductPrice(productId, newPriceValue);
		String categoryName = product.getCategory().getName();

		updateMinPriceCache(categoryName, product);

		return product;
	}

	@Override
	@Transactional
	public void deleteProduct(Long productId) {
		Product product = selectProductPort.findById(productId);
		deleteProductPort.deleteProduct(productId);

		String categoryName = product.getCategory().getName();

		MinPriceCacheValue minPrice = productCachePort.getMinPrice(categoryName);
		if (minPrice.getPrdouctId() == productId) {
			productCachePort.removeMinPrice(categoryName);
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

	@Override
	@Transactional(readOnly = true)
	public CheapestBrandResult getMinBrandCombination() {
		List<Product> allProducts = selectProductPort.findAllProducts();

		Map<String, List<Product>> brandMap =
				allProducts.stream().collect(Collectors.groupingBy(p -> p.getBrand().getName()));

		String cheapestBrand = null;
		long minTotal = Long.MAX_VALUE;
		List<Product> cheapestList = new ArrayList<>();

		for (var entry : brandMap.entrySet()) {
			String brandName = entry.getKey();
			List<Product> products = entry.getValue();

			long sum = 0;
			for (Product p : products) {
				sum += p.getPriceValue();
			}
			if (sum < minTotal) {
				minTotal = sum;
				cheapestBrand = brandName;
				cheapestList = products;
			}
		}

		return new CheapestBrandResult(cheapestBrand, new Price(minTotal), cheapestList);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<CategoryExtremesResult> getCategoryExtremes(String categoryName) {
		List<Product> products = selectProductPort.findByCategoryName(categoryName);
		if (products.isEmpty()) {
			return Optional.empty();
		}
		Product minP = Collections.min(products, Comparator.comparingLong(Product::getPriceValue));
		Product maxP = Collections.max(products, Comparator.comparingLong(Product::getPriceValue));

		CategoryExtremesResult result = new CategoryExtremesResult(
				categoryName,
				minP.getBrand().getName(),
				minP.getPriceValue(),
				maxP.getBrand().getName(),
				maxP.getPriceValue()
		);
		return Optional.of(result);
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

	private Product buildProductFromCacheValue(MinPriceCacheValue cached, Category category) {
		// 캐시에 저장된 정보 -> Domain 객체
		Brand brand = new Brand(cached.getBrandName());
		Price price = new Price(cached.getPrice());
		return new Product(brand, category, price);
	}

	/* ===================== 내부 결과 객체 ===================== */

	@Getter
	public static class CheapestBrandResult {
		private final String brandName;
		private final Price totalPrice;
		private final List<Product> products;

		public CheapestBrandResult(String brandName, Price totalPrice, List<Product> products) {
			this.brandName = brandName;
			this.totalPrice = totalPrice;
			this.products = products;
		}
	}

	@Getter
	public static class CategoryExtremesResult {
		private final String categoryName;
		private final String minBrandName;
		private final long minPrice;
		private final String maxBrandName;
		private final long maxPrice;

		public CategoryExtremesResult(String categoryName,
									  String minBrandName,
									  long minPrice,
									  String maxBrandName,
									  long maxPrice) {
			this.categoryName = categoryName;
			this.minBrandName = minBrandName;
			this.minPrice = minPrice;
			this.maxBrandName = maxBrandName;
			this.maxPrice = maxPrice;
		}
	}

}
