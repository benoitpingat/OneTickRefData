package com.cmcmarkets.refdata.config;

import com.cmcmarkets.prophet.common.utils.httpmonitor.HttpPortMonitorService;
import com.cmcmarkets.refdata.main.RefDataService;
import com.cmcmarkets.refdata.service.RefDataRestServer;
import com.cmcmarkets.refdata.service.SwaggerHttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RefDataServiceConfigurationTest {

    @Mock
    HttpPortMonitorService httpPortMonitorService;

    @Mock
    RefDataRestServer refDataRestServer;

    RefDataService refDataService;

    @BeforeEach
    public void setUp() {

        refDataService = new RefDataService(httpPortMonitorService, refDataRestServer);
    }

    @Test
    public void startStopRefDayaService() throws Exception {
        refDataService.start();
        verifyBeansStarted();

        refDataService.stop();
        verifyBeansStopped();

    }


    private void verifyBeansStarted() throws Exception {
        Mockito.verify(httpPortMonitorService).init();
        Mockito.verify(refDataRestServer).start();
    }

    private void verifyBeansStopped() throws Exception {
        Mockito.verify(httpPortMonitorService).stop();
        Mockito.verify(refDataRestServer).stop();
    }
}