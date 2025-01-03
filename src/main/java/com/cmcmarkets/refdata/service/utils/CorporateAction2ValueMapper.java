package com.cmcmarkets.refdata.service.utils;

import com.cmcmarkets.prophet.messaging.common.imagecache.Updater;
import com.cmcmarkets.prophet.messaging.common.imagecache.ValueType;
import com.cmcmarkets.prophet.messaging.pricing.api.fxraw.FXRawDataContract;
import com.cmcmarkets.refdata.service.api.CorporateAction;
import com.cmcmarkets.refdata.service.onetick.ReferenceDataAudit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

public class CorporateAction2ValueMapper implements Updater<ReferenceDataAudit, CorporateAction> {
    private HashSet<FXRawDataContract> fields = null;
    private static final Logger LOG = LoggerFactory.getLogger(CorporateAction2ValueMapper.class);

    public CorporateAction2ValueMapper(FXRawDataContract[] fields) {
        if (fields != null && fields.length > 0) {
            this.fields = new HashSet(Arrays.asList(fields));
        }

    }

    @Override
    public void apply(CorporateAction corporateAction, ValueType<ReferenceDataAudit> cachedImage) {

        try {
            if (this.isRequiredField(ReferenceDataAudit.AdditiveAdjustment)) {
                cachedImage.put(ReferenceDataAudit.AdditiveAdjustment, corporateAction.getAdditiveAdjustment());
            }
            if (this.isRequiredField(ReferenceDataAudit.EffectiveDateTime)) {
                cachedImage.put(ReferenceDataAudit.EffectiveDateTime, corporateAction.getEffectiveDate());
            }
            if (this.isRequiredField(ReferenceDataAudit.MultiplicativeAdjustment)) {
                cachedImage.put(ReferenceDataAudit.MultiplicativeAdjustment, corporateAction.getMultiplicativeAdjustment());
            }
            if (this.isRequiredField(ReferenceDataAudit.AdjustmentTypeName)) {
                cachedImage.put(ReferenceDataAudit.AdjustmentTypeName, corporateAction.getAdjustmentTypeName());
            }
        } catch (Exception e) {
            LOG.error("Mapping Exception :", e);
            throw new RuntimeException(e);
        }
    }

    private boolean isRequiredField(ReferenceDataAudit contract) {
        return contract.isMandatory() || this.fields == null || this.fields.contains(contract);
    }
}
