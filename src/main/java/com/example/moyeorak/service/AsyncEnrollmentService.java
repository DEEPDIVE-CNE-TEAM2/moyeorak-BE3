package com.example.moyeorak.service;

import com.example.moyeorak.dto.EnrollmentMessage;
import com.example.moyeorak.dto.EnrollmentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEnrollmentService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs.queue.url}")
    private String queueUrl;

    public void sendEnrollment(EnrollmentRequest request, String email) {
        log.info("[ASYNC] 대관 신청 요청 큐에 전송 중... by {}", email);

        try {
            // 메시지 변환
            String payload = objectMapper.writeValueAsString(new EnrollmentMessage(request, email));

            // SQS 메시지 요청 생성
            SendMessageRequest messageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payload)
                    .build();

            // 메시지 전송
            sqsAsyncClient.sendMessage(messageRequest)
                    .thenAccept(response -> {
                        log.info("[ASYNC] SQS 전송 성공. 메시지 ID: {}", response.messageId());
                    })
                    .exceptionally(e -> {
                        log.error("[ASYNC] SQS 전송 실패", e);
                        return null;
                    });

        } catch (JsonProcessingException e) {
            log.error("메시지 변환 실패", e);
        }
    }
}
