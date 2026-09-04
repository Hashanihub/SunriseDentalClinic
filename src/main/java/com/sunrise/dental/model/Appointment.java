package com.sunrise.dental.model;

import com.sunrise.dental.model.enums.AppointmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment entity.
 */
public class Appointment {
    private int appointmentId;
    private String appointmentNumber;
    private int patientId;
    private int dentistId;
    private int treatmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;
    private String cancellationReason;
    private Integer rescheduledFrom;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Patient, Dentist, Treatment details (for display purposes)
    private Patient patient;
    private Dentist dentist;
    private Treatment treatment;

    // Default constructor
    public Appointment() {
        this.status = AppointmentStatus.SCHEDULED;
    }

    // Parameterized constructor
    public Appointment(int appointmentId, String appointmentNumber, int patientId, int dentistId,
                       int treatmentId, LocalDate appointmentDate, LocalTime appointmentTime,
                       AppointmentStatus status, String notes, String cancellationReason,
                       Integer rescheduledFrom, Integer createdBy,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.appointmentId = appointmentId;
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
        this.cancellationReason = cancellationReason;
        this.rescheduledFrom = rescheduledFrom;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder
    public static class Builder {
        private Appointment appointment = new Appointment();

        public Builder appointmentId(int appointmentId) {
            appointment.appointmentId = appointmentId;
            return this;
        }

        public Builder appointmentNumber(String appointmentNumber) {
            appointment.appointmentNumber = appointmentNumber;
            return this;
        }

        public Builder patientId(int patientId) {
            appointment.patientId = patientId;
            return this;
        }

        public Builder dentistId(int dentistId) {
            appointment.dentistId = dentistId;
            return this;
        }

        public Builder treatmentId(int treatmentId) {
            appointment.treatmentId = treatmentId;
            return this;
        }

        public Builder appointmentDate(LocalDate appointmentDate) {
            appointment.appointmentDate = appointmentDate;
            return this;
        }

        public Builder appointmentTime(LocalTime appointmentTime) {
            appointment.appointmentTime = appointmentTime;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            appointment.status = status;
            return this;
        }

        public Builder notes(String notes) {
            appointment.notes = notes;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            appointment.cancellationReason = cancellationReason;
            return this;
        }

        public Builder rescheduledFrom(Integer rescheduledFrom) {
            appointment.rescheduledFrom = rescheduledFrom;
            return this;
        }

        public Builder createdBy(Integer createdBy) {
            appointment.createdBy = createdBy;
            return this;
        }

        public Builder patient(Patient patient) {
            appointment.patient = patient;
            return this;
        }

        public Builder dentist(Dentist dentist) {
            appointment.dentist = dentist;
            return this;
        }

        public Builder treatment(Treatment treatment) {
            appointment.treatment = treatment;
            return this;
        }

        public Appointment build() {
            return appointment;
        }
    }

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Integer getRescheduledFrom() {
        return rescheduledFrom;
    }

    public void setRescheduledFrom(Integer rescheduledFrom) {
        this.rescheduledFrom = rescheduledFrom;
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

    public Dentist getDentist() {
        return dentist;
    }

    public void setDentist(Dentist dentist) {
        this.dentist = dentist;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED && status != AppointmentStatus.NO_SHOW;
    }

    public boolean isPast() {
        return appointmentDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", appointmentNumber='" + appointmentNumber + '\'' +
                ", patientId=" + patientId +
                ", dentistId=" + dentistId +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", status=" + status +
                '}';
    }
}