package org.example.testsupport;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Service-layer test slice.
 */
@Configuration
@Import({ DataConfig.class })
@ComponentScan(basePackages = {
        "org.example.service",
        "org.example.mapper",
        "org.example.aspect"
})
@EnableAspectJAutoProxy
public class ServiceLayerConfig {


}
