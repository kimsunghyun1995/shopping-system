package com.ksh.shopping_system.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksh.shopping_system.application.port.in.brand.CreateBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.DeleteBrandUseCase;
import com.ksh.shopping_system.application.port.in.brand.UpdateBrandUseCase;
import com.ksh.shopping_system.application.port.in.product.*;
import com.ksh.shopping_system.common.type.Price;
import com.ksh.shopping_system.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	// Brand
	@MockitoBean
	private CreateBrandUseCase createBrandUseCase;
	@MockitoBean
	private UpdateBrandUseCase updateBrandUseCase;
	@MockitoBean
	private DeleteBrandUseCase deleteBrandUseCase;

	// Product
	@MockitoBean
	private CreateProductUseCase createProductUseCase;
	@MockitoBean
	private UpdateProductUseCase updateProductUseCase;
	@MockitoBean
	private DeleteProductUseCase deleteProductUseCase;

	// Query
	@MockitoBean
	private GetMinPriceByCategoryUseCase getMinPriceByCategoryUseCase;
	@MockitoBean
	private GetMinBrandCombinationUseCase getMinBrandCombinationUseCase;
	@MockitoBean
	private GetCategoryExtremesUseCase getCategoryExtremesUseCase;

	@Test
	@DisplayName("브랜드 생성 API - 정상")
	void createBrand_ok() throws Exception {
		var mockBrand = new Brand("Nike");
		given(createBrandUseCase.createBrand("Nike")).willReturn(mockBrand);

		mockMvc.perform(post("/api/products/brand")
						.param("brandName", "Nike")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드 생성 완료: Nike"));
	}

	@Test
	@DisplayName("브랜드 수정 API - 정상")
	void updateBrand_ok() throws Exception {
		Brand updatedBrand = new Brand("Adidas");
		given(updateBrandUseCase.updateBrand(1L, "Adidas")).willReturn(updatedBrand);

		mockMvc.perform(put("/api/products/brand/{brandId}", 1L)
						.param("newName", "Adidas")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드명 수정 완료: Adidas"));
	}

	@Test
	@DisplayName("브랜드 삭제 API - 정상")
	void deleteBrand_ok() throws Exception {
		willDoNothing().given(deleteBrandUseCase).deleteBrand(1L);

		mockMvc.perform(delete("/api/products/brand/{brandId}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("브랜드 삭제 완료"));
	}

	@Test
	@DisplayName("상품 생성 API - 정상")
	void createProduct_ok() throws Exception {
		// given
		Brand brand = new Brand("Nike");
		Category category = new Category("상의");
		Product mockProduct = new Product(brand, category, new Price(10000));
		given(createProductUseCase.createProduct("Nike", "상의", 10000))
				.willReturn(mockProduct);

		mockMvc.perform(post("/api/products")
						.param("brandName", "Nike")
						.param("categoryName", "상의")
						.param("price", "10000")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("상품 생성 완료: Nike/상의/10000"));
	}

	@Test
	@DisplayName("상품 수정 API - 정상")
	void updateProduct_ok() throws Exception {
		Brand brand = new Brand("Nike");
		Category category = new Category("상의");
		Product updated = new Product(brand, category, new Price(12000));

		given(updateProductUseCase.updateProduct(1L, 12000))
				.willReturn(updated);

		mockMvc.perform(put("/api/products/{productId}", 1L)
						.param("newPrice", "12000")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("상품 가격 수정 완료: 12000"));
	}

	@Test
	@DisplayName("상품 삭제 API - 정상")
	void deleteProduct_ok() throws Exception {
		willDoNothing().given(deleteProductUseCase).deleteProduct(1L);

		mockMvc.perform(delete("/api/products/{productId}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.resultCode").value("0000"))
				.andExpect(jsonPath("$.resultMessage").value("상품 삭제 완료"));
	}

	@Test
	@DisplayName("카테고리별 최저가 조회 API - 정상")
	void getMinPriceByCategory_ok() throws Exception {
		Brand brandA = new Brand("A");
		Category catTop = new Category("상의");
		Product product1 = new Product(brandA, catTop, new Price(5000));

		// mock
		given(getMinPriceByCategoryUseCase.getMinPriceByCategory())
				.willReturn(List.of(product1));

		mockMvc.perform(get("/api/products/min-price-by-category"))
				.andExpect(status().isOk())
				// JSON Array
				.andExpect(jsonPath("$[0].category").value("상의"))
				.andExpect(jsonPath("$[0].brand").value("A"))
				.andExpect(jsonPath("$[0].price").value(5000))
				.andExpect(jsonPath("$[1].category").value("총액"))
				.andExpect(jsonPath("$[1].price").value(5000));
	}

	@Test
	@DisplayName("단일 브랜드 기준 최저가 상품 조합 조회 API - 정상")
	void getMinBrandCombination_ok() throws Exception {
		Brand brandD = new Brand("D");
		Category catTop = new Category("상의");
		Product p1 = new Product(101L, brandD, catTop, new Price(5100));

		CheapestBrandResult mockResult = new CheapestBrandResult(
				"D",
				new Price(5100),
				List.of(p1)
		);
		given(getMinBrandCombinationUseCase.getMinBrandCombination())
				.willReturn(mockResult);

		mockMvc.perform(get("/api/products/cheapest-brand"))
				.andExpect(status().isOk())
				// JSON fields
				.andExpect(jsonPath("$.brand").value("D"))
				.andExpect(jsonPath("$.categoryPrices[0].category").value("상의"))
				.andExpect(jsonPath("$.categoryPrices[0].price").value(5100))
				.andExpect(jsonPath("$.totalPrice").value(5100));
	}

	@Test
	@DisplayName("특정 카테고리 최저가/최고가 조회 API - 정상")
	void getCategoryExtremes_ok() throws Exception {
		CategoryExtremesResult extremes = new CategoryExtremesResult(
				"상의",
				"C", 10000,
				"I", 11400
		);
		given(getCategoryExtremesUseCase.getCategoryExtremes("상의"))
				.willReturn(extremes);

		mockMvc.perform(get("/api/products/category-extremes")
						.param("categoryName", "상의")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").value("상의"))
				.andExpect(jsonPath("$.minPrice.brand").value("C"))
				.andExpect(jsonPath("$.minPrice.price").value(10000))
				.andExpect(jsonPath("$.maxPrice.brand").value("I"))
				.andExpect(jsonPath("$.maxPrice.price").value(11400));
	}

}
