package com.ksh.shopping_system.common.response;

import lombok.Getter;

@Getter
public class ErrorResponse extends BaseResponse {
	public ErrorResponse(String resultCode, String resultMessage) {
		super(resultCode, resultMessage);
	}
	// 추후, 커스텀한 ErrorResponse 생성
}