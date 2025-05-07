package com.ksh.shopping_system.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrandTest {

	@Test
	@DisplayName("브랜드 객체가 올바르게 생성되는지 검증한다")
	void createBrand() {
		// given
		String name = "무신사";

		// when
		Brand brand = new Brand(name);

		// then
		assertThat(brand.getName()).isEqualTo(name);
	}

	@Nested
	@DisplayName("브랜드 생성 시")
	class CreateBrand {
		@Test
		@DisplayName("이름이 null이면 예외가 발생한다")
		void createBrandWithNullName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Brand(null));

			assertThat(exception.getMessage()).isEqualTo("브랜드 이름은 필수입니다.");
		}

		@Test
		@DisplayName("이름이 빈 문자열이면 예외가 발생한다")
		void createBrandWithEmptyName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Brand(""));

			assertThat(exception.getMessage()).isEqualTo("브랜드 이름은 필수입니다.");
		}

		@Test
		@DisplayName("이름이 공백 문자열이면 예외가 발생한다")
		void createBrandWithBlankName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Brand("  "));

			assertThat(exception.getMessage()).isEqualTo("브랜드 이름은 필수입니다.");
		}
	}

	@Test
	@DisplayName("같은 이름의 브랜드인지 대소문자 구분없이 비교한다")
	void isSameBrand() {
		// given
		Brand brand1 = new Brand("MUSINSA");
		Brand brand2 = new Brand("muSinsa");
		Brand brand3 = new Brand("Adidas");

		// when & then
		assertThat(brand1.isSameBrand(brand2)).isEqualTo(true);
		assertThat(brand1.isSameBrand(brand3)).isEqualTo(false);
	}
	
}