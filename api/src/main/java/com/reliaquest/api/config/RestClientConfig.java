package com.reliaquest.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${api.employee.base-url}")
    private String employeeBaseUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(employeeBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
