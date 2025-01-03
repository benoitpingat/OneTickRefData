package com.cmcmarkets.refdata.service.utils;

import com.cmcmarkets.prophet.messaging.common.imagecache.Updater;
import com.cmcmarkets.prophet.messaging.common.imagecache.ValueType;
import com.cmcmarkets.prophet.messaging.pricing.api.fxraw.FXRawDataContract;
import com.cmcmarkets.refdata.service.api.Intention;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;

import java.util.Arrays;
import java.util.HashSet;

public class Intention2ValueMapper implements Updater<ReferenceDataAudit, Intention> {
    private HashSet<FXRawDataContract> fields = null;

    public Intention2ValueMapper(FXRawDataContract[] fields) {
        if (fields != null && fields.length > 0) {
            this.fields = new HashSet(Arrays.asList(fields));
        }

    }

    @Override
    public void apply(Intention intention, ValueType<ReferenceDataAudit> cachedImage) {

        if (this.isRequiredField(ReferenceDataAudit.User)) {
            cachedImage.put(ReferenceDataAudit.User, intention.getUser());
        }
        if (this.isRequiredField(ReferenceDataAudit.Hostname)) {
            cachedImage.put(ReferenceDataAudit.Hostname, intention.getHostname());
        }
        if (this.isRequiredField(ReferenceDataAudit.Endpoint)) {
            cachedImage.put(ReferenceDataAudit.Endpoint, intention.getEndPoint());
        }
        if (this.isRequiredField(ReferenceDataAudit.RequestBody)) {
            cachedImage.put(ReferenceDataAudit.RequestBody, intention.getRequestBody());
        }



    }

    private boolean isRequiredField(ReferenceDataAudit action) {
        return action.isMandatory() || this.fields == null || this.fields.contains(action);
    }
}
