package com.cmcmarkets.refdata.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorporateAction {
        @JsonProperty("PMS_CODE")
        private String pmsCode;
        @JsonProperty("EFFECTIVE_DATE")
        private long effectiveDate;
        @JsonProperty("MULTIPLICATIVE_ADJUSTMENT")
        private double multiplicativeAdjustment;
        @JsonProperty("ADDITIVE_ADJUSTMENT")
        private int additiveAdjustment;
        @JsonProperty("ADJUSTMENT_TYPE_NAME")
        private String adjustmentTypeName;

        // Getters and Setters
        public String getPmsCode() {
            return pmsCode;
        }

        public void setPmsCode(String pmsCode) {
            this.pmsCode = pmsCode;
        }

        public long getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(long effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        public double getMultiplicativeAdjustment() {
            return multiplicativeAdjustment;
        }

        public void setMultiplicativeAdjustment(double multiplicativeAdjustment) {
            this.multiplicativeAdjustment = multiplicativeAdjustment;
        }

        public int getAdditiveAdjustment() {
            return additiveAdjustment;
        }

        public void setAdditiveAdjustment(int additiveAdjustment) {
            this.additiveAdjustment = additiveAdjustment;
        }

        public String getAdjustmentTypeName() {
            return adjustmentTypeName;
        }

        public void setAdjustmentTypeName(String adjustmentTypeName) {
            this.adjustmentTypeName = adjustmentTypeName;
        }

    @Override
    public String toString() {
        return "CorporateAction{" +
                "pmsCode='" + pmsCode + '\'' +
                ", effectiveDate=" + effectiveDate +
                ", multiplicativeAdjustment=" + multiplicativeAdjustment +
                ", additiveAdjustment=" + additiveAdjustment +
                ", adjustmentTypeName='" + adjustmentTypeName + '\'' +
                '}';
    }
}
