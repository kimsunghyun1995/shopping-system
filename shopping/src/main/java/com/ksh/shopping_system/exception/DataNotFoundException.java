package com.ksh.shopping_system.exception;


import com.ksh.shopping_system.common.response.ErrorCode;

public class DataNotFoundException extends BusinessException{

	public DataNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public DataNotFoundException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

}
