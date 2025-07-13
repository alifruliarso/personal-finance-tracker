package com.galapea.techblog.pftgriddbcloud.webapi;

import org.springframework.http.HttpStatusCode;

public class GridDbException extends RuntimeException {
	private final HttpStatusCode statusCode;
	private final String errorBody;

	public GridDbException(String message, HttpStatusCode statusCode, String errorBody) {
		super(message);
		this.statusCode = statusCode;
		this.errorBody = errorBody;
	}

	public GridDbException(
			String message, HttpStatusCode statusCode, String errorBody, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
		this.errorBody = errorBody;
	}

	public HttpStatusCode getStatusCode() {
		return statusCode;
	}

	public String getErrorBody() {
		return errorBody;
	}
}
