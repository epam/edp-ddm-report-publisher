package com.epam.digital.data.platform.report.config;

import com.epam.digital.data.platform.report.config.feign.FeignErrorDecoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.Retryer.Default;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Value("${redash.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate ->
            requestTemplate.header("Authorization", apiKey);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Default();
    }
}
