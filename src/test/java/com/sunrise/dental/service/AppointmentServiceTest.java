package com.sunrise.dental.service;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.DentistDAO;
import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.dto.AppointmentRequestDTO;
import com.sunrise.dental.exception.AppointmentConflictException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Dentist;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.model.enums.AppointmentStatus;
import com.sunrise.dental.model.enums.AvailabilityStatus;
import com.sunrise.dental.service.Impl.AppointmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentDAO appointmentDAO;

    @Mock
    private PatientDAO patientDAO;

    @Mock
    private DentistDAO dentistDAO;

    @Mock
    private TreatmentDAO treatmentDAO;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient testPatient;
    private Dentist testDentist;
    private Treatment testTreatment;
    private Appointment testAppointment;
    private AppointmentRequestDTO testRequest;

    private final LocalDate testDate =
            LocalDate.now().plusDays(2);

    private final LocalTime testTime =
            LocalTime.of(9, 0);

    @BeforeEach
    void setUp() {

        testPatient = new Patient();
        testPatient.setPatientId(1);
        testPatient.setPatientNumber("P0001");
        testPatient.setFullName("John Doe");
        testPatient.setContactNumber("0712345678");
        testPatient.setActive(true);

        testDentist = new Dentist();
        testDentist.setDentistId(1);
        testDentist.setDentistNumber("D001");
        testDentist.setFullName("Dr. Smith");
        testDentist.setAvailabilityStatus(
                AvailabilityStatus.AVAILABLE
        );
        testDentist.setActive(true);

        testTreatment = new Treatment();
        testTreatment.setTreatmentId(1);
        testTreatment.setTreatmentCode("T001");
        testTreatment.setTreatmentName("Consultation");
        testTreatment.setTreatmentCost(
                BigDecimal.ZERO
        );
        testTreatment.setConsultationFee(
                BigDecimal.valueOf(1000)
        );
        testTreatment.setActive(true);

        testAppointment = new Appointment();
        testAppointment.setAppointmentId(1);
        testAppointment.setAppointmentNumber("A0001");
        testAppointment.setPatientId(1);
        testAppointment.setDentistId(1);
        testAppointment.setTreatmentId(1);
        testAppointment.setAppointmentDate(testDate);
        testAppointment.setAppointmentTime(testTime);
        testAppointment.setStatus(
                AppointmentStatus.SCHEDULED
        );

        testRequest = new AppointmentRequestDTO();
        testRequest.setPatientNumber("P0001");
        testRequest.setDentistId(1);
        testRequest.setTreatmentId(1);
        testRequest.setAppointmentDate(testDate);
        testRequest.setAppointmentTime(testTime);
        testRequest.setNotes("Test appointment");
    }

    @Test
    void createAppointment_ShouldSucceed_WhenAllDataIsValid()
            throws Exception {

        when(patientDAO.findByPatientNumber("P0001"))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        when(treatmentDAO.findById(1))
                .thenReturn(Optional.of(testTreatment));

        when(appointmentDAO.isDentistAvailable(
                1,
                testDate,
                "09:00"))
                .thenReturn(true);

        when(appointmentDAO.save(any(Appointment.class)))
                .thenReturn(testAppointment);

        Appointment result =
                appointmentService.createAppointment(
                        testRequest
                );

        assertNotNull(result);

        assertEquals(
                testAppointment.getAppointmentNumber(),
                result.getAppointmentNumber()
        );

        verify(patientDAO)
                .findByPatientNumber("P0001");

        verify(dentistDAO)
                .findById(1);

        verify(treatmentDAO)
                .findById(1);

        verify(appointmentDAO)
                .isDentistAvailable(
                        eq(1),
                        eq(testDate),
                        anyString()
                );

        verify(appointmentDAO)
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenPatientNotFound()
            throws Exception {

        testRequest.setPatientNumber("P9999");

        when(patientDAO.findByPatientNumber("P9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO)
                .findByPatientNumber("P9999");

        verify(dentistDAO, never())
                .findById(anyInt());

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDentistNotFound()
            throws Exception {

        when(patientDAO.findByPatientNumber("P0001"))
                .thenReturn(Optional.of(testPatient));

        testRequest.setDentistId(999);

        when(dentistDAO.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO)
                .findByPatientNumber("P0001");

        verify(dentistDAO)
                .findById(999);

        verify(treatmentDAO, never())
                .findById(anyInt());

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDentistIsNotAvailable()
            throws Exception {

        testDentist.setAvailabilityStatus(
                AvailabilityStatus.BUSY
        );

        when(patientDAO.findByPatientNumber("P0001"))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        assertThrows(
                ValidationException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO)
                .findByPatientNumber("P0001");

        verify(dentistDAO)
                .findById(1);

        verify(treatmentDAO, never())
                .findById(anyInt());

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenTreatmentNotFound()
            throws Exception {

        when(patientDAO.findByPatientNumber("P0001"))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        testRequest.setTreatmentId(999);

        when(treatmentDAO.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO)
                .findByPatientNumber("P0001");

        verify(dentistDAO)
                .findById(1);

        verify(treatmentDAO)
                .findById(999);

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDentistIsDoubleBooked()
            throws Exception {

        when(patientDAO.findByPatientNumber("P0001"))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        when(treatmentDAO.findById(1))
                .thenReturn(Optional.of(testTreatment));

        when(appointmentDAO.isDentistAvailable(
                1,
                testDate,
                "09:00"))
                .thenReturn(false);

        assertThrows(
                AppointmentConflictException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO)
                .findByPatientNumber("P0001");

        verify(dentistDAO)
                .findById(1);

        verify(treatmentDAO)
                .findById(1);

        verify(appointmentDAO)
                .isDentistAvailable(
                        eq(1),
                        eq(testDate),
                        anyString()
                );

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenDateIsInPast()
            throws Exception {

        testRequest.setAppointmentDate(
                LocalDate.now().minusDays(1)
        );

        assertThrows(
                ValidationException.class,
                () -> appointmentService.createAppointment(
                        testRequest
                )
        );

        verify(patientDAO, never())
                .findByPatientNumber(anyString());

        verify(appointmentDAO, never())
                .save(any(Appointment.class));
    }

    @Test
    void getAppointmentById_ShouldReturnAppointment_WhenAppointmentExists()
            throws Exception {

        when(appointmentDAO.findById(1))
                .thenReturn(Optional.of(testAppointment));

        when(patientDAO.findById(1))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        when(treatmentDAO.findById(1))
                .thenReturn(Optional.of(testTreatment));

        Appointment result =
                appointmentService.getAppointmentById(1);

        assertNotNull(result);

        assertEquals(
                testAppointment.getAppointmentNumber(),
                result.getAppointmentNumber()
        );

        verify(appointmentDAO)
                .findById(1);

        verify(patientDAO)
                .findById(1);

        verify(dentistDAO)
                .findById(1);

        verify(treatmentDAO)
                .findById(1);
    }

    @Test
    void getAppointmentById_ShouldThrowException_WhenAppointmentNotFound()
            throws Exception {

        when(appointmentDAO.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentById(999)
        );

        verify(appointmentDAO)
                .findById(999);

        verify(patientDAO, never())
                .findById(anyInt());
    }

    @Test
    void getAppointmentByNumber_ShouldReturnAppointment_WhenAppointmentExists()
            throws Exception {

        when(appointmentDAO.findByAppointmentNumber("A0001"))
                .thenReturn(Optional.of(testAppointment));

        when(patientDAO.findById(1))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        when(treatmentDAO.findById(1))
                .thenReturn(Optional.of(testTreatment));

        Appointment result =
                appointmentService.getAppointmentByNumber(
                        "A0001"
                );

        assertNotNull(result);

        assertEquals(
                "A0001",
                result.getAppointmentNumber()
        );

        verify(appointmentDAO)
                .findByAppointmentNumber("A0001");
    }

    @Test
    void getAppointmentByNumber_ShouldThrowException_WhenAppointmentNotFound()
            throws Exception {

        when(appointmentDAO.findByAppointmentNumber("A9999"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.getAppointmentByNumber(
                        "A9999"
                )
        );

        verify(appointmentDAO)
                .findByAppointmentNumber("A9999");
    }

    @Test
    void getAppointmentsByPatientId_ShouldReturnAppointments()
            throws Exception {

        List<Appointment> appointments =
                Arrays.asList(testAppointment);

        when(appointmentDAO.findByPatientId(1))
                .thenReturn(appointments);

        when(patientDAO.findById(1))
                .thenReturn(Optional.of(testPatient));

        when(dentistDAO.findById(1))
                .thenReturn(Optional.of(testDentist));

        when(treatmentDAO.findById(1))
                .thenReturn(Optional.of(testTreatment));

        List<Appointment> result =
                appointmentService.getAppointmentsByPatientId(1);

        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        verify(appointmentDAO)
                .findByPatientId(1);
    }

    @Test
    void updateAppointmentStatus_ShouldSucceed_WhenAppointmentExists()
            throws Exception {

        when(appointmentDAO.findById(1))
                .thenReturn(Optional.of(testAppointment));

        when(appointmentDAO.updateStatus(
                1,
                AppointmentStatus.CONFIRMED))
                .thenReturn(true);

        boolean result =
                appointmentService.updateAppointmentStatus(
                        1,
                        AppointmentStatus.CONFIRMED
                );

        assertTrue(result);

        verify(appointmentDAO)
                .findById(1);

        verify(appointmentDAO)
                .updateStatus(
                        1,
                        AppointmentStatus.CONFIRMED
                );
    }

    @Test
    void updateAppointmentStatus_ShouldThrowException_WhenAppointmentNotFound()
            throws Exception {

        when(appointmentDAO.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.updateAppointmentStatus(
                        999,
                        AppointmentStatus.CONFIRMED
                )
        );

        verify(appointmentDAO)
                .findById(999);

        verify(appointmentDAO, never())
                .updateStatus(
                        anyInt(),
                        any(AppointmentStatus.class)
                );
    }

    @Test
    void cancelAppointment_ShouldSucceed_WhenAppointmentExists()
            throws Exception {

        when(appointmentDAO.findById(1))
                .thenReturn(Optional.of(testAppointment));

        when(appointmentDAO.cancelAppointment(
                1,
                "Patient cancelled"))
                .thenReturn(true);

        boolean result =
                appointmentService.cancelAppointment(
                        1,
                        "Patient cancelled"
                );

        assertTrue(result);

        verify(appointmentDAO)
                .findById(1);

        verify(appointmentDAO)
                .cancelAppointment(
                        1,
                        "Patient cancelled"
                );
    }

    @Test
    void cancelAppointment_ShouldThrowException_WhenAppointmentNotFound()
            throws Exception {

        when(appointmentDAO.findById(999))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.cancelAppointment(
                        999,
                        "Patient cancelled"
                )
        );

        verify(appointmentDAO)
                .findById(999);

        verify(appointmentDAO, never())
                .cancelAppointment(
                        anyInt(),
                        anyString()
                );
    }

    @Test
    void isDentistAvailable_ShouldReturnTrue_WhenDentistIsAvailable()
            throws Exception {

        when(appointmentDAO.isDentistAvailable(
                1,
                testDate,
                "09:00"))
                .thenReturn(true);

        boolean result =
                appointmentService.isDentistAvailable(
                        1,
                        testDate,
                        testTime
                );

        assertTrue(result);

        verify(appointmentDAO)
                .isDentistAvailable(
                        eq(1),
                        eq(testDate),
                        anyString()
                );
    }

    @Test
    void isDentistAvailable_ShouldReturnFalse_WhenDentistIsNotAvailable()
            throws Exception {

        when(appointmentDAO.isDentistAvailable(
                1,
                testDate,
                "09:00"))
                .thenReturn(false);

        boolean result =
                appointmentService.isDentistAvailable(
                        1,
                        testDate,
                        testTime
                );

        assertFalse(result);

        verify(appointmentDAO)
                .isDentistAvailable(
                        eq(1),
                        eq(testDate),
                        anyString()
                );
    }

    @Test
    void getStatistics_ShouldReturnStatistics()
            throws Exception {

        when(appointmentDAO.countByStatus(null))
                .thenReturn(100L);

        when(appointmentDAO.countTodayAppointments())
                .thenReturn(5L);

        when(appointmentDAO.countByStatus(
                AppointmentStatus.SCHEDULED))
                .thenReturn(30L);

        when(appointmentDAO.countByStatus(
                AppointmentStatus.CONFIRMED))
                .thenReturn(20L);

        when(appointmentDAO.countByStatus(
                AppointmentStatus.COMPLETED))
                .thenReturn(40L);

        when(appointmentDAO.countByStatus(
                AppointmentStatus.CANCELLED))
                .thenReturn(8L);

        when(appointmentDAO.countByStatus(
                AppointmentStatus.NO_SHOW))
                .thenReturn(2L);

        AppointmentService.AppointmentStatistics stats =
                appointmentService.getStatistics();

        assertNotNull(stats);

        assertEquals(
                100L,
                stats.getTotalAppointments()
        );

        assertEquals(
                5L,
                stats.getTodayAppointments()
        );

        assertEquals(
                30L,
                stats.getScheduled()
        );

        assertEquals(
                20L,
                stats.getConfirmed()
        );

        assertEquals(
                40L,
                stats.getCompleted()
        );

        assertEquals(
                8L,
                stats.getCancelled()
        );

        assertEquals(
                2L,
                stats.getNoShow()
        );

        verify(appointmentDAO)
                .countByStatus(null);

        verify(appointmentDAO)
                .countTodayAppointments();

        verify(appointmentDAO, times(5))
                .countByStatus(
                        any(AppointmentStatus.class)
                );
    }
}