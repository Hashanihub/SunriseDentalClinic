/**
 * Dashboard Logic
 * Sunrise Dental Clinic Management System
 */

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('dashboard.html')) {
        loadDashboardData();
        loadTodayAppointments();
        setInterval(loadDashboardData, 60000);
        setInterval(loadTodayAppointments, 60000);
    }
});

// Load dashboard statistics
async function loadDashboardData() {
    try {
        const response = await apiGet('/reports/dashboard-stats');
        if (response.status === 200) {
            const data = response.data;

            document.getElementById('todayAppointments').textContent = data.todayAppointments || 0;
            document.getElementById('completedAppointments').textContent = data.completedAppointments || 0;
            document.getElementById('pendingAppointments').textContent = data.pendingAppointments || 0;
            document.getElementById('cancelledAppointments').textContent = data.cancelledAppointments || 0;
            document.getElementById('totalPatients').textContent = data.totalPatients || 0;
            document.getElementById('todayRevenue').textContent = formatCurrency(data.todayRevenue);
        }
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
    }
}

// Load today's appointments
async function loadTodayAppointments() {
    const tableBody = document.getElementById('todayAppointmentsTable');
    if (!tableBody) return;

    try {
        const today = new Date().toISOString().split('T')[0];
        const response = await apiGet(`/appointments?date=${today}`);

        if (response.status === 200) {
            const appointments = response.data || [];

            if (appointments.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No appointments for today</td></tr>';
                return;
            }

            tableBody.innerHTML = appointments.map(app => {
                const patientName = app.patient ? app.patient.fullName : `Patient #${app.patientId}`;
                const dentistName = app.dentist ? app.dentist.fullName : `Dentist #${app.dentistId}`;
                const treatmentName = app.treatment ? app.treatment.treatmentName : `Treatment #${app.treatmentId}`;

                return `
                    <tr>
                        <td>${app.appointmentNumber || '-'}</td>
                        <td>${patientName}</td>
                        <td>${dentistName}</td>
                        <td>${treatmentName}</td>
                        <td>${formatTime(app.appointmentTime)}</td>
                        <td><span class="${getStatusBadgeClass(app.status)}">${app.status || 'SCHEDULED'}</span></td>
                    </tr>
                `;
            }).join('');
        } else {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Failed to load appointments</td></tr>';
        }
    } catch (error) {
        console.error('Error loading today\'s appointments:', error);
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Error loading appointments</td></tr>';
    }
}