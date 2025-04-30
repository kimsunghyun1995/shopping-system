package com.ksh.shopping_system.common.response;

import lombok.Getter;

// 공통 응답 구조
@Getter
public class BaseResponse {
	private String resultCode;
	private String resultMessage;

	public BaseResponse() {
		this.resultCode = "0000";
		this.resultMessage = "success";
	}

	public BaseResponse(String resultCode, String resultMessage) {
		this.resultCode = resultCode;
		this.resultMessage = resultMessage;
	}

}
