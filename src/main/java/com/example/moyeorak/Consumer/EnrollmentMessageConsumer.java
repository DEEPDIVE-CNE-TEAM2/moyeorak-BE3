package com.example.moyeorak.consumer;

import com.example.moyeorak.dto.EnrollmentRequest;
import com.example.moyeorak.dto.EnrollmentMessage;
import com.example.moyeorak.service.EnrollmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentMessageConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final EnrollmentService enrollmentService;

    @Value("${sqs.queue.url}")
    private String queueUrl;

    @PostConstruct
    public void startPolling() {
        Thread pollingThread = new Thread(this::poll);
        pollingThread.setDaemon(true); // 앱 종료시 자동 종료
        pollingThread.start();
    }

    private void poll() {
        log.info("[SQS CONSUMER] SQS 메시지 폴링 시작");

        while (true) {
            try {
                ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(5)
                        .waitTimeSeconds(10)
                        .build();

                List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

                for (Message message : messages) {
                    handleMessage(message);
                    deleteMessage(message);
                }
            } catch (Exception e) {
                log.error("[SQS CONSUMER] 메시지 처리 중 예외 발생", e);
            }
        }
    }

    private void handleMessage(Message message) {
        try {
            log.info("[SQS CONSUMER] 수신된 메시지: {}", message.body());

            EnrollmentMessage enrollmentMessage = objectMapper.readValue(
                    message.body(), EnrollmentMessage.class);

            EnrollmentRequest request = enrollmentMessage.getRequest();
            String email = enrollmentMessage.getEmail();

            enrollmentService.enrollByEmail(email, request);

            log.info("[SQS CONSUMER] 수강 신청 완료 for {}", email);

        } catch (Exception e) {
            log.error("[SQS CONSUMER] 메시지 처리 실패", e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
        log.info("[SQS CONSUMER] 메시지 삭제 완료");
    }
}
