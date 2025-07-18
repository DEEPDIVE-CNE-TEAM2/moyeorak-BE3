package com.example.moyeorak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import io.awspring.cloud.messaging.core.SqsTemplate;

@Configuration
public class SQSConfig {
    
    @Value("${cloud.aws.region.static}")
    private String region;

    // SQS Client
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }

    // SQS 템플릿
    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }

}
