package com.galapea.techblog.pftgriddbcloud.config;

import java.util.Set;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.SessionTrackingMode;

@Configuration
public class ServletConfig {

	@Bean
	public ServletContextInitializer servletContextInitializer() {
		// prevent Thymeleaf from appending the session id to resources
		return servletContext ->
				servletContext.setSessionTrackingModes(Set.of(SessionTrackingMode.COOKIE));
	}
}
