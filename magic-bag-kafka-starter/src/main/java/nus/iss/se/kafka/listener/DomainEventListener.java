//package nus.iss.se.kafka.listener;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import nus.iss.se.kafka.event.DomainEvent;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class DomainEventListener {
//    private final ApplicationContext applicationContext;
//    private final Map<String , Set<DomainEventHandler<?>>> handlers = new HashMap<>();
//
//    // 创建虚拟线程池（每个任务一个虚拟线程）
//    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
//
//    @PostConstruct
//    public void init() {
//        // 扫描所有标记了 @EventHandler 的 Spring Bean
//        Map<String, Object> beansWithAnnotation =
//                applicationContext.getBeansWithAnnotation(EventHandler.class);
//
//        beansWithAnnotation.forEach((beanName, bean) -> {
//            if (!(bean instanceof DomainEventHandler)) {
//                log.warn("{} is @EventHandler but the DomainEventHandler interface has not been implemented",beanName);
//                return;
//            }
//
//            EventHandler annotation = bean.getClass().getAnnotation(EventHandler.class);
//            if (!annotation.enabled()) {
//                // 跳过禁用的处理器
//                return;
//            }
//            String topic = annotation.topic();
//
//            @SuppressWarnings("unchecked")
//            DomainEventHandler<DomainEvent> handler = (DomainEventHandler<DomainEvent>) bean;
//
//            handlers.computeIfAbsent(topic, k -> new HashSet<>()).add(handler);
//            log.info("Auto register kafka event handler: {} -> {}", topic, bean.getClass().getSimpleName());
//        });
//
//        if (handlers.isEmpty()) {
//            log.warn("未找到任何 @EventHandler 处理器");
//        }
//    }
//
//    public void consume(DomainEvent event) {
//        if (event == null) {
//            return;
//        }
//        virtualThreadExecutor.submit(() -> handleEventAsync(event));
//    }
//
//    // 异步处理事件的具体逻辑
//    private void handleEventAsync(DomainEvent event) {
//        Set<DomainEventHandler<?>> eventHandlers = handlers.get(event.getTopic());
//        if (eventHandlers != null && !eventHandlers.isEmpty()){
//            eventHandlers.forEach(handler -> {
//                try {
//                    log.info("开始处理事件 [{}] {} -> {}", event.getMessageId(),
//                            event.getClass().getSimpleName(),
//                            event.getTopic());
//
//                    @SuppressWarnings("unchecked")
//                    DomainEventHandler<DomainEvent> typed = (DomainEventHandler<DomainEvent>) handler;
//                    typed.handle(event);
//                } catch (Exception e) {
//                    log.error("处理事件失败 [{}] {} -> {}: {}", event.getMessageId(),
//                            event.getClass().getSimpleName(), event.getTopic(), ExceptionUtils.getStackTrace(e)); // 打印异常堆栈
//                    // 重试或记录
//                }
//            });
//        }else {
//            log.info("无处理器匹配事件 [{}] {} -> {}", event.getMessageId(),
//                    event.getClass().getSimpleName(),
//                    event.getTopic());
//        }
//    }
//
//    @PreDestroy
//    public void destroy() {
//        virtualThreadExecutor.shutdown();
//    }
//
//    public Set<String> getRegisteredTopics() {
//        return handlers.keySet();
//    }
//}
