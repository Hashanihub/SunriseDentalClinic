/**
 * Reports Logic
 * Sunrise Dental Clinic Management System
 */

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('reports.html')) {
        // Set default dates
        const today = new Date();
        const thirtyDaysAgo = new Date();
        thirtyDaysAgo.setDate(today.getDate() - 30);

        const dateInputs = document.querySelectorAll('input[type="date"]');
        dateInputs.forEach(input => {
            if (input.id && input.id.includes('Start') || input.id && input.id.includes('End')) {
                if (input.id.includes('Start')) {
                    input.value = thirtyDaysAgo.toISOString().split('T')[0];
                } else {
                    input.value = today.toISOString().split('T')[0];
                }
            }
        });

        // Load dentist dropdown for report
        loadDentistsForReport();
    }
});

// Show report tab
function showReport(tabName) {
    // Hide all report contents
    document.querySelectorAll('.report-content').forEach(el => {
        el.style.display = 'none';
    });

    // Remove active class from all tabs
    document.querySelectorAll('.tab-btn').forEach(el => {
        el.classList.remove('active');
    });

    // Show selected report
    const content = document.getElementById(tabName + 'Report');
    if (content) {
        content.style.display = 'block';
    }

    // Activate tab
    const tabs = document.querySelectorAll('.tab-btn');
    const tabMap = {
        'daily': 0,
        'dentist': 1,
        'revenue': 2,
        'payment': 3,
        'patient': 4
    };
    const index = tabMap[tabName] || 0;
    if (tabs[index]) {
        tabs[index].classList.add('active');
    }
}

// ==================== DAILY APPOINTMENTS REPORT ====================

function loadDailyReport() {
    const date = document.getElementById('dailyDate').value;
    if (!date) {
        alert('Please select a date.');
        return;
    }

    const tableBody = document.getElementById('dailyReportBody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading...</td></tr>';

    apiGet(`/reports/daily-appointments?date=${date}`)
        .then(response => {
            if (response.status === 200) {
                const appointments = response.data || [];
                renderDailyReport(appointments);
            } else {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Failed to load data</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error loading daily report:', error);
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Error loading data</td></tr>';
        });
}

function renderDailyReport(appointments) {
    const tableBody = document.getElementById('dailyReportBody');

    if (appointments.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No appointments found for this date</td></tr>';
        return;
    }

    tableBody.innerHTML = appointments.map(app => `
        <tr>
            <td>${app.appointmentNumber || '-'}</td>
            <td>${app.patient ? app.patient.fullName : `Patient #${app.patientId}`}</td>
            <td>${app.dentist ? app.dentist.fullName : `Dentist #${app.dentistId}`}</td>
            <td>${app.treatment ? app.treatment.treatmentName : `Treatment #${app.treatmentId}`}</td>
            <td>${formatTime(app.appointmentTime)}</td>
            <td><span class="${getStatusBadgeClass(app.status)}">${app.status || 'SCHEDULED'}</span></td>
        </tr>
    `).join('');
}

// ==================== DENTIST REPORT ====================

function loadDentistsForReport() {
    const select = document.getElementById('dentistSelect');
    if (!select) return;

    apiGet('/dentists')
        .then(response => {
            if (response.status === 200) {
                populateSelect('dentistSelect', response.data, 'dentistId', 'fullName', 'Select Dentist');
            }
        })
        .catch(error => console.error('Error loading dentists:', error));
}

function loadDentistReport() {
    const dentistId = document.getElementById('dentistSelect').value;
    const startDate = document.getElementById('dentistStartDate').value;
    const endDate = document.getElementById('dentistEndDate').value;

    if (!dentistId) {
        alert('Please select a dentist.');
        return;
    }
    if (!startDate || !endDate) {
        alert('Please select start and end dates.');
        return;
    }

    const tableBody = document.getElementById('dentistReportBody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading...</td></tr>';

    apiGet(`/reports/dentist-appointments?dentistId=${dentistId}&startDate=${startDate}&endDate=${endDate}`)
        .then(response => {
            if (response.status === 200) {
                const appointments = response.data || [];
                renderDentistReport(appointments);
            } else {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Failed to load data</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error loading dentist report:', error);
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Error loading data</td></tr>';
        });
}

function renderDentistReport(appointments) {
    const tableBody = document.getElementById('dentistReportBody');

    if (appointments.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No appointments found</td></tr>';
        return;
    }

    tableBody.innerHTML = appointments.map(app => `
        <tr>
            <td>${app.appointmentNumber || '-'}</td>
            <td>${app.patient ? app.patient.fullName : `Patient #${app.patientId}`}</td>
            <td>${app.treatment ? app.treatment.treatmentName : `Treatment #${app.treatmentId}`}</td>
            <td>${formatDate(app.appointmentDate)}</td>
            <td>${formatTime(app.appointmentTime)}</td>
            <td><span class="${getStatusBadgeClass(app.status)}">${app.status || 'SCHEDULED'}</span></td>
        </tr>
    `).join('');
}

// ==================== REVENUE REPORT ====================

function loadRevenueReport() {
    const startDate = document.getElementById('revenueStartDate').value;
    const endDate = document.getElementById('revenueEndDate').value;

    if (!startDate || !endDate) {
        alert('Please select start and end dates.');
        return;
    }

    const tableBody = document.getElementById('revenueReportBody');
    tableBody.innerHTML = '<tr><td colspan="3" class="text-center">Loading...</td></tr>';

    apiGet(`/reports/revenue?startDate=${startDate}&endDate=${endDate}`)
        .then(response => {
            if (response.status === 200) {
                const data = response.data || [];
                renderRevenueReport(data);
            } else {
                tableBody.innerHTML = '<tr><td colspan="3" class="text-center">Failed to load data</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error loading revenue report:', error);
            tableBody.innerHTML = '<tr><td colspan="3" class="text-center">Error loading data</td></tr>';
        });
}

function renderRevenueReport(data) {
    const tableBody = document.getElementById('revenueReportBody');

    if (data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="3" class="text-center">No revenue data found</td></tr>';
        return;
    }

    let totalRevenue = 0;
    tableBody.innerHTML = data.map(item => {
        totalRevenue += item.revenue || 0;
        return `
            <tr>
                <td>${item.treatmentName || '-'}</td>
                <td>${item.count || 0}</td>
                <td>${formatCurrency(item.revenue)}</td>
            </tr>
        `;
    }).join('');

    // Add total row
    tableBody.innerHTML += `
        <tr style="font-weight:bold;border-top:2px solid #333;">
            <td>TOTAL</td>
            <td>-</td>
            <td>${formatCurrency(totalRevenue)}</td>
        </tr>
    `;
}

// ==================== PAYMENT REPORT ====================

function loadPaymentReport() {
    const startDate = document.getElementById('paymentStartDate').value;
    const endDate = document.getElementById('paymentEndDate').value;

    if (!startDate || !endDate) {
        alert('Please select start and end dates.');
        return;
    }

    const tableBody = document.getElementById('paymentReportBody');
    tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Loading...</td></tr>';

    apiGet(`/reports/payments?startDate=${startDate}&endDate=${endDate}`)
        .then(response => {
            if (response.status === 200) {
                const bills = response.data || [];
                renderPaymentReport(bills);
            } else {
                tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Failed to load data</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error loading payment report:', error);
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Error loading data</td></tr>';
        });
}

function renderPaymentReport(bills) {
    const tableBody = document.getElementById('paymentReportBody');

    if (bills.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center">No payment data found</td></tr>';
        return;
    }

    tableBody.innerHTML = bills.map(bill => `
        <tr>
            <td>${bill.billNumber || '-'}</td>
            <td>${bill.patient ? bill.patient.fullName : `Patient #${bill.patientId}`}</td>
            <td>${formatCurrency(bill.totalAmount)}</td>
            <td><span class="${getStatusBadgeClass(bill.paymentStatus)}">${bill.paymentStatus || 'UNPAID'}</span></td>
            <td>${formatDate(bill.billDate)}</td>
        </tr>
    `).join('');
}

// ==================== PATIENT REPORT ====================

function loadPatientReport() {
    const startDate = document.getElementById('patientStartDate').value;
    const endDate = document.getElementById('patientEndDate').value;

    if (!startDate || !endDate) {
        alert('Please select start and end dates.');
        return;
    }

    const tableBody = document.getElementById('patientReportBody');
    tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Loading...</td></tr>';

    apiGet(`/reports/patients?startDate=${startDate}&endDate=${endDate}`)
        .then(response => {
            if (response.status === 200) {
                const patients = response.data || [];
                renderPatientReport(patients);
            } else {
                tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Failed to load data</td></tr>';
            }
        })
        .catch(error => {
            console.error('Error loading patient report:', error);
            tableBody.innerHTML = '<tr><td colspan="5" class="text-center">Error loading data</td></tr>';
        });
}

function renderPatientReport(patients) {
    const tableBody = document.getElementById('patientReportBody');

    if (patients.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="5" class="text-center">No patients found</td></tr>';
        return;
    }

    tableBody.innerHTML = patients.map(patient => `
        <tr>
            <td>${patient.patientNumber || '-'}</td>
            <td>${patient.fullName || '-'}</td>
            <td>${patient.contactNumber || '-'}</td>
            <td>${formatDate(patient.registrationDate)}</td>
            <td><span class="status-badge ${patient.active ? 'status-paid' : 'status-cancelled'}">${patient.active ? 'Active' : 'Inactive'}</span></td>
        </tr>
    `).join('');
}