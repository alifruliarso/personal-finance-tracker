package com.galapea.techblog.pftgriddbcloud.webapi;

public record SQLUpdateResponse(int status, int updatedRows, String stmt, String message) {

	public SQLUpdateResponse(int status, int updatedRows, String stmt) {
		this(status, updatedRows, stmt, null);
	}
}
