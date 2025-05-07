package com.ksh.shopping_system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksh.shopping_system.adapter.out.persistence.repository.BrandRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.CategoryRepository;
import com.ksh.shopping_system.adapter.out.persistence.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * 통합 시나리오 API 테스트입니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShoppingSystemApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BrandRepository brandRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("브랜드 생성, 수정, 삭제 API 테스트")
	void brandCrudTest() throws Exception {
		// 1) Create
		mockMvc.perform(post("/api/products/brand")
						.param("brandName", "ZBrand"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드 생성 완료: ZBrand"));

		// 2) Update
		var brandZ = brandRepository.findByName("ZBrand")
				.orElseThrow(() -> new IllegalStateException("Brand not found: ZBrand"));

		mockMvc.perform(put("/api/products/brand/{brandId}", brandZ.getId())
						.param("newName", "ZUpdated"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드명 수정 완료: ZUpdated"));

		// 3) Delete
		mockMvc.perform(delete("/api/products/brand/{brandId}", brandZ.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드 삭제 완료"));
	}

	@Test
	@DisplayName("상품 생성, 수정, 삭제 API 테스트")
	void productCrudTest() throws Exception {
		// 1) Create
		mockMvc.perform(post("/api/products")
						.param("brandName", "A")
						.param("categoryName", "상의")
						.param("price", "12345"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("상품 생성 완료: A/상의/12345"));

		// 2) find product in DB
		var createdProduct = productRepository.findAll().stream()
				.filter(p -> p.getPrice().equals(12345L))
				.findAny().orElse(null);
		assertThat(createdProduct).isNotNull();

		// 3) Update
		mockMvc.perform(put("/api/products/{productId}", createdProduct.getId())
						.param("newPrice", "99999"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultMessage").value("상품 가격 수정 완료: 99999"));

		// check DB
		var updated = productRepository.findById(createdProduct.getId()).orElseThrow();
		assertThat(updated.getPrice()).isEqualTo(99999L);

		// 4) Delete
		mockMvc.perform(delete("/api/products/{productId}", createdProduct.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultMessage").value("상품 삭제 완료"));

		// DB에 없는지 확인
		assertThat(productRepository.findById(createdProduct.getId())).isEmpty();
	}

	@Test
	@DisplayName("1. 카테고리별 최저가 + 총액 조회")
	void getMinPriceByCategoryTest() throws Exception {
		mockMvc.perform(get("/api/products/min-price-by-category"))
				.andExpect(status().isOk())
				// 첫 번째 항목 확인 (가방)
				.andExpect(jsonPath("$[0].category").value("가방"))
				.andExpect(jsonPath("$[0].brand").value("A"))
				.andExpect(jsonPath("$[0].price").value(2000))
				// 총액 확인 (마지막 항목)
				.andExpect(jsonPath("$[8].category").value("총액"))
				.andExpect(jsonPath("$[8].brand").value(""))
				.andExpect(jsonPath("$[8].price").value(34100))
				.andExpect(jsonPath("$[8].resultCode").value("0000"))
				.andExpect(jsonPath("$[8].resultMessage").value("success"));
	}

	@Test
	@DisplayName("2. 단일 브랜드 기준 최저가 상품 조합 조회")
	void getMinBrandCombinationTest() throws Exception {
		mockMvc.perform(get("/api/products/cheapest-brand"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.brand").value("D"))
				.andExpect(jsonPath("$.totalPrice").value(36100))
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("success"))
				// categoryPrices 배열 항목 확인
				.andExpect(jsonPath("$.categoryPrices[0].category").value("상의"))
				.andExpect(jsonPath("$.categoryPrices[0].price").value(10100))
				.andExpect(jsonPath("$.categoryPrices[2].category").value("바지"))
				.andExpect(jsonPath("$.categoryPrices[2].price").value(3000))
				.andExpect(jsonPath("$.categoryPrices[5].category").value("모자"))
				.andExpect(jsonPath("$.categoryPrices[5].price").value(1500));
	}

	@Test
	@DisplayName("3. 특정 카테고리 최저가/최고가 조회")
	void getCategoryExtremesTest() throws Exception {
		// 예: categoryName=상의 => min brand=C(10000), max brand=I(11400)
		mockMvc.perform(get("/api/products/category-extremes")
						.param("categoryName", "상의"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").value("상의"))
				.andExpect(jsonPath("$.minPrice.brand").value("C"))
				.andExpect(jsonPath("$.minPrice.price").value(10000))
				.andExpect(jsonPath("$.maxPrice.brand").value("I"))
				.andExpect(jsonPath("$.maxPrice.price").value(11400))
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("success"));
	}

}
