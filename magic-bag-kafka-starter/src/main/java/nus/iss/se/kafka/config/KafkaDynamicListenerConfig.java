//package nus.iss.se.kafka.config;
//
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import nus.iss.se.kafka.event.DomainEvent;
//import nus.iss.se.kafka.listener.DomainEventListener;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.KafkaListenerConfigurer;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
//import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.lang.NonNull;
//import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
//
//import java.util.Set;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class KafkaDynamicListenerConfig implements KafkaListenerConfigurer {
//    @Value("${spring.kafka.consumer.group-id:magic-bag}")
//    private String groupId;
//
//    private final DomainEventListener domainEventListener;
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory(ConsumerFactory<String, DomainEvent> consumerFactory) {
//        // 支持反序列化
//        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(consumerFactory);
//        return factory;
//    }
//
//    // 动态注册Kafka监听端点
//    @Override
//    @SneakyThrows
//    public void configureKafkaListeners(@NonNull KafkaListenerEndpointRegistrar registrar) {
//        Set<String> registeredTopics = domainEventListener.getRegisteredTopics();
//        if (registeredTopics.isEmpty()){
//            // 如果微服务没有要监听的topic，就跳过
//            return;
//        }
//        MethodKafkaListenerEndpoint<String, DomainEvent> endpoint = new MethodKafkaListenerEndpoint<>();
//        endpoint.setId("dynamic-topic");
//        // 设置监听方法
//        endpoint.setMethod(DomainEventListener.class.getMethod("consume", DomainEvent.class));
//        // 设置监听的topics（从已注册的处理器中获取）
//        endpoint.setTopics(domainEventListener.getRegisteredTopics().toArray(new String[0]));
//        // 设置groupId
//        endpoint.setGroupId(groupId);
//        // 设置bean实例
//        endpoint.setBean(domainEventListener);
//
//        endpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
//        // 注册端点
//        registrar.registerEndpoint(endpoint);
//    }
//}
