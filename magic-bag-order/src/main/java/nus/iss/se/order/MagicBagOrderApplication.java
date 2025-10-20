package nus.iss.se.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {"nus.iss.se.order", "nus.iss.se.common"})
@EnableFeignClients(basePackages = "nus.iss.se.order.api")
@EnableDiscoveryClient
@EnableConfigurationProperties
public class MagicBagOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicBagOrderApplication.class, args);
    }

}
