package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.DentistDAO;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.dto.AppointmentRequestDTO;
import com.sunrise.dental.exception.AppointmentConflictException;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.model.enums.AppointmentStatus;
import com.sunrise.dental.service.AppointmentService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of AppointmentService interface.
 */
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger =
            LogManager.getLogger(AppointmentServiceImpl.class);

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;

    /**
     * Constructor injection.
     * This allows Mockito to inject mock DAO objects during unit testing.
     */
    public AppointmentServiceImpl() {
        this.appointmentDAO = new com.sunrise.dental.dao.Impl.AppointmentDAOImpl();
        this.patientDAO = new com.sunrise.dental.dao.Impl.PatientDAOImpl();
        this.dentistDAO = new com.sunrise.dental.dao.Impl.DentistDAOImpl();
        this.treatmentDAO = new com.sunrise.dental.dao.Impl.TreatmentDAOImpl();
    }

    public AppointmentServiceImpl(
            AppointmentDAO appointmentDAO,
            PatientDAO patientDAO,
            DentistDAO dentistDAO,
            TreatmentDAO treatmentDAO) {

        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
    }

    @Override
    public Appointment createAppointment(AppointmentRequestDTO request)
            throws ValidationException,
            AppointmentConflictException,
            ResourceNotFoundException,
            DatabaseException {

        logger.info(
                "Creating appointment for patient: {}",
                request.getPatientNumber()
        );

        validateAppointmentRequest(request);

        Optional<Patient> patientOptional =
                patientDAO.findByPatientNumber(request.getPatientNumber());

        if (patientOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Patient",
                    request.getPatientNumber()
            );
        }

        Patient patient = patientOptional.get();

        Optional<Dentist> dentistOptional =
                dentistDAO.findById(request.getDentistId());

        if (dentistOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Dentist",
                    String.valueOf(request.getDentistId())
            );
        }

        Dentist dentist = dentistOptional.get();

        if (!dentist.isAvailable()) {
            throw new ValidationException(
                    "Dentist is not available"
            );
        }

        Optional<Treatment> treatmentOptional =
                treatmentDAO.findById(request.getTreatmentId());

        if (treatmentOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Treatment",
                    String.valueOf(request.getTreatmentId())
            );
        }

        boolean available = appointmentDAO.isDentistAvailable(
                request.getDentistId(),
                request.getAppointmentDate(),
                request.getAppointmentTime().toString()
        );

        if (!available) {
            throw new AppointmentConflictException(
                    dentist.getFullName(),
                    request.getAppointmentDate()
                            + " at "
                            + request.getAppointmentTime()
            );
        }

        Appointment appointment = new Appointment();

        appointment.setPatientId(patient.getPatientId());
        appointment.setDentistId(request.getDentistId());
        appointment.setTreatmentId(request.getTreatmentId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.getNotes());
        appointment.setCreatedBy(request.getCreatedBy());

        appointment.setPatient(patient);
        appointment.setDentist(dentist);
        appointment.setTreatment(treatmentOptional.get());

        Appointment saved = appointmentDAO.save(appointment);

        logger.info(
                "Appointment created successfully: {}",
                saved.getAppointmentNumber()
        );

        return saved;
    }

    @Override
    public Appointment getAppointmentById(int appointmentId)
            throws ResourceNotFoundException, DatabaseException {

        Optional<Appointment> appointment =
                appointmentDAO.findById(appointmentId);

        if (appointment.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    String.valueOf(appointmentId)
            );
        }

        return enrichAppointmentWithDetails(appointment.get());
    }

    @Override
    public Appointment getAppointmentByNumber(String appointmentNumber)
            throws ResourceNotFoundException, DatabaseException {

        try {
            ValidationUtil.validateAppointmentNumber(appointmentNumber);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }

        Optional<Appointment> appointment =
                appointmentDAO.findByAppointmentNumber(appointmentNumber);

        if (appointment.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    appointmentNumber
            );
        }

        return enrichAppointmentWithDetails(appointment.get());
    }

    @Override
    public List<Appointment> getAppointmentsByPatientId(int patientId)
            throws DatabaseException {

        return enrichAppointmentsWithDetails(
                appointmentDAO.findByPatientId(patientId)
        );
    }

    @Override
    public List<Appointment> getAppointmentsByDentistId(int dentistId)
            throws DatabaseException {

        return enrichAppointmentsWithDetails(
                appointmentDAO.findByDentistId(dentistId)
        );
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date)
            throws DatabaseException {

        return enrichAppointmentsWithDetails(
                appointmentDAO.findByDate(date)
        );
    }

    @Override
    public List<Appointment> getAppointmentsByDateRange(
            LocalDate startDate,
            LocalDate endDate)
            throws DatabaseException {

        try {
            ValidationUtil.validateDateRange(startDate, endDate);
        } catch (ValidationException e) {
            throw new DatabaseException(e.getMessage());
        }

        return enrichAppointmentsWithDetails(
                appointmentDAO.findByDateRange(startDate, endDate)
        );
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(
            AppointmentStatus status)
            throws DatabaseException {

        return enrichAppointmentsWithDetails(
                appointmentDAO.findByStatus(status)
        );
    }

    @Override
    public List<Appointment> getTodayAppointments()
            throws DatabaseException {

        return enrichAppointmentsWithDetails(
                appointmentDAO.findTodayAppointments()
        );
    }

    @Override
    public Appointment updateAppointment(
            Appointment appointment)
            throws ValidationException,
            AppointmentConflictException,
            ResourceNotFoundException,
            DatabaseException {

        Optional<Appointment> existing =
                appointmentDAO.findById(
                        appointment.getAppointmentId()
                );

        if (existing.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    String.valueOf(
                            appointment.getAppointmentId()
                    )
            );
        }

        ValidationUtil.validateAppointmentDate(
                appointment.getAppointmentDate(),
                false
        );

        ValidationUtil.validateAppointmentTime(
                appointment.getAppointmentTime()
        );

        if (!appointmentDAO.isDentistAvailable(
                appointment.getDentistId(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime().toString())) {

            List<Appointment> conflicts =
                    appointmentDAO.findByDentistAndDate(
                            appointment.getDentistId(),
                            appointment.getAppointmentDate()
                    );

            for (Appointment conflict : conflicts) {

                if (conflict.getAppointmentId()
                        != appointment.getAppointmentId()
                        && conflict.getAppointmentTime()
                        .equals(appointment.getAppointmentTime())
                        && conflict.getStatus()
                        != AppointmentStatus.CANCELLED
                        && conflict.getStatus()
                        != AppointmentStatus.NO_SHOW) {

                    Optional<Dentist> dentist =
                            dentistDAO.findById(
                                    appointment.getDentistId()
                            );

                    throw new AppointmentConflictException(
                            dentist.map(Dentist::getFullName)
                                    .orElse("Unknown"),
                            appointment.getAppointmentDate()
                                    + " at "
                                    + appointment.getAppointmentTime()
                    );
                }
            }
        }

        Appointment updated =
                appointmentDAO.update(appointment);

        logger.info(
                "Appointment updated: {}",
                updated.getAppointmentNumber()
        );

        return enrichAppointmentWithDetails(updated);
    }

    @Override
    public boolean updateAppointmentStatus(
            int appointmentId,
            AppointmentStatus status)
            throws ResourceNotFoundException, DatabaseException {

        Optional<Appointment> appointment =
                appointmentDAO.findById(appointmentId);

        if (appointment.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    String.valueOf(appointmentId)
            );
        }

        boolean updated =
                appointmentDAO.updateStatus(
                        appointmentId,
                        status
                );

        logger.info(
                "Appointment status updated: {} -> {}",
                appointmentId,
                status
        );

        return updated;
    }

    @Override
    public boolean cancelAppointment(
            int appointmentId,
            String reason)
            throws ResourceNotFoundException, DatabaseException {

        Optional<Appointment> appointment =
                appointmentDAO.findById(appointmentId);

        if (appointment.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    String.valueOf(appointmentId)
            );
        }

        boolean cancelled =
                appointmentDAO.cancelAppointment(
                        appointmentId,
                        reason
                );

        logger.info(
                "Appointment cancelled: {}",
                appointmentId
        );

        return cancelled;
    }

    @Override
    public Appointment rescheduleAppointment(
            int appointmentId,
            LocalDate newDate,
            LocalTime newTime)
            throws ResourceNotFoundException,
            AppointmentConflictException,
            ValidationException,
            DatabaseException {

        ValidationUtil.validateAppointmentDate(
                newDate,
                false
        );

        ValidationUtil.validateAppointmentTime(
                newTime
        );

        Optional<Appointment> existingOptional =
                appointmentDAO.findById(appointmentId);

        if (existingOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Appointment",
                    String.valueOf(appointmentId)
            );
        }

        Appointment existing =
                existingOptional.get();

        if (!appointmentDAO.isDentistAvailable(
                existing.getDentistId(),
                newDate,
                newTime.toString())) {

            Optional<Dentist> dentist =
                    dentistDAO.findById(
                            existing.getDentistId()
                    );

            throw new AppointmentConflictException(
                    dentist.map(Dentist::getFullName)
                            .orElse("Unknown"),
                    newDate + " at " + newTime
            );
        }

        Appointment rescheduled =
                createRescheduledAppointment(
                        existing,
                        newDate,
                        newTime
                );

        Appointment saved =
                appointmentDAO.save(rescheduled);

        appointmentDAO.cancelAppointment(
                existing.getAppointmentId(),
                "Rescheduled to "
                        + saved.getAppointmentNumber()
        );

        logger.info(
                "Appointment rescheduled: {} -> {}",
                existing.getAppointmentNumber(),
                saved.getAppointmentNumber()
        );

        return enrichAppointmentWithDetails(saved);
    }

    @Override
    public boolean isDentistAvailable(
            int dentistId,
            LocalDate date,
            LocalTime time)
            throws DatabaseException {

        return appointmentDAO.isDentistAvailable(
                dentistId,
                date,
                time.toString()
        );
    }

    @Override
    public AppointmentStatistics getStatistics()
            throws DatabaseException {

        AppointmentStatistics stats =
                new AppointmentStatistics();

        stats.setTotalAppointments(
                appointmentDAO.countByStatus(null)
        );

        stats.setTodayAppointments(
                appointmentDAO.countTodayAppointments()
        );

        stats.setScheduled(
                appointmentDAO.countByStatus(
                        AppointmentStatus.SCHEDULED
                )
        );

        stats.setConfirmed(
                appointmentDAO.countByStatus(
                        AppointmentStatus.CONFIRMED
                )
        );

        stats.setCompleted(
                appointmentDAO.countByStatus(
                        AppointmentStatus.COMPLETED
                )
        );

        stats.setCancelled(
                appointmentDAO.countByStatus(
                        AppointmentStatus.CANCELLED
                )
        );

        stats.setNoShow(
                appointmentDAO.countByStatus(
                        AppointmentStatus.NO_SHOW
                )
        );

        return stats;
    }

    private void validateAppointmentRequest(
            AppointmentRequestDTO request)
            throws ValidationException {

        ValidationUtil.validateRequired(
                request.getPatientNumber(),
                "Patient number"
        );

        ValidationUtil.validateRequired(
                request.getDentistId(),
                "Dentist ID"
        );

        ValidationUtil.validateRequired(
                request.getTreatmentId(),
                "Treatment ID"
        );

        ValidationUtil.validateAppointmentDate(
                request.getAppointmentDate(),
                false
        );

        ValidationUtil.validateAppointmentTime(
                request.getAppointmentTime()
        );
    }

    private Appointment enrichAppointmentWithDetails(
            Appointment appointment)
            throws DatabaseException {

        if (appointment == null) {
            return null;
        }

        try {
            Optional<Patient> patient =
                    patientDAO.findById(
                            appointment.getPatientId()
                    );

            patient.ifPresent(
                    appointment::setPatient
            );

            Optional<Dentist> dentist =
                    dentistDAO.findById(
                            appointment.getDentistId()
                    );

            dentist.ifPresent(
                    appointment::setDentist
            );

            Optional<Treatment> treatment =
                    treatmentDAO.findById(
                            appointment.getTreatmentId()
                    );

            treatment.ifPresent(
                    appointment::setTreatment
            );

        } catch (DatabaseException e) {
            logger.warn(
                    "Failed to enrich appointment with details: {}",
                    e.getMessage()
            );
        }

        return appointment;
    }

    private List<Appointment> enrichAppointmentsWithDetails(
            List<Appointment> appointments)
            throws DatabaseException {

        for (Appointment appointment : appointments) {
            enrichAppointmentWithDetails(appointment);
        }

        return appointments;
    }

    private Appointment createRescheduledAppointment(
            Appointment existing,
            LocalDate newDate,
            LocalTime newTime) {

        Appointment rescheduled =
                new Appointment();

        rescheduled.setPatientId(
                existing.getPatientId()
        );

        rescheduled.setDentistId(
                existing.getDentistId()
        );

        rescheduled.setTreatmentId(
                existing.getTreatmentId()
        );

        rescheduled.setAppointmentDate(
                newDate
        );

        rescheduled.setAppointmentTime(
                newTime
        );

        rescheduled.setStatus(
                AppointmentStatus.SCHEDULED
        );

        rescheduled.setNotes(
                "Rescheduled from "
                        + existing.getAppointmentNumber()
        );

        rescheduled.setRescheduledFrom(
                existing.getAppointmentId()
        );

        rescheduled.setCreatedBy(
                existing.getCreatedBy()
        );

        return rescheduled;
    }
}