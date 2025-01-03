package com.cmcmarkets.refdata.service.utils;

import com.cmcmarkets.prophet.messaging.common.imagecache.ValueEntryList;
import com.cmcmarkets.refdata.service.api.Intention;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Intention2ValueMapperTest {

    private final Intention2ValueMapper underTest = new Intention2ValueMapper(null);

    @Test
    public void testMapping(){

        Intention input = new Intention("user", "hotname", "endpoint", "requestBody");

        ThreadLocal<ValueEntryList<ReferenceDataAudit>> refDataValueEntryList = ThreadLocal.withInitial(ValueEntryList::getInstance);
        ValueEntryList<ReferenceDataAudit> valueEntryList = refDataValueEntryList.get();
        valueEntryList.reset();

        underTest.apply(input, valueEntryList);

        assertEquals(4, valueEntryList.getEntries().size());
        assertTrue(valueEntryList.getEntries().stream().anyMatch(p -> input.getUser().equals(p.getStringValue())));
        assertTrue(valueEntryList.getEntries().stream().anyMatch(p -> input.getHostname().equals(p.getStringValue())));
        assertTrue(valueEntryList.getEntries().stream().anyMatch(p -> input.getRequestBody().equals(p.getStringValue())));

    }

}