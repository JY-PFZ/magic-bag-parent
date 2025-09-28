package nus.iss.se.kafka.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.kafka.event.DomainEvent;
import nus.iss.se.kafka.event.EventEnvelope;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    @Override
    public void publish(EventEnvelope event) {
        try {
            // 发送消息，返回 CompletableFuture
            CompletableFuture<SendResult<String, EventEnvelope>> future = kafkaTemplate.send(event.topic(), event).toCompletableFuture();

            // 添加回调
            future
                    .thenAccept(result -> log.info("Kafka event send success: {}",result.getRecordMetadata().toString()))
                    .exceptionally(throwable -> {
                        log.error("Kafka event send failed: {}",throwable.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            // 可降级：写入数据库事件表，后续重试
            log.error("Kafka send exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void publishAll(List<EventEnvelope> events) {
        events.forEach(this::publish);
    }
}
