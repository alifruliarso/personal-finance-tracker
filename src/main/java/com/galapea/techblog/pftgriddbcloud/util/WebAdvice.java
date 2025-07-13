package com.galapea.techblog.pftgriddbcloud.util;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Provide attributes available in all templates.
 */
@ControllerAdvice
public class WebAdvice {

	@ModelAttribute("requestUri")
	public String getRequestUri(final HttpServletRequest request) {
		return request.getRequestURI();
	}

	@ModelAttribute("isDevserver")
	public Boolean getIsDevserver(final HttpServletRequest request) {
		return "1".equals(request.getHeader("X-Devserver"));
	}
}
