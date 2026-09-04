package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Bill entity.
 */
public class Bill {
    private int billId;
    private String billNumber;
    private int appointmentId;
    private int patientId;
    private BigDecimal treatmentCost;
    private BigDecimal consultationFee;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private BigDecimal paidAmount;
    private LocalDate billDate;
    private String notes;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related objects
    private Patient patient;
    private Appointment appointment;
    private Treatment treatment;

    // Default constructor
    public Bill() {
        this.paymentStatus = PaymentStatus.UNPAID;
        this.discount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.treatmentCost = BigDecimal.ZERO;
        this.consultationFee = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.billDate = LocalDate.now();
    }

    // Parameterized constructor
    public Bill(int billId, String billNumber, int appointmentId, int patientId,
                BigDecimal treatmentCost, BigDecimal consultationFee, BigDecimal subtotal,
                BigDecimal discount, BigDecimal tax, BigDecimal totalAmount,
                PaymentStatus paymentStatus, BigDecimal paidAmount, LocalDate billDate,
                String notes, Integer createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.billId = billId;
        this.billNumber = billNumber;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.treatmentCost = treatmentCost;
        this.consultationFee = consultationFee;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paidAmount = paidAmount;
        this.billDate = billDate;
        this.notes = notes;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Bill bill = new Bill();

        public Builder billId(int billId) {
            bill.billId = billId;
            return this;
        }

        public Builder billNumber(String billNumber) {
            bill.billNumber = billNumber;
            return this;
        }

        public Builder appointmentId(int appointmentId) {
            bill.appointmentId = appointmentId;
            return this;
        }

        public Builder patientId(int patientId) {
            bill.patientId = patientId;
            return this;
        }

        public Builder treatmentCost(BigDecimal treatmentCost) {
            bill.treatmentCost = treatmentCost;
            return this;
        }

        public Builder consultationFee(BigDecimal consultationFee) {
            bill.consultationFee = consultationFee;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            bill.subtotal = subtotal;
            return this;
        }

        public Builder discount(BigDecimal discount) {
            bill.discount = discount;
            return this;
        }

        public Builder tax(BigDecimal tax) {
            bill.tax = tax;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            bill.totalAmount = totalAmount;
            return this;
        }

        public Builder paymentStatus(PaymentStatus paymentStatus) {
            bill.paymentStatus = paymentStatus;
            return this;
        }

        public Builder paidAmount(BigDecimal paidAmount) {
            bill.paidAmount = paidAmount;
            return this;
        }

        public Builder billDate(LocalDate billDate) {
            bill.billDate = billDate;
            return this;
        }

        public Builder notes(String notes) {
            bill.notes = notes;
            return this;
        }

        public Builder createdBy(Integer createdBy) {
            bill.createdBy = createdBy;
            return this;
        }

        public Builder patient(Patient patient) {
            bill.patient = patient;
            return this;
        }

        public Builder appointment(Appointment appointment) {
            bill.appointment = appointment;
            return this;
        }

        public Builder treatment(Treatment treatment) {
            bill.treatment = treatment;
            return this;
        }

        public Bill build() {
            // Auto-calculate if not set
            if (bill.subtotal.compareTo(BigDecimal.ZERO) == 0) {
                bill.subtotal = bill.treatmentCost.add(bill.consultationFee);
            }
            if (bill.totalAmount.compareTo(BigDecimal.ZERO) == 0) {
                bill.totalAmount = bill.subtotal.subtract(bill.discount).add(bill.tax);
            }
            return bill;
        }
    }

    // Getters and Setters
    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(paidAmount);
    }

    public boolean isFullyPaid() {
        return paymentStatus == PaymentStatus.PAID || getBalanceDue().compareTo(BigDecimal.ZERO) <= 0;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "billId=" + billId +
                ", billNumber='" + billNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}