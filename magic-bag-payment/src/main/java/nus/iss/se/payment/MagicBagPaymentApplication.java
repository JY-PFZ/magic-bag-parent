package nus.iss.se.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {"nus.iss.se.payment", "nus.iss.se.common"})
@EnableFeignClients(basePackages = "nus.iss.se.payment.api")
@EnableDiscoveryClient
@EnableConfigurationProperties
public class MagicBagPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicBagPaymentApplication.class, args);
    }

}
