package com.cmcmarkets.refdata.service.onetick;

import com.cmcmarkets.prophet.messaging.common.DataContract;
import com.cmcmarkets.prophet.messaging.common.DataContractType;
import com.cmcmarkets.prophet.messaging.common.FieldValueType;
import com.cmcmarkets.prophet.messaging.common.imagecache.tibrv.TibrvMsgTypeToFieldValueType;

public enum ReferenceDataAudit implements DataContractType {
    RequestId(1, FieldValueType.STRING, true),
    RefDataUUID(2, FieldValueType.STRING, true),
    User(3, FieldValueType.STRING, true),
    Hostname(4, FieldValueType.STRING, true),
    RequestTimeStamp(5, FieldValueType.DATE_LONG, false),
    Endpoint(6, FieldValueType.STRING, false),
    RequestBody(7, FieldValueType.STRING, true),
    StartDateTime(20, FieldValueType.LONG, false),
    EndDateTime(21, FieldValueType.LONG, false),
    EffectiveDateTime(22, FieldValueType.LONG, true),
    Symbology(30, FieldValueType.STRING, false),
    MappedSymbolName(31, FieldValueType.STRING, false),
    MultiplicativeAdjustment(32, FieldValueType.FLOAT, false),
    AdditiveAdjustment(33, FieldValueType.FLOAT, false),
    AdjustmentTypeName(34, FieldValueType.STRING, false);


    int fid;
    FieldValueType fieldValueType;
    short type;
    int startArrayFid;
    boolean isMandatory;

    ReferenceDataAudit(int fid, FieldValueType type, boolean isMandatory) {
        this.fid = (short)fid;
        this.fieldValueType = type;
        this.startArrayFid = 0;
        this.type = TibrvMsgTypeToFieldValueType.map(type);
        this.isMandatory = isMandatory;
    }

    public int getFid() {
        return this.fid;
    }

    public short getType() {
        return this.type;
    }

    public FieldValueType getFieldValueType() {
        return this.fieldValueType;
    }

    public boolean isArray() {
        return this.fieldValueType.isArray();
    }

    public boolean isMandatory() {
        return this.isMandatory;
    }

    public int getArrayStartFid() {
        return this.startArrayFid;
    }

    public DataContract getUnderlyingDataContract() {
        return null;
    }
}
