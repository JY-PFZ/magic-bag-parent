package nus.iss.se.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {"nus.iss.se.cart", "nus.iss.se.common"})
@EnableFeignClients(basePackages = "nus.iss.se.cart.api")
@EnableDiscoveryClient
@EnableConfigurationProperties
public class MagicBagCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicBagCartApplication.class, args);
    }

}
