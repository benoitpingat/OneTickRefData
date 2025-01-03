package com.cmcmarkets.refdata.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean(name = "swaggerConfig")
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OneTick RefData Service")
                        .version("1.0")
                        .description("API for processing items"));
    }
}


/*import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@Configuration
public class SwaggerConfig extends ServletContainer {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        OpenAPI oas = new OpenAPI().info(new Info()
                .title("OneTick RefData Service")
                .description("Your API Description")
                .version("1.0.0"));

        // Create SwaggerConfiguration instance and set OpenAPI
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true);

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .servletConfig(config)
                    .application(null) // Replace with your JAX-RS Application class if you have one
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}*/
