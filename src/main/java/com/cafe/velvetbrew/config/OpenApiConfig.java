package com.cafe.velvetbrew.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Without this, Swagger UI has no "Authorize" button and no way to attach a
 * Bearer token to a "Try it out" call - every protected endpoint (which is
 * nearly all of them) can only ever be exercised from the docs as an
 * unauthenticated request, always failing with 401.
 */
@Configuration
public class OpenApiConfig {

	private static final String SCHEME_NAME = "bearerAuth";

	@Bean
	public OpenAPI openApi() {

		return new OpenAPI()
				.components(new Components()
						.addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
	}
}
