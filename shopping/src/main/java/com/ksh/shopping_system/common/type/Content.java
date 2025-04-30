package com.ksh.shopping_system.common.type;

import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.exception.InvalidValueException;
import lombok.Getter;

@Getter
public class Content {
	private static final int CONTENT_MAX_SIZE = 1500;

	private String Content;

	public Content(String content) {

		if (content == null || content.isEmpty()) {
			throw new InvalidValueException(ErrorCode.INVALID_TITLE);
		}

		if(content.length() > CONTENT_MAX_SIZE) {
			throw new InvalidValueException(ErrorCode.TITLE_TOO_LONG);
		}

		this.Content = content;
	}

}
