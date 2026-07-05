package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"org.example", "org.example.common"})
@EnableDiscoveryClient
@EnableJms
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableAsync
public class TaskSpringCoreDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskSpringCoreDemoApplication.class, args);
    }
}
