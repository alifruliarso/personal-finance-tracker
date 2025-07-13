package com.galapea.techblog.pftgriddbcloud.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class NotImplementedException extends RuntimeException {

	public NotImplementedException() {
		super();
	}

	public NotImplementedException(final String message) {
		super(message);
	}
}
