package nus.iss.se.kafka.event;

import java.util.UUID;

public record EventEnvelope(
        String messageId,
        long timestamp,
        String topic,
        String data
){
    public static EventEnvelope of(String data, String topic) {
        return new EventEnvelope(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                topic,
                data
        );
    }
}
