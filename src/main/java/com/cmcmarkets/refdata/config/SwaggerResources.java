package com.cmcmarkets.refdata.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.cmcmarkets.refdata.service")
public class SwaggerResources {
}

/*import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerResources extends ResourceConfig {
    public SwaggerResources() {
        packages("com.cmcmarkets.service"); // Replace with your resource package
    }
}*/
