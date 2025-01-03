package com.cmcmarkets.refdata.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DelistSymbol {


    @JsonProperty(value = "SYMBOL", required = true)
    private String symbol;
    @JsonProperty("EFFECTIVE_DATE")
    private long effectiveDate;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(long effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
