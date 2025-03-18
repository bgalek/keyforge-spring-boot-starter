package com.github.bgalek.keyforge.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeyForgeSpringBootStarterApplicationTests {

    @LocalServerPort
    int port;
    RestOperations restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:%d".formatted(port))
                .errorHandler(response -> false)
                .build();
    }

    @Test
    void shouldNotBreakAnonymousEndpointsWithAuthenticationInjection() {
        String response = restTemplate.getForObject("/anonymousAuthentication", String.class);
        assertThat(response).isEqualTo("anonymous authentication");
    }

    @Test
    void shouldNotBreakAnonymousEndpointsWithPrincipalInjection() {
        String response = restTemplate.getForObject("/anonymousPrincipal", String.class);
        assertThat(response).isEqualTo("anonymous principal");
    }

    @Test
    void shouldNotBreakDefaultSpringSecurityBehaviorWhenNoAuthorizationProvided() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/authentication", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotBreakDefaultSpringSecurityBehaviorWhenNoPrincipalProvided() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/principal", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldAuthenticateIfValidKeyProvided() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Token sk_openai_MDk4ZjZiY2Q0NjIxMzM3MzhhZGU0ZTgzMjYyN2I0ZjY");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/authentication",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("openai");
    }

    @Test
    void shouldAuthenticatePrincipalIfValidKeyProvided() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Token sk_openai_MDk4ZjZiY2Q0NjIxMzM3MzhhZGU0ZTgzMjYyN2I0ZjY");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/principal",
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("openai");
    }

    @SpringBootApplication
    static class TestApp {
        public static void main(String[] args) {
            SpringApplication.run(TestApp.class, args);
        }

        @RestController
        @RequestMapping("/")
        static class TestController {
            @GetMapping("/anonymousAuthentication")
            String getAnonymousAuthentication(Authentication authentication) {
                return Optional.ofNullable(authentication).map(Authentication::getName).orElse("anonymous authentication");
            }

            @GetMapping("/anonymousPrincipal")
            String getAnonymousPrincipal(Principal principal) {
                return Optional.ofNullable(principal).map(Principal::getName).orElse("anonymous principal");
            }

            @GetMapping("/api/authentication")
            String getAuthentication(Authentication authentication) {
                return authentication.getName();
            }

            @GetMapping("/api/principal")
            String getPrincipal(Principal principal) {
                return principal.getName();
            }
        }

        @Configuration
        @EnableWebSecurity
        static class SecurityConfig {
            @Bean
            SecurityFilterChain securityFilterChain(HttpSecurity http, KeyForgeAuthenticationFilter keyForgeAuthenticationFilter) throws Exception {
                http.addFilterBefore(keyForgeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                http.authorizeHttpRequests(httpRequests -> {
                    httpRequests.requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).authenticated()
                            .anyRequest()
                            .permitAll();
                });
                return http.build();
            }
        }
    }
}
