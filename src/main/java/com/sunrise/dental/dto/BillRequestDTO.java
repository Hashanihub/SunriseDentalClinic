package com.sunrise.dental.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for bill generation requests.
 */
public class BillRequestDTO {

    private Integer appointmentId;
    private BigDecimal discount;
    private BigDecimal tax;
    private Integer createdBy;

    public BillRequestDTO() {
        this.discount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }
}