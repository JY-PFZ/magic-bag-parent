package nus.iss.se.merchant.kafka.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.kafka.event.EventEnvelope;
import nus.iss.se.merchant.kafka.EventTopicType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {


    @KafkaListener(topics = EventTopicType.MERCHANT_PROCESSED)
    public void consume(EventEnvelope event) throws MessagingException {

    }
}
