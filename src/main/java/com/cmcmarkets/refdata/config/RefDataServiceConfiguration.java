package com.cmcmarkets.refdata.config;

import com.cmcmarkets.framework.service.config.spring.QueryableEncryptablePropertyPlaceholderConfigurer;
import com.cmcmarkets.prophet.common.utils.httpmonitor.HttpPortMonitorService;
import com.cmcmarkets.prophet.pricing.config.HttpMonitorServiceConfiguration;
import com.cmcmarkets.refdata.main.RefDataService;
import com.cmcmarkets.refdata.service.OneTickRefData;
import com.cmcmarkets.refdata.service.RefDataRestServer;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ImportResource({"classpath:service-framework.xml"})
@Import({HttpMonitorServiceConfiguration.class,
        OnetickConfiguration.class})
public class RefDataServiceConfiguration {

    @Bean(name = "propertiesPriority")
    public QueryableEncryptablePropertyPlaceholderConfigurer properties(@Qualifier("propertyEncryptor") StandardPBEStringEncryptor propertyEncryptor) {
        QueryableEncryptablePropertyPlaceholderConfigurer queryableEncryptablePropertyPlaceholderConfigurer = new QueryableEncryptablePropertyPlaceholderConfigurer(propertyEncryptor);
        queryableEncryptablePropertyPlaceholderConfigurer.setIgnoreResourceNotFound(false);
        queryableEncryptablePropertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        queryableEncryptablePropertyPlaceholderConfigurer.setLocations(
                new ClassPathResource("refDataService-application.properties"),
                new ClassPathResource("onetick.properties"),
                new ClassPathResource("version.properties"));
        return queryableEncryptablePropertyPlaceholderConfigurer;
    }

    @Bean(name = "httpRestServer")
    public RefDataRestServer httpRestServer(@Value("${RestHttpPort}") int restHttpPort,
                                            @Qualifier("oneTickRefData") OneTickRefData oneTickRefData) {
        return new RefDataRestServer(restHttpPort, oneTickRefData);
    }

    @Bean(name = "refDataService")
    public RefDataService refDataService(  HttpPortMonitorService httpPortMonitorService,
                                           @Qualifier("httpRestServer") RefDataRestServer restServer) {

        return new RefDataService(httpPortMonitorService, restServer);
    }

}