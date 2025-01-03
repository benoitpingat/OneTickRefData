package com.cmcmarkets.refdata.config;

import com.cmcmarkets.prophet.common.onetick.Config;
import com.cmcmarkets.prophet.common.onetick.DummyOneTickRecorder;
import com.cmcmarkets.prophet.common.onetick.GenericOneTickRecorder;
import com.cmcmarkets.prophet.common.onetick.OneTickRecorder;
import com.cmcmarkets.prophet.messaging.common.imagecache.ValueEntryList;
import com.cmcmarkets.refdata.service.OneTickRefData;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;
import com.omd.jomd.timeval_t;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;

@Configuration
@PropertySource("onetick.properties")
@ImportResource("onetickInstanceToMountMap.xml")
public class OnetickConfiguration {

    @Autowired
    @Qualifier("inst2OneTickMountMap")
    private Map<String, String> oneTickMountMap;


    @Bean(name = "refAuditConfig")
    Config corpActConfig(@Value("${instance.name}") String instanceName,
                         @Value("${all.oneTickContext}") String oneTickContext,
                         @Value("${all.oneTickDBName}") String oneTickDBName,
                         @Value("${all.oneTickLocation}") String oneTickLocation,
                         @Value("${all.oneTickConfig}") String oneTickConfigFile,
                         @Value("${all.writeToOneTick:true}") boolean oneTickWriterEnabled,
                         @Value("${all.oneTickWriterThreadCount}") int oneTickWriterThreadCount,
                         @Value("${all.oneTickisFTAware}") boolean oneTickIsFTAware) {
        return new Config(instanceName, oneTickContext, oneTickDBName, oneTickMountMap, oneTickLocation,
                oneTickConfigFile, oneTickWriterEnabled, oneTickWriterThreadCount, oneTickIsFTAware);
    }

    @Bean(name = "refDataAuditRecorder")
    @Lazy
    OneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>> onetickRecorder(@Qualifier("refAuditConfig") Config config) {
        return new DummyOneTickRecorder<>();
        //return new GenericOneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>>(config, null, ReferenceDataAudit.class);
    }


    @Bean(name = "oneTickRefData")
    public OneTickRefData oneTickRefData(@Value("classpath:${Onetick.UUIDOtq}") Resource uuidLookUpQueryResource,
                                         @Value("classpath:${Onetick.test}") Resource quoteHistoryQueryResource,
                                         @Qualifier("refDataAuditRecorder") OneTickRecorder<ReferenceDataAudit, ValueEntryList<ReferenceDataAudit>> refDataAuditRecorder) throws IOException {
        return new OneTickRefData(uuidLookUpQueryResource, quoteHistoryQueryResource, refDataAuditRecorder, /*new timeval_t()*/ null);
    }


}
