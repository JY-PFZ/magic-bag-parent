package nus.iss.se.kafka.event;

public interface DomainEvent {
    String messageId();
    long timestamp();


    default String topic(){
        Class<?> clazz = this.getClass();
        DomainEventDef annotation = clazz.getAnnotation(DomainEventDef.class);
        if (annotation != null) {
            return annotation.topic();
        }
        throw new RuntimeException("Event class " + clazz.getSimpleName() + " is missing @DomainEventDef annotation");
    }
}
