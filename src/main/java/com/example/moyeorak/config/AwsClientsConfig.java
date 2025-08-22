package com.example.moyeorak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
public class AwsClientsConfig {

    // application.properties 에 aws.region이 없을 때 기본값으로 ap-northeast-2 사용
    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    public CloudWatchClient cloudWatchClient() {
        CloudWatchClient client = CloudWatchClient.builder()
                .region(Region.of(region))
                .build(); // 자격증명: 기본 프로바이더 체인(IAM Role/환경변수/SharedCredentials)
        System.out.println("[CloudWatch] region=" + region);
        return client;
    }

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient() {
        CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .build();
        System.out.println("[CloudWatchLogs] region=" + region);
        return client;
    }
}
