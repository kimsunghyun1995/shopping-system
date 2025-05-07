package com.ksh.shopping_system.domain;

import com.ksh.shopping_system.common.type.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

	@Test
	@DisplayName("상품 객체가 올바르게 생성되는지 검증한다")
	void createProduct() {
		// given
		Brand brand = new Brand("무신사");
		Category category = new Category("상의");
		Price price = new Price(12000);

		// when
		Product product = new Product(brand, category, price);

		// then
		assertThat(product.getBrand()).isEqualTo(brand);
		assertThat(product.getCategory()).isEqualTo(category);
		assertThat(product.getPrice()).isEqualTo(price);
	}

	@Nested
	@DisplayName("상품 생성 시")
	class CreateProduct {

		@Test
		@DisplayName("브랜드가 null이면 예외가 발생한다")
		void createProductWithNullBrand() {
			// given
			Category category = new Category("아우터");
			Price price = new Price(10000);

			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Product(null, category, price));

			assertThat(exception.getMessage()).isEqualTo("브랜드와 카테고리는 필수입니다.");
		}

		@Test
		@DisplayName("카테고리가 null이면 예외가 발생한다")
		void createProductWithNullCategory() {
			// given
			Brand brand = new Brand("무신사");
			Price price = new Price(10000);

			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Product(brand, null, price));

			assertThat(exception.getMessage()).isEqualTo("브랜드와 카테고리는 필수입니다.");
		}
	}

	@Test
	@DisplayName("상품 가격을 변경할 수 있다")
	void changePrice() {
		// given
		Brand brand = new Brand("무신사");
		Category category = new Category("모자");
		Price originalPrice = new Price(8000);
		Product product = new Product(brand, category, originalPrice);

		// when
		Price newPrice = new Price(9000);
		product.changePrice(newPrice);

		// then
		assertThat(product.getPrice()).isEqualTo(newPrice);
	}

}
