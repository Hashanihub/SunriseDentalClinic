package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.AvailabilityStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dentist entity.
 */
public class Dentist {
    private int dentistId;
    private String dentistNumber;
    private String fullName;
    private String specialization;
    private String contactNumber;
    private String email;
    private BigDecimal consultationFee;
    private AvailabilityStatus availabilityStatus;
    private boolean active;
    private LocalDate joinedDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Dentist() {
        this.active = true;
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.consultationFee = BigDecimal.ZERO;
    }

    // Parameterized constructor
    public Dentist(int dentistId, String dentistNumber, String fullName, String specialization,
                   String contactNumber, String email, BigDecimal consultationFee,
                   AvailabilityStatus availabilityStatus, boolean active, LocalDate joinedDate,
                   String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.dentistId = dentistId;
        this.dentistNumber = dentistNumber;
        this.fullName = fullName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
        this.consultationFee = consultationFee;
        this.availabilityStatus = availabilityStatus;
        this.active = active;
        this.joinedDate = joinedDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Dentist dentist = new Dentist();

        public Builder dentistId(int dentistId) {
            dentist.dentistId = dentistId;
            return this;
        }

        public Builder dentistNumber(String dentistNumber) {
            dentist.dentistNumber = dentistNumber;
            return this;
        }

        public Builder fullName(String fullName) {
            dentist.fullName = fullName;
            return this;
        }

        public Builder specialization(String specialization) {
            dentist.specialization = specialization;
            return this;
        }

        public Builder contactNumber(String contactNumber) {
            dentist.contactNumber = contactNumber;
            return this;
        }

        public Builder email(String email) {
            dentist.email = email;
            return this;
        }

        public Builder consultationFee(BigDecimal consultationFee) {
            dentist.consultationFee = consultationFee;
            return this;
        }

        public Builder availabilityStatus(AvailabilityStatus availabilityStatus) {
            dentist.availabilityStatus = availabilityStatus;
            return this;
        }

        public Builder active(boolean active) {
            dentist.active = active;
            return this;
        }

        public Builder joinedDate(LocalDate joinedDate) {
            dentist.joinedDate = joinedDate;
            return this;
        }

        public Builder notes(String notes) {
            dentist.notes = notes;
            return this;
        }

        public Dentist build() {
            return dentist;
        }
    }

    // Getters and Setters
    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public String getDentistNumber() {
        return dentistNumber;
    }

    public void setDentistNumber(String dentistNumber) {
        this.dentistNumber = dentistNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public boolean isAvailable() {
        return active && availabilityStatus == AvailabilityStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Dentist{" +
                "dentistId=" + dentistId +
                ", dentistNumber='" + dentistNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", availabilityStatus=" + availabilityStatus +
                '}';
    }
}