package com.cmcmarkets.refdata.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SymbolNameHistory {

    @JsonProperty("TRANSACTION_ID")
    private String transactionId;
    @JsonProperty("REF_DATA_UUID")
    private String refDataUuid;
    @JsonProperty("START_DATETIME")
    private long startDateTime;
    @JsonProperty(value = "END_DATETIME", required = false)
    private long endDateTime;
    @JsonProperty("SYMBOL")
    private String symbol;

    public SymbolNameHistory() {
    }

    public SymbolNameHistory(String refDataUuid, String symbol) {
        this.refDataUuid = refDataUuid;
        this.symbol = symbol;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRefDataUuid() {
        return refDataUuid;
    }

    public void setRefDataUuid(String refDataUuid) {
        this.refDataUuid = refDataUuid;
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "SymbolNameHistory{" +
                "transactionId='" + transactionId + '\'' +
                ", refDataUuid='" + refDataUuid + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
