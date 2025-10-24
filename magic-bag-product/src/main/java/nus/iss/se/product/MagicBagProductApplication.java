package nus.iss.se.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {"nus.iss.se.product", "nus.iss.se.common"})
@EnableFeignClients(basePackages = "nus.iss.se.product.api")
@EnableDiscoveryClient
@EnableConfigurationProperties
public class MagicBagProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicBagProductApplication.class, args);
    }

}
