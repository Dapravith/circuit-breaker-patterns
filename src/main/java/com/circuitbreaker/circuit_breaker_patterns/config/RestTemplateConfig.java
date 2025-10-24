package com.circuitbreaker.circuit_breaker_patterns.config;

import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.web.client.*;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {

        // Create request factory with timeout settings
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 5 seconds connection timeout
        requestFactory.setReadTimeout(10000); // 10 seconds read timeout

        return new RestTemplate(requestFactory);
    }
}
