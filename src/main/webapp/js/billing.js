/**
 * Billing Logic
 * Sunrise Dental Clinic Management System
 */

let currentBill = null;

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('billing.html')) {
        setupBillForm();
        setupPaymentForm();
        checkForAppointmentParam();
    }
});

// Check for appointment param from search
function checkForAppointmentParam() {
    const params = new URLSearchParams(window.location.search);
    const appointmentNumber = params.get('appointment');
    if (appointmentNumber) {
        document.getElementById('billAppointmentNumber').value = appointmentNumber;
        // Auto-generate bill
        setTimeout(() => {
            document.getElementById('billForm').dispatchEvent(new Event('submit'));
        }, 500);
    }
}

// Setup bill form
function setupBillForm() {
    const form = document.getElementById('billForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await generateBill();
        });
    }
}

// Generate bill
async function generateBill() {
    const appointmentNumber = document.getElementById('billAppointmentNumber').value.trim();
    const discount = parseFloat(document.getElementById('billDiscount').value) || 0;
    const tax = parseFloat(document.getElementById('billTax').value) || 0;

    clearError('billError');
    document.getElementById('billResult').style.display = 'none';

    if (!appointmentNumber) {
        showError('billError', 'Appointment number is required.');
        return;
    }

    // First, get appointment details to find appointment ID
    try {
        const searchResponse = await apiGet(`/appointments/search?number=${appointmentNumber}`);
        if (searchResponse.status !== 200) {
            showError('billError', 'Appointment not found. Please check the appointment number.');
            return;
        }

        const appointment = searchResponse.data;
        const appointmentId = appointment.appointmentId;

        // Generate bill
        const billData = {
            appointmentId: appointmentId,
            discount: discount,
            tax: tax,
            createdBy: 1 // This should come from session
        };

        const response = await apiPost('/bills', billData);
        if (response.status === 201 || response.status === 200) {
            currentBill = response.data;
            displayBill(currentBill);
            showSuccess('Bill generated successfully!');
        } else {
            const errorMsg = response.data && response.data.error ? response.data.error : 'Failed to generate bill';
            showError('billError', errorMsg);
        }
    } catch (error) {
        console.error('Error generating bill:', error);
        showError('billError', 'Failed to generate bill. Please try again.');
    }
}

// Display bill details
function displayBill(bill) {
    document.getElementById('billResult').style.display = 'block';

    document.getElementById('billNumber').textContent = bill.billNumber || '-';
    document.getElementById('billDate').textContent = formatDate(bill.billDate);
    document.getElementById('billPatient').textContent = bill.patient ? bill.patient.fullName : `Patient #${bill.patientId}`;
    document.getElementById('billAppointment').textContent = bill.appointment ? bill.appointment.appointmentNumber : '-';
    document.getElementById('billDentist').textContent = bill.appointment && bill.appointment.dentist ? bill.appointment.dentist.fullName : '-';
    document.getElementById('billTreatment').textContent = bill.appointment && bill.appointment.treatment ? bill.appointment.treatment.treatmentName : '-';

    document.getElementById('billTreatmentCost').textContent = formatCurrency(bill.treatmentCost);
    document.getElementById('billConsultFee').textContent = formatCurrency(bill.consultationFee);
    document.getElementById('billSubtotal').textContent = formatCurrency(bill.subtotal);
    document.getElementById('billDiscountDisplay').textContent = formatCurrency(bill.discount);
    document.getElementById('billTaxDisplay').textContent = formatCurrency(bill.tax);
    document.getElementById('billTotal').textContent = formatCurrency(bill.totalAmount);
    document.getElementById('billPaid').textContent = formatCurrency(bill.paidAmount);
    document.getElementById('billBalance').textContent = formatCurrency(bill.balanceDue || (bill.totalAmount - bill.paidAmount));

    // Update status
    const statusEl = document.getElementById('billStatus');
    statusEl.textContent = bill.paymentStatus || 'UNPAID';
    statusEl.className = getStatusBadgeClass(bill.paymentStatus);

    // Store bill ID for payment
    document.getElementById('paymentBillId').value = bill.billId;
    document.getElementById('paymentAmount').value = bill.balanceDue || (bill.totalAmount - bill.paidAmount);
}

// Print bill
function printBill() {
    if (!currentBill) {
        alert('Please generate a bill first.');
        return;
    }

    // Open print window
    window.open(`/sunrise-dental-clinic/api/reports/print/${currentBill.billNumber}`, '_blank');
}

// Process payment
function processPayment() {
    if (!currentBill) {
        alert('Please generate a bill first.');
        return;
    }

    // Check if bill is already paid
    if (currentBill.paymentStatus === 'PAID') {
        alert('This bill is already fully paid.');
        return;
    }

    document.getElementById('paymentModal').classList.add('show');
    document.getElementById('paymentAmount').value = currentBill.balanceDue || (currentBill.totalAmount - currentBill.paidAmount);
}

// Close payment modal
function closePaymentModal() {
    document.getElementById('paymentModal').classList.remove('show');
    clearError('paymentError');
}

// Setup payment form
function setupPaymentForm() {
    const form = document.getElementById('paymentForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await processPaymentSubmit();
        });
    }
}

// Process payment submission
async function processPaymentSubmit() {
    const billId = document.getElementById('paymentBillId').value;
    const amount = parseFloat(document.getElementById('paymentAmount').value);
    const method = document.getElementById('paymentMethod').value;
    const reference = document.getElementById('paymentReference').value.trim();

    clearError('paymentError');

    if (!amount || amount <= 0) {
        showError('paymentError', 'Please enter a valid payment amount.');
        return;
    }

    const paymentData = {
        amount: amount,
        paymentMethod: method,
        referenceNumber: reference || null,
        createdBy: 1 // This should come from session
    };

    try {
        const response = await apiPost(`/bills/${billId}/pay`, paymentData);
        if (response.status === 200) {
            closePaymentModal();
            currentBill = response.data;
            displayBill(currentBill);
            showSuccess('Payment processed successfully!');
        } else {
            const errorMsg = response.data && response.data.error ? response.data.error : 'Failed to process payment';
            showError('paymentError', errorMsg);
        }
    } catch (error) {
        console.error('Error processing payment:', error);
        showError('paymentError', 'Failed to process payment. Please try again.');
    }
}

// Clear bill
function clearBill() {
    document.getElementById('billResult').style.display = 'none';
    document.getElementById('billAppointmentNumber').value = '';
    document.getElementById('billDiscount').value = '0';
    document.getElementById('billTax').value = '0';
    currentBill = null;
}