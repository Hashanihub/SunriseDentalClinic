package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity.
 */
public class Payment {
    private int paymentId;
    private String paymentNumber;
    private int billId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String notes;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related objects
    private Bill bill;

    // Default constructor
    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = PaymentMethod.CASH;
        this.amount = BigDecimal.ZERO;
    }

    // Parameterized constructor
    public Payment(int paymentId, String paymentNumber, int billId, BigDecimal amount,
                   LocalDateTime paymentDate, PaymentMethod paymentMethod,
                   String referenceNumber, String notes, Integer createdBy,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.paymentId = paymentId;
        this.paymentNumber = paymentNumber;
        this.billId = billId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Payment payment = new Payment();

        public Builder paymentId(int paymentId) {
            payment.paymentId = paymentId;
            return this;
        }

        public Builder paymentNumber(String paymentNumber) {
            payment.paymentNumber = paymentNumber;
            return this;
        }

        public Builder billId(int billId) {
            payment.billId = billId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            payment.amount = amount;
            return this;
        }

        public Builder paymentDate(LocalDateTime paymentDate) {
            payment.paymentDate = paymentDate;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            payment.paymentMethod = paymentMethod;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            payment.referenceNumber = referenceNumber;
            return this;
        }

        public Builder notes(String notes) {
            payment.notes = notes;
            return this;
        }

        public Builder createdBy(Integer createdBy) {
            payment.createdBy = createdBy;
            return this;
        }

        public Builder bill(Bill bill) {
            payment.bill = bill;
            return this;
        }

        public Payment build() {
            return payment;
        }
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
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

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", paymentNumber='" + paymentNumber + '\'' +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}