package nus.iss.se.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableConfigurationProperties
@ComponentScan("nus.iss.se.*")
public class MagicBagAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicBagAuthApplication.class, args);
    }

}
