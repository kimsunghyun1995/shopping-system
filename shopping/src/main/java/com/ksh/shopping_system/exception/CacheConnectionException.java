package com.ksh.shopping_system.exception;

/**
 * 캐시 연결(혹은 캐시 호출)에서 일시적 장애 발생 시 사용.
 * retryable 대상 예외
 */
public class CacheConnectionException extends RuntimeException {
	public CacheConnectionException(String message) {
		super(message);
	}
}
