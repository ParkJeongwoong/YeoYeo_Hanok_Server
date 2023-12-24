package com.yeoyeo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
	info = @io.swagger.v3.oas.annotations.info.Info(
		title = "예약/결제 API",
		version = "2.0.0",
		description = "한옥스테이 여여 - 서버 API"
))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi api() {
		return GroupedOpenApi.builder()
			.group("backend")
			.pathsToMatch("/**")
			.build();
    }

}
