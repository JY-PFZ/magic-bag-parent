package nus.iss.se.auth.kafka.handler;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.auth.kafka.EventTopicType;
import nus.iss.se.auth.service.EmailService;
import nus.iss.se.kafka.event.EventEnvelope;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final EmailService emailService;

    @KafkaListener(topics = EventTopicType.USER_REGISTERED)
    public void consume(EventEnvelope event) throws MessagingException {
        emailService.sendActivationEmail(event.data());
        log.info("success!: {}",event);
    }
}
