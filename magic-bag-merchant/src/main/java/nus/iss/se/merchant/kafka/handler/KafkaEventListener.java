package nus.iss.se.merchant.kafka.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.merchant.kafka.EventTopicType;
import nus.iss.se.merchant.kafka.event.MerchantProcessedEvent;
import nus.iss.se.merchant.service.IMerchantService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final ObjectMapper objectMapper;
    private final IMerchantService merchantService;

    @KafkaListener(topics = EventTopicType.MERCHANT_PROCESSED)
    public void consume(EventEnvelope event) throws MessagingException {
        try {
            log.info("handling event - {}, details: {}",EventTopicType.MERCHANT_PROCESSED, event);
            MerchantProcessedEvent processedEvent = objectMapper.readValue(event.data(), MerchantProcessedEvent.class);
            merchantService.handleRegisterResult(processedEvent);

            log.info("handle event - {}, success: {}",EventTopicType.MERCHANT_PROCESSED, processedEvent);
        } catch (JsonProcessingException e) {
            log.error("handle event - {}, failed. details: {}",EventTopicType.MERCHANT_PROCESSED, ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
