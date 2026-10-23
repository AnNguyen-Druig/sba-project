package com.sba.project.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigCorsTests {
    @Test
    void allowsConfiguredFrontendPreflightWithRefreshCookieCredentials() {
        var source = new SecurityConfig()
                .corsConfigurationSource("http://localhost:3000, http://127.0.0.1:3000, http://localhost:5173");
        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/api/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:3000")).isEqualTo("http://localhost:3000");
        assertThat(configuration.checkOrigin("http://127.0.0.1:3000")).isEqualTo("http://127.0.0.1:3000");
        assertThat(configuration.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("https://untrusted.example")).isNull();
        assertThat(configuration.getAllowedMethods()).contains("OPTIONS", "POST", "DELETE");
        assertThat(configuration.checkHeaders(java.util.List.of("Authorization", "Content-Type")))
                .containsExactlyInAnyOrder("Authorization", "Content-Type");
        assertThat(configuration.getAllowCredentials()).isTrue();
        assertThat(configuration.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void fallsBackToLocalhostOriginsWhenConfigIsEmpty() {
        var source = new SecurityConfig().corsConfigurationSource("");
        CorsConfiguration configuration = source.getCorsConfiguration(
                new MockHttpServletRequest("OPTIONS", "/api/auth/login"));

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("http://localhost:3000")).isEqualTo("http://localhost:3000");
        assertThat(configuration.checkOrigin("http://127.0.0.1:5173")).isEqualTo("http://127.0.0.1:5173");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
