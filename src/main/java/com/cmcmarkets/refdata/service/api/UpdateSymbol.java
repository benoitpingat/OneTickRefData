package com.cmcmarkets.refdata.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateSymbol {

    @JsonProperty(value ="OLD_SYMBOL", required = true)
    private String oldSymbol;
    @JsonProperty(value ="NEW_SYMBOL", required = true)
    private String newSymbol;
    @JsonProperty("EFFECTIVE_DATE")
    private long effectiveDate;

    public String getOldSymbol() {
        return oldSymbol;
    }

    public void setOldSymbol(String oldSymbol) {
        this.oldSymbol = oldSymbol;
    }

    public String getNewSymbol() {
        return newSymbol;
    }

    public void setNewSymbol(String newSymbol) {
        this.newSymbol = newSymbol;
    }

    public long getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(long effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
