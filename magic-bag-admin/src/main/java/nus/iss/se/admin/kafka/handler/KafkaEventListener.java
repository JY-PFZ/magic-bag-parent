package nus.iss.se.admin.kafka.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import nus.iss.se.admin.constant.TaskStatus;
import nus.iss.se.admin.constant.TaskType;
import nus.iss.se.admin.entity.AdminTask;
import nus.iss.se.admin.kafka.EventTopicType;
import nus.iss.se.admin.kafka.event.MerchantRegisterEvent;
import nus.iss.se.admin.service.IAdminTaskService;
import nus.iss.se.kafka.event.EventEnvelope;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final ObjectMapper objectMapper;
    private final IAdminTaskService adminTaskService;

    @KafkaListener(topics = EventTopicType.MERCHANT_REGISTERED)
    public void consume(EventEnvelope event) throws MessagingException {
        try {
            AdminTask task = new AdminTask();

            MerchantRegisterEvent registeredEvent = objectMapper.readValue(event.data(), MerchantRegisterEvent.class);
            task.setData(event.data());
            task.setApplicant(registeredEvent.getUserId());
            task.setTitle("Merchant registration review - " + registeredEvent.getShopName());

            task.setType(TaskType.MERCHANT_APPROVAL.getCode());
            task.setStatus(TaskStatus.PENDING.getCode());
            task.setStartTime(new Date());

            adminTaskService.save(task);
        } catch (JsonProcessingException e) {
            log.error("创建商家注册代办任务失败：Json解析失败：{}", ExceptionUtils.getStackTrace(e));
        }
    }
}
