package com.sunrise.dental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Treatment entity.
 */
public class Treatment {
    private int treatmentId;
    private String treatmentCode;
    private String treatmentName;
    private String description;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Treatment() {
        this.active = true;
        this.treatmentCost = BigDecimal.ZERO;
        this.consultationFee = BigDecimal.ZERO;
    }

    // Parameterized constructor
    public Treatment(int treatmentId, String treatmentCode, String treatmentName,
                     String description, BigDecimal treatmentCost, BigDecimal consultationFee,
                     boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.treatmentId = treatmentId;
        this.treatmentCode = treatmentCode;
        this.treatmentName = treatmentName;
        this.description = description;
        this.treatmentCost = treatmentCost;
        this.consultationFee = consultationFee;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Treatment treatment = new Treatment();

        public Builder treatmentId(int treatmentId) {
            treatment.treatmentId = treatmentId;
            return this;
        }

        public Builder treatmentCode(String treatmentCode) {
            treatment.treatmentCode = treatmentCode;
            return this;
        }

        public Builder treatmentName(String treatmentName) {
            treatment.treatmentName = treatmentName;
            return this;
        }

        public Builder description(String description) {
            treatment.description = description;
            return this;
        }

        public Builder treatmentCost(BigDecimal treatmentCost) {
            treatment.treatmentCost = treatmentCost;
            return this;
        }

        public Builder consultationFee(BigDecimal consultationFee) {
            treatment.consultationFee = consultationFee;
            return this;
        }

        public Builder active(boolean active) {
            treatment.active = active;
            return this;
        }

        public Treatment build() {
            return treatment;
        }
    }

    // Getters and Setters
    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getTreatmentCode() {
        return treatmentCode;
    }

    public void setTreatmentCode(String treatmentCode) {
        this.treatmentCode = treatmentCode;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(BigDecimal treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BigDecimal getTotalCost() {
        return treatmentCost.add(consultationFee);
    }

    @Override
    public String toString() {
        return "Treatment{" +
                "treatmentId=" + treatmentId +
                ", treatmentCode='" + treatmentCode + '\'' +
                ", treatmentName='" + treatmentName + '\'' +
                ", treatmentCost=" + treatmentCost +
                ", consultationFee=" + consultationFee +
                '}';
    }
}