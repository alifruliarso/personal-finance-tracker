package com.galapea.techblog.pftgriddbcloud.webapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridDbCloudClientConfig {

	@Bean
	public GridDbCloudClient gridDbCloudClient(GridDbCloudClientProperties properties) {
		return new GridDbCloudClient(properties.getBaseUrl(), properties.getAuthToken());
	}
}
