package com.cmcmarkets.refdata.main;

import com.cmcmarkets.prophet.common.StartStoppable;
import com.cmcmarkets.prophet.common.utils.httpmonitor.HttpPortMonitorService;
import com.cmcmarkets.refdata.config.RefDataServiceConfiguration;
import com.cmcmarkets.refdata.service.RefDataRestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.cmcmarkets.prophet.common.utils.SpringPropertiesHelper.getProperties;

public class RefDataService implements StartStoppable {
    private static final Logger LOG = LoggerFactory.getLogger(RefDataService.class);

    private final HttpPortMonitorService httpPortMonitorService;

    private final RefDataRestServer refDataRestServer;

    public RefDataService(HttpPortMonitorService httpPortMonitorService,
                          RefDataRestServer refDataRestServer) {

        this.httpPortMonitorService = httpPortMonitorService;
        this.refDataRestServer = refDataRestServer;
    }

    @Override
    public void start() throws Exception {

        LOG.info("Starting RefData Service Monitoring Http Server");
        httpPortMonitorService.init();

        LOG.info("Starting RefData Service Rest Http Server");
        refDataRestServer.start();


    }


    @Override
    public void stop() {

        LOG.info("Stopping RefData Service Monitoring Http Server");
        httpPortMonitorService.stop();

        LOG.info("Stopping RefData Service Rest Http Server");
        refDataRestServer.stop();

    }

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RefDataServiceConfiguration.class);
        ConfigurableEnvironment environment = context.getEnvironment();
        LOG.info("RefDataService Properties: \n" + getProperties(environment));
        RefDataService refDataService = (RefDataService) context.getBean("refDataService");
        refDataService.start();
    }

}
