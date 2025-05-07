package com.ksh.shopping_system.common.response;

public enum ErrorCode {
	// 파라미터 조건 에러 코드 4000 ~
	INVALID_PARAMETER("4000", "Invalid parameter"),

	// 제목 관련 에러코드
	INVALID_TITLE("4000", "Title cannot be null or empty"),
	TITLE_TOO_LONG("4000", "title is too long"),

	// 내용 관련 에러코드
	INVALID_CONTENT("4000", "Content cannot be null or empty"),
	CONTENT_TOO_LONG("4000", "Content is too long"),

	// 상품 , 브랜드, 카데고리 관련 에러코드
	BRAND_NOT_FOUND("4001", "해당 브랜드가 존재하지 않습니다."),
	CATEGORY_NOT_FOUND("4002", "해당 카테고리가 존재하지 않습니다."),
	PRODUCT_NOT_FOUND("4003", "해당 상품이 존재하지 않습니다."),
	BRAND_CACHE_NOT_FOUND("4004", "브랜드 캐시에 해당 브랜드가 없습니다."),
	CATEGORY_EXTREMES_CACHE_MISSING("4005", "카테고리 최저가/최고가 정보를 캐시에서 찾을 수 없습니다."),

	// 기타오류, 추후 필요하다면 ErrorCode 분리
	DATA_NOT_FOUND("9999", "entity not found"),
	DEFAULT_ERROR("9999", "default error");

	private final String code;
	private final String message;

	ErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}
