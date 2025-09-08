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

    // 다른 설정들과 통일: cloud.aws.region.static
    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    // 로컬(프로파일) / 운영(IAM Role) 전환 플래그
    @Value("${aws.use.default-credentials:true}")
    private boolean useDefaultCredentials;

    // 로컬에서 사용할 Shared Credentials 프로파일명
    @Value("${aws.profile.name:sqs-user}")
    private String profileName;

    /** 공용 CredentialsProvider (S3/CloudWatch/SQS에서 재사용) */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (useDefaultCredentials) {
            log.info("[AWS Creds] DefaultCredentialsProvider (IAM Role/Env)");
            return DefaultCredentialsProvider.create();
        } else {
            log.info("[AWS Creds] ProfileCredentialsProvider profile={}", profileName);
            return ProfileCredentialsProvider.create(profileName);
        }
    }

    @Bean
    public CloudWatchClient cloudWatchClient(AwsCredentialsProvider creds) {
        CloudWatchClient client = CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[CloudWatch] region={}", region);
        return client;
    }

    @Bean
    public CloudWatchLogsClient cloudWatchLogsClient(AwsCredentialsProvider creds) {
        CloudWatchLogsClient client = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[CloudWatchLogs] region={}", region);
        return client;
    }

    /** S3 Presigner (컨텍스트 종료 시 안전하게 close) */
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(AwsCredentialsProvider creds) {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();
        log.info("[S3Presigner] region={}", region);
        return presigner;
    }
}
