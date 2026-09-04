package com.sunrise.dental.service;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.dao.PaymentDAO;
import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.dto.BillRequestDTO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.model.enums.PaymentMethod;
import com.sunrise.dental.model.enums.PaymentStatus;
import com.sunrise.dental.service.Impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingService.
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillDAO billDAO;

    @Mock
    private AppointmentDAO appointmentDAO;

    @Mock
    private TreatmentDAO treatmentDAO;

    @Mock
    private PaymentDAO paymentDAO;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Treatment testTreatment;
    private Appointment testAppointment;
    private Bill testBill;
    private BillRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testTreatment = new Treatment();
        testTreatment.setTreatmentId(1);
        testTreatment.setTreatmentCode("T001");
        testTreatment.setTreatmentName("Consultation");
        testTreatment.setTreatmentCost(BigDecimal.valueOf(5000));
        testTreatment.setConsultationFee(BigDecimal.valueOf(1000));

        testAppointment = new Appointment();
        testAppointment.setAppointmentId(1);
        testAppointment.setAppointmentNumber("A0001");
        testAppointment.setPatientId(1);
        testAppointment.setTreatmentId(1);

        testBill = new Bill();
        testBill.setBillId(1);
        testBill.setBillNumber("B0001");
        testBill.setAppointmentId(1);
        testBill.setPatientId(1);
        testBill.setTreatmentCost(BigDecimal.valueOf(5000));
        testBill.setConsultationFee(BigDecimal.valueOf(1000));
        testBill.setSubtotal(BigDecimal.valueOf(6000));
        testBill.setDiscount(BigDecimal.ZERO);
        testBill.setTax(BigDecimal.ZERO);
        testBill.setTotalAmount(BigDecimal.valueOf(6000));
        testBill.setPaymentStatus(PaymentStatus.UNPAID);
        testBill.setPaidAmount(BigDecimal.ZERO);
        testBill.setBillDate(LocalDate.now());

        testRequest = new BillRequestDTO();
        testRequest.setAppointmentId(1);
        testRequest.setDiscount(BigDecimal.ZERO);
        testRequest.setTax(BigDecimal.ZERO);
    }

    @Test
    void generateBill_ShouldSucceed_WhenDataIsValid() throws Exception {
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(testAppointment));
        when(billDAO.findByAppointmentId(1)).thenReturn(Optional.empty());
        when(treatmentDAO.findById(1)).thenReturn(Optional.of(testTreatment));
        when(billDAO.save(any(Bill.class))).thenReturn(testBill);

        Bill result = billingService.generateBill(testRequest);

        assertNotNull(result);
        assertEquals(testBill.getBillNumber(), result.getBillNumber());
        assertEquals(testBill.getTotalAmount(), result.getTotalAmount());

        verify(appointmentDAO).findById(1);
        verify(billDAO).findByAppointmentId(1);
        verify(treatmentDAO).findById(1);
        verify(billDAO).save(any(Bill.class));
    }

    @Test
    void generateBill_ShouldThrowException_WhenAppointmentNotFound() throws Exception {
        when(appointmentDAO.findById(999)).thenReturn(Optional.empty());
        testRequest.setAppointmentId(999);

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.generateBill(testRequest);
        });

        verify(appointmentDAO).findById(999);
        verify(billDAO, never()).findByAppointmentId(anyInt());
        verify(billDAO, never()).save(any(Bill.class));
    }

    @Test
    void generateBill_ShouldThrowException_WhenBillAlreadyExists() throws Exception {
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(testAppointment));
        when(billDAO.findByAppointmentId(1)).thenReturn(Optional.of(testBill));

        assertThrows(ValidationException.class, () -> {
            billingService.generateBill(testRequest);
        });

        verify(appointmentDAO).findById(1);
        verify(billDAO).findByAppointmentId(1);
        verify(treatmentDAO, never()).findById(anyInt());
        verify(billDAO, never()).save(any(Bill.class));
    }

    @Test
    void generateBill_ShouldThrowException_WhenTreatmentNotFound() throws Exception {
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(testAppointment));
        when(billDAO.findByAppointmentId(1)).thenReturn(Optional.empty());
        when(treatmentDAO.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.generateBill(testRequest);
        });

        verify(appointmentDAO).findById(1);
        verify(billDAO).findByAppointmentId(1);
        verify(treatmentDAO).findById(1);
        verify(billDAO, never()).save(any(Bill.class));
    }

    @Test
    void generateBill_ShouldCalculateTotalsCorrectly_WithDiscountAndTax() throws Exception {
        when(appointmentDAO.findById(1)).thenReturn(Optional.of(testAppointment));
        when(billDAO.findByAppointmentId(1)).thenReturn(Optional.empty());
        when(treatmentDAO.findById(1)).thenReturn(Optional.of(testTreatment));
        when(billDAO.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setBillNumber("B0001");
            return bill;
        });

        testRequest.setDiscount(BigDecimal.valueOf(500));
        testRequest.setTax(BigDecimal.valueOf(300));

        Bill result = billingService.generateBill(testRequest);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(6000), result.getSubtotal());
        assertEquals(BigDecimal.valueOf(500), result.getDiscount());
        assertEquals(BigDecimal.valueOf(300), result.getTax());
        assertEquals(BigDecimal.valueOf(5800), result.getTotalAmount()); // 6000 - 500 + 300

        verify(billDAO).save(any(Bill.class));
    }

    @Test
    void getBillById_ShouldReturnBill_WhenBillExists() throws Exception {
        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));

        Bill result = billingService.getBillById(1);

        assertNotNull(result);
        assertEquals(testBill.getBillNumber(), result.getBillNumber());

        verify(billDAO).findById(1);
    }

    @Test
    void getBillById_ShouldThrowException_WhenBillNotFound() throws Exception {
        when(billDAO.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.getBillById(999);
        });

        verify(billDAO).findById(999);
    }

    @Test
    void getBillByNumber_ShouldReturnBill_WhenBillExists() throws Exception {
        when(billDAO.findByBillNumber("B0001")).thenReturn(Optional.of(testBill));

        Bill result = billingService.getBillByNumber("B0001");

        assertNotNull(result);
        assertEquals(testBill.getBillNumber(), result.getBillNumber());

        verify(billDAO).findByBillNumber("B0001");
    }

    @Test
    void getBillByNumber_ShouldThrowException_WhenBillNotFound() throws Exception {
        when(billDAO.findByBillNumber("B9999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.getBillByNumber("B9999");
        });

        verify(billDAO).findByBillNumber("B9999");
    }

    @Test
    void getBillByAppointmentId_ShouldReturnBill_WhenBillExists() throws Exception {
        when(billDAO.findByAppointmentId(1)).thenReturn(Optional.of(testBill));

        Bill result = billingService.getBillByAppointmentId(1);

        assertNotNull(result);
        assertEquals(testBill.getBillNumber(), result.getBillNumber());

        verify(billDAO).findByAppointmentId(1);
    }

    @Test
    void getBillByAppointmentId_ShouldThrowException_WhenBillNotFound() throws Exception {
        when(billDAO.findByAppointmentId(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.getBillByAppointmentId(999);
        });

        verify(billDAO).findByAppointmentId(999);
    }

    @Test
    void processPayment_ShouldSucceed_WhenPaymentIsValid() throws Exception {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(6000));
        payment.setPaymentMethod(PaymentMethod.CASH);

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));
        when(paymentDAO.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setPaymentNumber("PAY0001");
            return p;
        });
        when(paymentDAO.getTotalPaidByBill(1)).thenReturn(BigDecimal.valueOf(6000));
        when(billDAO.update(any(Bill.class))).thenReturn(testBill);

        Bill result = billingService.processPayment(1, payment);

        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());

        verify(billDAO).findById(1);
        verify(paymentDAO).save(any(Payment.class));
        verify(paymentDAO).getTotalPaidByBill(1);
        verify(billDAO).update(any(Bill.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenBillNotFound() throws Exception {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(6000));

        when(billDAO.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            billingService.processPayment(999, payment);
        });

        verify(billDAO).findById(999);
        verify(paymentDAO, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenPaymentAmountIsZero() throws Exception {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.ZERO);

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));

        assertThrows(ValidationException.class, () -> {
            billingService.processPayment(1, payment);
        });

        verify(billDAO).findById(1);
        verify(paymentDAO, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenPaymentAmountIsNegative() throws Exception {
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(-100));

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));

        assertThrows(ValidationException.class, () -> {
            billingService.processPayment(1, payment);
        });

        verify(billDAO).findById(1);
        verify(paymentDAO, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenBillIsAlreadyPaid() throws Exception {
        testBill.setPaymentStatus(PaymentStatus.PAID);
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(1000));

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));

        assertThrows(ValidationException.class, () -> {
            billingService.processPayment(1, payment);
        });

        verify(billDAO).findById(1);
        verify(paymentDAO, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldThrowException_WhenPaymentExceedsBalance() throws Exception {
        testBill.setPaidAmount(BigDecimal.valueOf(1000));
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(10000));

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));

        assertThrows(ValidationException.class, () -> {
            billingService.processPayment(1, payment);
        });

        verify(billDAO).findById(1);
        verify(paymentDAO, never()).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldSetStatusToPartial_WhenPaymentIsPartial() throws Exception {
        testBill.setPaymentStatus(PaymentStatus.UNPAID);
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(3000));

        when(billDAO.findById(1)).thenReturn(Optional.of(testBill));
        when(paymentDAO.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setPaymentNumber("PAY0001");
            return p;
        });
        when(paymentDAO.getTotalPaidByBill(1)).thenReturn(BigDecimal.valueOf(3000));
        when(billDAO.update(any(Bill.class))).thenReturn(testBill);

        Bill result = billingService.processPayment(1, payment);

        assertNotNull(result);
        assertEquals(PaymentStatus.PARTIAL, result.getPaymentStatus());

        verify(billDAO).findById(1);
        verify(paymentDAO).save(any(Payment.class));
        verify(paymentDAO).getTotalPaidByBill(1);
        verify(billDAO).update(any(Bill.class));
    }

    @Test
    void getTotalRevenue_ShouldReturnTotalRevenue() throws Exception {
        when(billDAO.getTotalRevenue(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(15000));

        BigDecimal revenue = billingService.getTotalRevenue(
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        assertEquals(BigDecimal.valueOf(15000), revenue);
        verify(billDAO).getTotalRevenue(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getRevenueByTreatment_ShouldReturnRevenueData() throws Exception {
        List<Object[]> mockData = Arrays.asList(
                new Object[]{"Consultation", 5, BigDecimal.valueOf(5000)},
                new Object[]{"Cleaning", 3, BigDecimal.valueOf(3000)}
        );
        when(billDAO.getRevenueByTreatment(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockData);

        List<BillingService.RevenueData> result = billingService.getRevenueByTreatment(
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Consultation", result.get(0).getTreatmentName());
        assertEquals(5, result.get(0).getCount());
        assertEquals(BigDecimal.valueOf(5000), result.get(0).getRevenue());

        verify(billDAO).getRevenueByTreatment(any(LocalDate.class), any(LocalDate.class));
    }
}