package com.cmcmarkets.refdata.service.utils;

import com.cmcmarkets.prophet.messaging.common.imagecache.Updater;
import com.cmcmarkets.prophet.messaging.common.imagecache.ValueType;
import com.cmcmarkets.prophet.messaging.pricing.api.fxraw.FXRawDataContract;
import com.cmcmarkets.refdata.service.api.SymbolNameHistory;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class SymbolHistory2ValueMapper implements Updater<ReferenceDataAudit, SymbolNameHistory> {
    private HashSet<FXRawDataContract> fields = null;

    public SymbolHistory2ValueMapper(FXRawDataContract[] fields) {
        if (fields != null && fields.length > 0) {
            this.fields = new HashSet(Arrays.asList(fields));
        }

    }

    @Override
    public void apply(SymbolNameHistory symbol, ValueType<ReferenceDataAudit> cachedImage) {
        cachedImage.put(ReferenceDataAudit.RefDataUUID, symbol.getRefDataUuid());
        cachedImage.put(ReferenceDataAudit.RequestId, symbol.getTransactionId());


        if (this.isRequiredField(ReferenceDataAudit.MappedSymbolName) && Objects.nonNull(symbol.getSymbol())) {
            cachedImage.put(ReferenceDataAudit.MappedSymbolName, symbol.getSymbol());
        }
        if (this.isRequiredField(ReferenceDataAudit.StartDateTime)) {
            cachedImage.put(ReferenceDataAudit.StartDateTime, symbol.getStartDateTime());
        }
        if (this.isRequiredField(ReferenceDataAudit.EndDateTime)) {
            cachedImage.put(ReferenceDataAudit.EndDateTime, symbol.getEndDateTime());
        }
    }

    private boolean isRequiredField(ReferenceDataAudit action) {
        return action.isMandatory() || this.fields == null || this.fields.contains(action);
    }
}
