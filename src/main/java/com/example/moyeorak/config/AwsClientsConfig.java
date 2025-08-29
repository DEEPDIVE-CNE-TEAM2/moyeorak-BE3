package com.example.moyeorak.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class AwsClientsConfig {

    // application.yml(.properties)에 aws.region 없으면 기본 ap-northeast-2
    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    public CloudWatchClient cloudWatchClient() {
        CloudWatchClient client = CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build(); // 자격증명: 기본 체인(IAM Role/Env/Shared Credentials)
        log.info("[CloudWatch] region={}", region);
        return client;
    }

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient() {
        CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        log.info("[CloudWatchLogs] region={}", region);
        return client;
    }

    /**
     * S3 Presigner 빈 등록 (필수)
     * destroyMethod="close"로 컨텍스트 종료 시 안전하게 자원 해제
     */
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner() {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        log.info("[S3Presigner] region={}", region);
        return presigner;
    }
}
