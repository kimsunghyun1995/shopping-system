package com.ksh.shopping_system.common.type;

import com.ksh.shopping_system.common.response.ErrorCode;
import com.ksh.shopping_system.exception.InvalidValueException;

public class Title {
	private static final int TITLE_MAX_SIZE = 255;

	private String title;

	public Title(String title) {

		if (title == null || title.isEmpty()) {
			throw new InvalidValueException(ErrorCode.INVALID_TITLE);
		}

		if(title.length() > TITLE_MAX_SIZE) {
			throw new InvalidValueException(ErrorCode.TITLE_TOO_LONG);
		}

		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
