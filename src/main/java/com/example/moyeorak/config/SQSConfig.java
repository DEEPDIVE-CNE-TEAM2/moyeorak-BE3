package com.example.moyeorak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SQSConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${aws.use.default-credentials:false}")
    private boolean useDefaultCredentials;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider getCredentialsProvider() {
        if (useDefaultCredentials) {
            return software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider.create();
        } else {
            return software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider.create("sqs-user");
        }
    }
}
