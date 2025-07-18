package com.example.moyeorak.service;

import com.example.moyeorak.dto.RentalApplicationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.operations.SqsTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncRentalApplicationService {

    private final SqsTemplate sqsTemplate;

    @Value("${sqs.queue.url}")
    private String queueUrl;

    // SQS에 대관 신청 메시지를 전송
    public void sendRentalApplication(RentalApplicationRequest request, String email) {
        log.info("[ASYNC] 대관 신청 요청 큐에 전송 중... by {}", email);

        sqsTemplate.send(to -> to.queue(queueUrl).payload(new RentalApplicationMessage(request, email)));

        log.info("[ASYNC] SQS 전송 완료: {}", request);
    }
}
