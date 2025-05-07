package com.ksh.shopping_system.application.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.ksh.shopping_system.adapter.out.cache.dto.MaxPriceCacheValue;
import com.ksh.shopping_system.adapter.out.cache.dto.MinPriceCacheValue;
import com.ksh.shopping_system.adapter.out.persistence.entity.CategoryEntity;
import com.ksh.shopping_system.adapter.out.persistence.repository.BrandRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.CategoryRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.ProductRepository;
import com.ksh.shopping_system.application.port.in.brand.CreateBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.DeleteBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.UpdateBrandUseCase;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.domain.Brand;
import com.ksh.shopping_system.domain.CategoryExtremesResult;
import com.ksh.shopping_system.domain.Product;
import com.ksh.shopping_system.exception.DataNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * productService의 통합 테스트
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductServiceTest {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private CreateBrandUseCase createBrandUseCase;
	@Autowired
	private UpdateBrandUseCase updateBrandUseCase;
	@Autowired
	private DeleteBrandUseCase deleteBrandUseCase;

	@Autowired
	private CreateProductUseCase createProductUseCase;
	@Autowired
	private UpdateProductUseCase updateProductUseCase;
	@Autowired
	private DeleteProductUseCase deleteProductUseCase;

	@Autowired
	private GetMinPriceByCategoryUseCase getMinPriceByCategoryUseCase;
	@Autowired
	private GetCategoryExtremesUseCase getCategoryExtremesUseCase;
	@Autowired
	private GetMinBrandCombinationUseCase getMinBrandCombinationUseCase;

	@Autowired
	private Cache<String, MinPriceCacheValue> minPriceByCategoryCache;
	@Autowired
	private Cache<String, MaxPriceCacheValue> maxPriceByCategoryCache;
	@Autowired
	private Cache<String, Long> brandTotalPriceCache;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		// 캐시 초기화
		minPriceByCategoryCache.invalidateAll();
		maxPriceByCategoryCache.invalidateAll();
		brandTotalPriceCache.invalidateAll();

		// 데이터베이스 테이블 초기화 (순서 확인)
		productRepository.deleteAllInBatch();
		categoryRepository.deleteAllInBatch();
		brandRepository.deleteAllInBatch();

		// ID 시퀀스 초기화 (H2 데이터베이스 사용 시)
		entityManager.createNativeQuery("ALTER TABLE products ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE categories ALTER COLUMN id RESTART WITH 1").executeUpdate();
		entityManager.createNativeQuery("ALTER TABLE brands ALTER COLUMN id RESTART WITH 1").executeUpdate();

		// 필요한 경우 entityManager flush 및 clear
		entityManager.flush();
		entityManager.clear();
	}

	@Test
	@DisplayName("브랜드 생성/수정/삭제 시 예외 없이 동작해야 함")
	void testBrandLifecycle() {
		// 1) 브랜드 생성
		Brand created = createBrandUseCase.createBrand("Nike");
		assertThat(created).isNotNull();
		assertThat(created.getName()).isEqualTo("Nike");

		// 2) 브랜드 수정
		Brand updated = updateBrandUseCase.updateBrand(1L, "Adidas");
		assertThat(updated.getName()).isEqualTo("Adidas");

		// 3) 브랜드 삭제
		deleteBrandUseCase.deleteBrand(1L);

		// 4) 삭제 후 조회 시 예외 발생
		Throwable thrown = catchThrowable(() -> updateBrandUseCase.updateBrand(1L, "Reebok"));
		assertThat(thrown).isInstanceOf(DataNotFoundException.class);
		DataNotFoundException ex = (DataNotFoundException) thrown;
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BRAND_NOT_FOUND);
	}

	@Test
	@DisplayName("동일 브랜드명 중복 생성 시 예외 발생")
	void testDuplicateBrandName() {
		// 첫 생성
		createBrandUseCase.createBrand("Nike");

		// 두 번째 동일 이름
		Throwable thrown = catchThrowable(() -> createBrandUseCase.createBrand("Nike"));
		// 이미 존재하는 브랜드이면 BusinessException or InvalidValueException 등 발생
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("이미 존재하는 브랜드");
	}

	@Test
	@DisplayName("상품 생성 시 가격이 음수면 예외 발생")
	void testCreateProductWithNegativePrice() {
		// 사전
		createBrandUseCase.createBrand("Puma");
		categoryRepository.save(new CategoryEntity("상의"));

		// 음수 가격
		Throwable thrown = catchThrowable(() ->
				createProductUseCase.createProduct("Puma", "상의", -100)
		);

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("가격은 0 이상이어야 합니다.");
	}

	@Test
	@DisplayName("상품 생성 후 카테고리별 최저가, 브랜드별 합계가 정상 갱신되는지 테스트")
	void testCreateProductAndCheckCache() {
		// 0) 선행 브랜드 생성
		createBrandUseCase.createBrand("Nike");
		categoryRepository.save(new CategoryEntity("상의"));

		// 1) 상품 생성
		Product product = createProductUseCase.createProduct("Nike", "상의", 10000);
		assertThat(product).isNotNull();
		assertThat(product.getBrand().getName()).isEqualTo("Nike");
		assertThat(product.getCategory().getName()).isEqualTo("상의");
		assertThat(product.getPriceValue()).isEqualTo(10000);

		// 2) 카테고리별 최저가 조회
		List<Product> minPriceProducts = getMinPriceByCategoryUseCase.getMinPriceByCategory();
		// "상의" 카테고리에 1개 존재 => 해당 상품이 포함되어 있어야 함
		assertThat(minPriceProducts).isNotEmpty();
		Product found = minPriceProducts.stream()
				.filter(p -> p.getCategory().getName().equals("상의"))
				.findAny()
				.orElse(null);
		assertThat(found).isNotNull();
		assertThat(found.getPriceValue()).isEqualTo(10000);

		// 3) 특정 카테고리 최저가/최고가
		var extremes = getCategoryExtremesUseCase.getCategoryExtremes("상의");
		assertThat(extremes).isNotNull();
		assertThat(extremes.minBrandName()).isEqualTo("Nike");
		assertThat(extremes.maxBrandName()).isEqualTo("Nike");
		assertThat(extremes.minPrice()).isEqualTo(10000);
		assertThat(extremes.maxPrice()).isEqualTo(10000);

		// 4) 브랜드별 최저 조합
		var cheapestBrand = getMinBrandCombinationUseCase.getMinBrandCombination();
		assertThat(cheapestBrand).isNotNull();
		assertThat(cheapestBrand.brandName()).isEqualTo("Nike");
		assertThat(cheapestBrand.totalPrice().getPrice()).isEqualTo(10000);
	}

	@Test
	@DisplayName("다수 브랜드/카테고리/상품 등록 후 브랜드별 최저합 비교")
	void testMultipleBrandsAndCategories() {
		// 1) 여러 브랜드 생성
		createBrandUseCase.createBrand("Nike");   // brandId=1
		createBrandUseCase.createBrand("Adidas"); // brandId=2

		// 2) 여러 카테고리
		categoryRepository.save(new CategoryEntity("상의"));     // id=1
		categoryRepository.save(new CategoryEntity("바지"));     // id=2

		// 3) 상품 등록
		// Nike 상의  -> 8,000
		var p1 = createProductUseCase.createProduct("Nike", "상의", 8000);
		// Nike 바지  -> 12,000
		var p2 = createProductUseCase.createProduct("Nike", "바지", 12000);

		// Adidas 상의 -> 9000
		var p3 = createProductUseCase.createProduct("Adidas", "상의", 9000);
		// Adidas 바지 -> 7000
		var p4 = createProductUseCase.createProduct("Adidas", "바지", 7000);

		// 4) getMinBrandCombination => 합계 비교
		// Nike = 8000 + 12000 = 20,000
		// Adidas = 9000 + 7000 = 16,000 => 더 저렴
		var cheapest = getMinBrandCombinationUseCase.getMinBrandCombination();
		assertThat(cheapest.brandName()).isEqualTo("Adidas");
		assertThat(cheapest.totalPrice().getPrice()).isEqualTo(16000);

		// 5) 카테고리별 최저가
		// 상의: Nike=8000, Adidas=9000 => Nike가 저렴
		var minPriceProducts = getMinPriceByCategoryUseCase.getMinPriceByCategory();
		// “상의” -> min=8000(Nike)
		var foundTop = minPriceProducts.stream()
				.filter(pp -> pp.getCategory().getName().equals("상의"))
				.findFirst()
				.orElse(null);
		assertThat(foundTop).isNotNull();
		assertThat(foundTop.getBrand().getName()).isEqualTo("Nike");
		assertThat(foundTop.getPriceValue()).isEqualTo(8000);

		// “바지” -> min=7000(Adidas)
		var foundPants = minPriceProducts.stream()
				.filter(pp -> pp.getCategory().getName().equals("바지"))
				.findFirst()
				.orElse(null);
		assertThat(foundPants).isNotNull();
		assertThat(foundPants.getBrand().getName()).isEqualTo("Adidas");
		assertThat(foundPants.getPriceValue()).isEqualTo(7000);

		// 6) 카테고리 Extremes
		// 상의 => min=8000(Nike), max=9000(Adidas)
		CategoryExtremesResult topExt = getCategoryExtremesUseCase.getCategoryExtremes("상의");
		assertThat(topExt.minPrice()).isEqualTo(8000);
		assertThat(topExt.minBrandName()).isEqualTo("Nike");
		assertThat(topExt.maxPrice()).isEqualTo(9000);
		assertThat(topExt.maxBrandName()).isEqualTo("Adidas");

		// 바지 => min=7000(Adidas), max=12000(Nike)
		CategoryExtremesResult pantsExt = getCategoryExtremesUseCase.getCategoryExtremes("바지");
		assertThat(pantsExt.minPrice()).isEqualTo(7000);
		assertThat(pantsExt.minBrandName()).isEqualTo("Adidas");
		assertThat(pantsExt.maxPrice()).isEqualTo(12000);
		assertThat(pantsExt.maxBrandName()).isEqualTo("Nike");
	}

	@Test
	@DisplayName("브랜드 이름이 빈문자이면 예외 발생")
	void testCreateBrand_emptyName() {
		// 가정: Service 로직에서 빈문자 or 공백만인 Brand 이름 -> InvalidValueException
		Throwable thrown = catchThrowable(() -> createBrandUseCase.createBrand("  "));
		assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("브랜드 이름은 필수");
	}

	@Test
	@DisplayName("존재하지 않는 브랜드 수정 시 DataNotFoundException")
	void testUpdateBrand_notExist() {
		// 가정: brandId=999는 없음
		Throwable thrown = catchThrowable(() -> updateBrandUseCase.updateBrand(999L, "Reebok"));
		assertThat(thrown).isInstanceOf(DataNotFoundException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.BRAND_NOT_FOUND);
	}

	@Test
	@DisplayName("없는 카테고리로 상품 생성 시 DataNotFoundException")
	void testCreateProduct_categoryNotExist() {
		// given: brand "Nike"는 존재
		createBrandUseCase.createBrand("Nike");
		// "상의" 카테고리는 미리 저장 안함 -> 존재X
		Throwable thrown = catchThrowable(() ->
				createProductUseCase.createProduct("Nike", "상의", 10000)
		);
		assertThat(thrown).isInstanceOf(DataNotFoundException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
	}

	@Test
	@DisplayName("없는 상품을 업데이트하려면 예외")
	void testUpdateProduct_notExist() {
		Throwable thrown = catchThrowable(() ->
				updateProductUseCase.updateProduct(999L, 15000)
		);
		// 가정: PRODUCT_NOT_FOUND
		assertThat(thrown).isInstanceOf(DataNotFoundException.class)
				.extracting("errorCode").isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
	}

	@Test
	@DisplayName("상품 가격 0원(경계값) 테스트")
	void testCreateProduct_zeroPrice() {
		// brand, category 미리 준비
		createBrandUseCase.createBrand("Uniqlo");
		categoryRepository.save(new CategoryEntity("바지"));

		Product product = createProductUseCase.createProduct("Uniqlo", "바지", 0);
		assertThat(product).isNotNull();
		assertThat(product.getPriceValue()).isEqualTo(0);
		// 이후 최저가, etc. 검증 가능
	}

	@Test
	@DisplayName("상품 가격이 Int Max(21억) 정도 되는 경우 처리")
	void testCreateProduct_largePrice() {
		createBrandUseCase.createBrand("Lv");
		categoryRepository.save(new CategoryEntity("가방"));

		long bigPrice = 2_147_483_000L;  // 근사치
		Product product = createProductUseCase.createProduct("Lv", "가방", bigPrice);
		assertThat(product.getPriceValue()).isEqualTo(bigPrice);
	}

	@Test
	@DisplayName("두 개 이상의 브랜드가 같은 총액(최저가) - 알파벳 순 우선")
	void testMultipleBrandsWithSameTotalPrice_alphabetical() {
		// BrandB, BrandA => "A"가 알파벳 순 앞선다
		createBrandUseCase.createBrand("BrandA");
		createBrandUseCase.createBrand("BrandB");

		categoryRepository.save(new CategoryEntity("상의"));
		categoryRepository.save(new CategoryEntity("바지"));

		// BrandA => 상의=5000, 바지=5000 => total=10000
		createProductUseCase.createProduct("BrandA", "상의", 5000);
		createProductUseCase.createProduct("BrandA", "바지", 5000);

		// BrandB => 상의=4000, 바지=6000 => total=10000 (동일)
		createProductUseCase.createProduct("BrandB", "상의", 4000);
		createProductUseCase.createProduct("BrandB", "바지", 6000);

		// 최저가 => 두 브랜드 모두 10000, 알파벳으로 BrandA<B => BrandA 선택
		var result = getMinBrandCombinationUseCase.getMinBrandCombination();
		assertThat(result.brandName()).isEqualTo("BrandA");
		assertThat(result.totalPrice().getPrice()).isEqualTo(10000);
	}

	@Test
	@DisplayName("카테고리 최저가/최고가 여러 브랜드 동일 - 알파벳 순으로 한 브랜드만 표시")
	void testCategoryExtremes_sameMinOrMax_alphabetical() {
		createBrandUseCase.createBrand("BrandA"); // brandId=1
		createBrandUseCase.createBrand("BrandB"); // brandId=2
		createBrandUseCase.createBrand("BrandC"); // brandId=3

		categoryRepository.save(new CategoryEntity("상의"));

		createProductUseCase.createProduct("BrandA", "상의", 10000);
		createProductUseCase.createProduct("BrandB", "상의", 10000);
		createProductUseCase.createProduct("BrandC", "상의", 10000);

		createProductUseCase.createProduct("BrandC", "상의", 15000);

		var extremes = getCategoryExtremesUseCase.getCategoryExtremes("상의");
		// Check
		assertThat(extremes.minPrice()).isEqualTo(10000);
		assertThat(extremes.minBrandName()).isEqualTo("BrandA");

		assertThat(extremes.maxPrice()).isEqualTo(15000);
		assertThat(extremes.maxBrandName()).isEqualTo("BrandC");
	}

}
