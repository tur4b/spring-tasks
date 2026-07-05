package org.example.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication(scanBasePackages = {"org.example.workload", "org.example.common"})
@EnableDiscoveryClient
@EnableJms
public class TrainerWorkloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerWorkloadApplication.class, args);
    }
}
