package com.ksh.shopping_system.domain;

import com.ksh.shopping_system.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryTest {

	@Test
	@DisplayName("카테고리 객체가 올바르게 생성되는지 검증한다")
	void createCategory() {
		// given
		String name = "상의";

		// when
		Category category = new Category(name);

		// then
		assertThat(category.getName()).isEqualTo(name);
	}

	@Test
	@DisplayName("카데고리가 255자가 넘으면 에러 발생")
	void validateCategoryName() {
		// given
		String name = "abcd";
		for (int i = 0; i < 255; i++) {
			name += "abcd";
		}

		String finalName = name;

		assertThatThrownBy(() -> new Category(finalName))
				.isInstanceOf(InvalidValueException.class)
				.hasMessage("title is too long");
	}

	@Nested
	@DisplayName("카테고리 생성 시")
	class CreateCategory {

		@Test
		@DisplayName("이름이 null이면 예외가 발생한다")
		void createCategoryWithNullName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Category(null));

			assertThat(exception.getMessage()).isEqualTo("카테고리 이름은 필수입니다.");
		}

		@Test
		@DisplayName("이름이 빈 문자열이면 예외가 발생한다")
		void createCategoryWithEmptyName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Category(""));

			assertThat(exception.getMessage()).isEqualTo("카테고리 이름은 필수입니다.");
		}

		@Test
		@DisplayName("이름이 공백 문자열이면 예외가 발생한다")
		void createCategoryWithBlankName() {
			// when & then
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new Category("   "));

			assertThat(exception.getMessage()).isEqualTo("카테고리 이름은 필수입니다.");
		}
	}

}
