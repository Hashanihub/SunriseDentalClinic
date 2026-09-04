package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.Gender;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient entity.
 */
public class Patient {
    private int patientId;
    private String patientNumber;
    private String fullName;
    private String address;
    private String contactNumber;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDate registrationDate;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Patient() {
        this.active = true;
        this.registrationDate = LocalDate.now();
    }

    // Parameterized constructor
    public Patient(int patientId, String patientNumber, String fullName, String address,
                   String contactNumber, String email, LocalDate dateOfBirth, Gender gender,
                   LocalDate registrationDate, boolean active, String notes,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.patientId = patientId;
        this.patientNumber = patientNumber;
        this.fullName = fullName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.registrationDate = registrationDate;
        this.active = active;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Patient patient = new Patient();

        public Builder patientId(int patientId) {
            patient.patientId = patientId;
            return this;
        }

        public Builder patientNumber(String patientNumber) {
            patient.patientNumber = patientNumber;
            return this;
        }

        public Builder fullName(String fullName) {
            patient.fullName = fullName;
            return this;
        }

        public Builder address(String address) {
            patient.address = address;
            return this;
        }

        public Builder contactNumber(String contactNumber) {
            patient.contactNumber = contactNumber;
            return this;
        }

        public Builder email(String email) {
            patient.email = email;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            patient.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(Gender gender) {
            patient.gender = gender;
            return this;
        }

        public Builder registrationDate(LocalDate registrationDate) {
            patient.registrationDate = registrationDate;
            return this;
        }

        public Builder active(boolean active) {
            patient.active = active;
            return this;
        }

        public Builder notes(String notes) {
            patient.notes = notes;
            return this;
        }

        public Patient build() {
            return patient;
        }
    }

    // Getters and Setters
    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", patientNumber='" + patientNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", gender=" + gender +
                '}';
    }
}