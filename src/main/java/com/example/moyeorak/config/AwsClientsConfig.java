package com.example.moyeorak.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class AwsClientsConfig {

    /** AWS Region (application.yml의 cloud.aws.region.static 사용, 기본값: ap-northeast-2) */
    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    /** 자격 증명 공급자 선택 플래그: true → DefaultCredentialsProvider (IAM Role/Env), false → ProfileCredentialsProvider */
    @Value("${aws.use.default-credentials:true}")
    private boolean useDefaultCredentials;

    /** 로컬 개발 시 사용할 AWS profile 이름 (~/.aws/credentials) */
    @Value("${aws.profile.name:sqs-user}")
    private String profileName;

    /** 공용 CredentialsProvider (S3/CloudWatch/SQS 등 모든 클라이언트에서 재사용) */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (useDefaultCredentials) {
            log.info("[AWS Creds] Using DefaultCredentialsProvider (IAM Role / Env vars)");
            return DefaultCredentialsProvider.create();
        } else {
            log.info("[AWS Creds] Using ProfileCredentialsProvider profile={}", profileName);
            return ProfileCredentialsProvider.create(profileName);
        }
    }

    /** CloudWatch Metrics Client */
    @Bean
    public CloudWatchClient cloudWatchClient(AwsCredentialsProvider creds) {
        CloudWatchClient client = CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[CloudWatch] initialized region={}", region);
        return client;
    }

    /** CloudWatch Logs Client */
    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient(AwsCredentialsProvider creds) {
        CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[CloudWatchLogs] initialized region={}", region);
        return client;
    }

    /** S3 Presigner (컨텍스트 종료 시 안전하게 close) */
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(AwsCredentialsProvider creds) {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[S3Presigner] initialized region={}", region);
        return presigner;
    }
}
