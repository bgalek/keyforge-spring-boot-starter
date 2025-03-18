# KeyForge Spring Boot Starter

KeyForge Spring Boot Starter is a Spring Boot starter that integrates the KeyForge library for API key authentication.

## Features

- API key authentication using KeyForge
- Integration with Spring Security
- Easy configuration through Spring Boot properties

## Requirements

- Java 17 or higher
- Spring Boot 3.4.3 or higher

## Installation

Add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.github.bgalek:spring-boot-starter-keyforge:1.0.0")
}
```

## Configuration

Configure your API keys in the `application.yml` file:

```yaml
keyforge:
  keys:
    - sk_openai_MDk4ZjZiY2Q0NjIxMzM3MzhhZGU0ZTgzMjYyN2I0ZjY
```

## Usage

### Security Configuration

To give users full control `KeyForgeAuthenticationFilter`
is not automatically configured so you need to add it to Spring Security filter chain.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, KeyForgeAuthenticationFilter keyForgeAuthenticationFilter) throws Exception {
        http.addFilterBefore(keyForgeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(httpRequests -> {
            httpRequests.requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).authenticated()
                    .anyRequest()
                    .permitAll();
        });
        return http.build();
    }
}
```
