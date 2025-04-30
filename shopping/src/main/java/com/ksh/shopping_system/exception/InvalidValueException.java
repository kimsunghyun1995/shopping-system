package com.ksh.shopping_system.exception;


import com.ksh.shopping_system.common.response.ErrorCode;

public class InvalidValueException extends BusinessException {

	public InvalidValueException(ErrorCode errorCode) {
		super(errorCode);
	}

	public InvalidValueException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

}
