package com.sunrise.dental.service;

import com.sunrise.dental.dao.PatientDAO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Patient;
import com.sunrise.dental.model.enums.Gender;
import com.sunrise.dental.service.Impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PatientService.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientDAO patientDAO;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setPatientId(1);
        testPatient.setPatientNumber("P0001");
        testPatient.setFullName("John Doe");
        testPatient.setAddress("123 Main St, Colombo");
        testPatient.setContactNumber("0712345678");
        testPatient.setEmail("john@example.com");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testPatient.setGender(Gender.MALE);
        testPatient.setRegistrationDate(LocalDate.now());
        testPatient.setActive(true);
    }

    @Test
    void registerPatient_ShouldSucceed_WhenDataIsValid() throws Exception {
        when(patientDAO.contactNumberExists(anyString(), anyInt())).thenReturn(false);
        when(patientDAO.save(any(Patient.class))).thenReturn(testPatient);

        Patient result = patientService.registerPatient(testPatient);

        assertNotNull(result);
        assertEquals(testPatient.getPatientNumber(), result.getPatientNumber());
        assertEquals(testPatient.getFullName(), result.getFullName());

        verify(patientDAO).contactNumberExists(testPatient.getContactNumber(), 0);
        verify(patientDAO).save(any(Patient.class));
    }

    @Test
    void registerPatient_ShouldThrowException_WhenNameIsInvalid() throws Exception {
        testPatient.setFullName("J"); // Too short

        assertThrows(ValidationException.class, () -> {
            patientService.registerPatient(testPatient);
        });

        verify(patientDAO, never()).save(any(Patient.class));
    }

    @Test
    void registerPatient_ShouldThrowException_WhenContactNumberIsInvalid() throws Exception {
        testPatient.setContactNumber("123"); // Invalid

        assertThrows(ValidationException.class, () -> {
            patientService.registerPatient(testPatient);
        });

        verify(patientDAO, never()).save(any(Patient.class));
    }

    @Test
    void registerPatient_ShouldThrowException_WhenContactNumberAlreadyExists() throws Exception {
        when(patientDAO.contactNumberExists(testPatient.getContactNumber(), 0)).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            patientService.registerPatient(testPatient);
        });

        verify(patientDAO).contactNumberExists(testPatient.getContactNumber(), 0);
        verify(patientDAO, never()).save(any(Patient.class));
    }

    @Test
    void getPatientById_ShouldReturnPatient_WhenPatientExists() throws Exception {
        when(patientDAO.findById(1)).thenReturn(Optional.of(testPatient));

        Patient result = patientService.getPatientById(1);

        assertNotNull(result);
        assertEquals(testPatient.getPatientId(), result.getPatientId());
        assertEquals(testPatient.getFullName(), result.getFullName());

        verify(patientDAO).findById(1);
    }

    @Test
    void getPatientById_ShouldThrowException_WhenPatientNotFound() throws Exception {
        when(patientDAO.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatientById(999);
        });

        verify(patientDAO).findById(999);
    }

    @Test
    void getPatientById_ShouldThrowException_WhenIdIsInvalid() throws Exception {
        assertThrows(ValidationException.class, () -> {
            patientService.getPatientById(0);
        });

        verify(patientDAO, never()).findById(anyInt());
    }

    @Test
    void getPatientByNumber_ShouldReturnPatient_WhenPatientExists() throws Exception {
        when(patientDAO.findByPatientNumber("P0001")).thenReturn(Optional.of(testPatient));

        Patient result = patientService.getPatientByNumber("P0001");

        assertNotNull(result);
        assertEquals(testPatient.getPatientNumber(), result.getPatientNumber());

        verify(patientDAO).findByPatientNumber("P0001");
    }

    @Test
    void getPatientByNumber_ShouldThrowException_WhenPatientNotFound() throws Exception {
        when(patientDAO.findByPatientNumber("P9999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.getPatientByNumber("P9999");
        });

        verify(patientDAO).findByPatientNumber("P9999");
    }

    @Test
    void searchPatientsByName_ShouldReturnPatients_WhenNameMatches() throws Exception {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientDAO.findByName("John")).thenReturn(patients);

        List<Patient> result = patientService.searchPatientsByName("John");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());

        verify(patientDAO).findByName("John");
    }

    @Test
    void searchPatientsByName_ShouldReturnAllActivePatients_WhenNameIsEmpty() throws Exception {
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientDAO.findAllActive()).thenReturn(patients);

        List<Patient> result = patientService.searchPatientsByName(null);

        assertNotNull(result);
        verify(patientDAO).findAllActive();
    }

    @Test
    void updatePatient_ShouldSucceed_WhenDataIsValid() throws Exception {
        when(patientDAO.findById(1)).thenReturn(Optional.of(testPatient));
        when(patientDAO.contactNumberExists(testPatient.getContactNumber(), 1)).thenReturn(false);
        when(patientDAO.update(any(Patient.class))).thenReturn(testPatient);

        Patient result = patientService.updatePatient(testPatient);

        assertNotNull(result);
        verify(patientDAO).findById(1);
        verify(patientDAO).contactNumberExists(testPatient.getContactNumber(), 1);
        verify(patientDAO).update(any(Patient.class));
    }

    @Test
    void updatePatient_ShouldThrowException_WhenPatientNotFound() throws Exception {
        when(patientDAO.findById(999)).thenReturn(Optional.empty());

        testPatient.setPatientId(999);

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(testPatient);
        });

        verify(patientDAO).findById(999);
        verify(patientDAO, never()).update(any(Patient.class));
    }

    @Test
    void deactivatePatient_ShouldSucceed_WhenPatientExists() throws Exception {
        when(patientDAO.findById(1)).thenReturn(Optional.of(testPatient));
        when(patientDAO.delete(1)).thenReturn(true);

        boolean result = patientService.deactivatePatient(1);

        assertTrue(result);
        verify(patientDAO).findById(1);
        verify(patientDAO).delete(1);
    }

    @Test
    void deactivatePatient_ShouldThrowException_WhenPatientNotFound() throws Exception {
        when(patientDAO.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deactivatePatient(999);
        });

        verify(patientDAO).findById(999);
        verify(patientDAO, never()).delete(anyInt());
    }

    @Test
    void getPatientCount_ShouldReturnCount() throws Exception {
        when(patientDAO.count()).thenReturn(10L);

        long count = patientService.getPatientCount();

        assertEquals(10L, count);
        verify(patientDAO).count();
    }
}