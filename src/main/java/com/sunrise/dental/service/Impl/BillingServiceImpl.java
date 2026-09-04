package com.sunrise.dental.service.Impl;

import com.sunrise.dental.dao.AppointmentDAO;
import com.sunrise.dental.dao.BillDAO;
import com.sunrise.dental.dao.PaymentDAO;
import com.sunrise.dental.dao.TreatmentDAO;
import com.sunrise.dental.dao.Impl.AppointmentDAOImpl;
import com.sunrise.dental.dao.Impl.BillDAOImpl;
import com.sunrise.dental.dao.Impl.PaymentDAOImpl;
import com.sunrise.dental.dao.Impl.TreatmentDAOImpl;
import com.sunrise.dental.dto.BillRequestDTO;
import com.sunrise.dental.exception.DatabaseException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.exception.ValidationException;
import com.sunrise.dental.model.Appointment;
import com.sunrise.dental.model.Bill;
import com.sunrise.dental.model.Payment;
import com.sunrise.dental.model.Treatment;
import com.sunrise.dental.model.enums.PaymentStatus;
import com.sunrise.dental.service.BillingService;
import com.sunrise.dental.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BillingService interface.
 */
public class BillingServiceImpl implements BillingService {

    private static final Logger logger = LogManager.getLogger(BillingServiceImpl.class);
    private final BillDAO billDAO;
    private final AppointmentDAO appointmentDAO;
    private final TreatmentDAO treatmentDAO;
    private final PaymentDAO paymentDAO;

    public BillingServiceImpl() {
        this.billDAO = new BillDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
        this.treatmentDAO = new TreatmentDAOImpl();
        this.paymentDAO = new PaymentDAOImpl();
    }

    @Override
    public Bill generateBill(BillRequestDTO request) throws ValidationException, ResourceNotFoundException, DatabaseException {
        logger.info("Generating bill for appointment: {}", request.getAppointmentId());

        ValidationUtil.validateRequired(request.getAppointmentId(), "Appointment ID");
        ValidationUtil.validateAmount(request.getDiscount(), "Discount");
        ValidationUtil.validateAmount(request.getTax(), "Tax");

        // Get appointment
        Optional<Appointment> appointmentOptional = appointmentDAO.findById(request.getAppointmentId());
        if (appointmentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Appointment", String.valueOf(request.getAppointmentId()));
        }
        Appointment appointment = appointmentOptional.get();

        // Check if bill already exists for this appointment
        Optional<Bill> existingBill = billDAO.findByAppointmentId(request.getAppointmentId());
        if (existingBill.isPresent()) {
            throw new ValidationException("Bill already exists for this appointment: " + existingBill.get().getBillNumber());
        }

        // Get treatment
        Optional<Treatment> treatmentOptional = treatmentDAO.findById(appointment.getTreatmentId());
        if (treatmentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Treatment", String.valueOf(appointment.getTreatmentId()));
        }
        Treatment treatment = treatmentOptional.get();

        // Calculate bill amounts
        BigDecimal treatmentCost = treatment.getTreatmentCost();
        BigDecimal consultationFee = treatment.getConsultationFee();
        BigDecimal subtotal = treatmentCost.add(consultationFee);
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discount).add(tax);

        // Create bill
        Bill bill = new Bill();
        bill.setAppointmentId(request.getAppointmentId());
        bill.setPatientId(appointment.getPatientId());
        bill.setTreatmentCost(treatmentCost);
        bill.setConsultationFee(consultationFee);
        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setTax(tax);
        bill.setTotalAmount(totalAmount);
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setBillDate(LocalDate.now());
        bill.setCreatedBy(request.getCreatedBy());
        bill.setNotes("Bill generated for appointment: " + appointment.getAppointmentNumber());

        Bill saved = billDAO.save(bill);
        logger.info("Bill generated successfully: {}", saved.getBillNumber());
        return saved;
    }

    @Override
    public Bill getBillById(int billId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(billId, "Bill");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Bill> bill = billDAO.findById(billId);
        if (bill.isEmpty()) {
            throw new ResourceNotFoundException("Bill", String.valueOf(billId));
        }
        return bill.get();
    }

    @Override
    public Bill getBillByNumber(String billNumber) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateRequired(billNumber, "Bill number");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Bill> bill = billDAO.findByBillNumber(billNumber);
        if (bill.isEmpty()) {
            throw new ResourceNotFoundException("Bill", billNumber);
        }
        return bill.get();
    }

    @Override
    public Bill getBillByAppointmentId(int appointmentId) throws ResourceNotFoundException, DatabaseException {
        try {
            ValidationUtil.validateId(appointmentId, "Appointment");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Optional<Bill> bill = billDAO.findByAppointmentId(appointmentId);
        if (bill.isEmpty()) {
            throw new ResourceNotFoundException("Bill", "appointment: " + appointmentId);
        }
        return bill.get();
    }

    @Override
    public List<Bill> getBillsByPatientId(int patientId) throws DatabaseException {
        try {
            ValidationUtil.validateId(patientId, "Patient");
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        return billDAO.findByPatientId(patientId);
    }

    @Override
    public List<Bill> getBillsByPaymentStatus(PaymentStatus status) throws DatabaseException {
        if (status == null) {
            return billDAO.findAll();
        }
        return billDAO.findByPaymentStatus(status);
    }

    @Override
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        return billDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public Bill processPayment(int billId, Payment payment)
            throws ResourceNotFoundException, ValidationException, DatabaseException {

        logger.info("Processing payment for bill: {}", billId);

        // Validate bill exists
        Optional<Bill> billOptional = billDAO.findById(billId);
        if (billOptional.isEmpty()) {
            throw new ResourceNotFoundException("Bill", String.valueOf(billId));
        }
        Bill bill = billOptional.get();

        // Validate payment
        ValidationUtil.validateAmount(payment.getAmount(), "Payment amount");
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be greater than zero.");
        }

        // Check if bill is already fully paid
        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Bill is already fully paid.");
        }

        // Check if payment exceeds balance
        BigDecimal balance = bill.getTotalAmount().subtract(bill.getPaidAmount());
        if (payment.getAmount().compareTo(balance) > 0) {
            throw new ValidationException("Payment amount exceeds balance due: " + balance);
        }

        // Save payment
        payment.setBillId(billId);
        payment.setPaymentDate(java.time.LocalDateTime.now());
        Payment savedPayment = paymentDAO.save(payment);

        // Update bill
        BigDecimal totalPaid = paymentDAO.getTotalPaidByBill(billId);
        bill.setPaidAmount(totalPaid);

        // Update payment status
        if (totalPaid.compareTo(bill.getTotalAmount()) >= 0) {
            bill.setPaymentStatus(PaymentStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            bill.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        Bill updated = billDAO.update(bill);
        logger.info("Payment processed successfully: {}", savedPayment.getPaymentNumber());
        return updated;
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        return billDAO.getTotalRevenue(startDate, endDate);
    }

    @Override
    public List<RevenueData> getRevenueByTreatment(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        List<Object[]> results = billDAO.getRevenueByTreatment(startDate, endDate);
        List<RevenueData> data = new ArrayList<>();
        for (Object[] row : results) {
            data.add(new RevenueData(
                    (String) row[0],
                    ((Number) row[1]).intValue(),
                    (BigDecimal) row[2]
            ));
        }
        return data;
    }
}