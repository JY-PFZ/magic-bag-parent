package nus.iss.se.kafka.publisher;


import nus.iss.se.kafka.event.DomainEvent;
import nus.iss.se.kafka.event.EventEnvelope;

import java.util.List;

public interface EventPublisher {
    void publish(EventEnvelope event);
    void publishAll(List<EventEnvelope> events);
}
