package com.github.bgalek.keyforge.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.github.bgalek.keyforge.spring.KeyForgeAutoConfiguration.KeyForgeProperties;

@Configuration
@EnableConfigurationProperties(KeyForgeProperties.class)
public class KeyForgeAutoConfiguration {

    @Bean
    public KeyForgeAuthenticationFilter keyForgeAuthenticationFilter(KeyForgeProperties keyForgeProperties) {
        return new KeyForgeAuthenticationFilter(keyForgeProperties, null);
    }

    @ConfigurationProperties("keyforge")
    public static class KeyForgeProperties {
        private final List<String> keys;

        public KeyForgeProperties(List<String> keys) {
            this.keys = keys;
        }

        public List<String> getKeys() {
            return keys;
        }
    }
}
