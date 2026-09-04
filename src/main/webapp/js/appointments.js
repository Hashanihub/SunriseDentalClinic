/**
 * Appointment Management Logic
 * Sunrise Dental Clinic Management System
 */

let currentAppointments = [];

document.addEventListener('DOMContentLoaded', function () {

    if (window.location.pathname.includes('appointments.html')) {

        // Set default date filter
        const filterDate = document.getElementById('filterDate');

        if (filterDate) {
            const today = new Date().toISOString().split('T')[0];
            filterDate.value = today;
        }

        loadAppointments();
        loadDentistDropdown();
        loadTreatmentDropdown();
        setupAppointmentForm();
        setupAppointmentFilters();
    }

    if (window.location.pathname.includes('appointment-search.html')) {
        setupSearch();
    }
});


// ==================== HELPER FUNCTIONS ====================

/**
 * Safely format appointment time.
 *
 * Backend may return LocalTime as:
 * 1. String: "10:30:00"
 * 2. Array: [10, 30, 0]
 * 3. Object: { hour: 10, minute: 30, second: 0 }
 */
function safeFormatTime(time) {

    if (time === null || time === undefined || time === '') {
        return '-';
    }

    // String format: "10:30:00"
    if (typeof time === 'string') {
        const parts = time.split(':');

        if (parts.length >= 2) {
            return `${parts[0].padStart(2, '0')}:${parts[1].padStart(2, '0')}`;
        }

        return time;
    }

    // Array format: [10, 30, 0]
    if (Array.isArray(time)) {

        const hour = Number(time[0] ?? 0);
        const minute = Number(time[1] ?? 0);

        return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
    }

    // Object format: {hour: 10, minute: 30}
    if (typeof time === 'object') {

        const hour = Number(time.hour ?? 0);
        const minute = Number(time.minute ?? 0);

        return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
    }

    return '-';
}


/**
 * Convert appointment time to HTML input time format.
 */
function formatTimeForInput(time) {

    if (time === null || time === undefined || time === '') {
        return '';
    }

    if (typeof time === 'string') {
        return time.substring(0, 5);
    }

    if (Array.isArray(time)) {

        const hour = Number(time[0] ?? 0);
        const minute = Number(time[1] ?? 0);

        return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
    }

    if (typeof time === 'object') {

        const hour = Number(time.hour ?? 0);
        const minute = Number(time.minute ?? 0);

        return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
    }

    return '';
}


/**
 * Show error message.
 * Only creates it if another JS file has not already defined it.
 */
if (typeof window.showError !== 'function') {
    window.showError = function (elementId, message) {

        const errorElement = document.getElementById(elementId);

        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        } else {
            console.error(message);
        }
    };
}


/**
 * Clear error message.
 */
if (typeof window.clearError !== 'function') {
    window.clearError = function (elementId) {

        const errorElement = document.getElementById(elementId);

        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
    };
}


/**
 * Show success message.
 */
if (typeof window.showSuccess !== 'function') {
    window.showSuccess = function (message) {
        console.log('Success:', message);
        alert(message);
    };
}


// ==================== APPOINTMENTS LIST ====================

async function loadAppointments() {

    const tableBody = document.getElementById('appointmentsTableBody');

    if (!tableBody) return;

    tableBody.innerHTML =
        '<tr><td colspan="8" class="text-center">Loading appointments...</td></tr>';

    try {

        const date = document.getElementById('filterDate')?.value;
        const status = document.getElementById('filterStatus')?.value;

        let url = '/appointments';

        const params = [];

        if (date) {
            params.push(`date=${date}`);
        }

        if (status) {
            params.push(`status=${status}`);
        }

        if (params.length) {
            url += '?' + params.join('&');
        }

        console.log('📋 Fetching appointments from:', url);

        const response = await apiGet(url);

        console.log('📋 Response status:', response.status);
        console.log('📋 Response data:', response.data);

        if (response.status === 200) {

            currentAppointments = response.data || [];

            console.log(
                '📋 Appointments loaded:',
                currentAppointments.length
            );

            renderAppointments(currentAppointments);

            const countElement =
                document.getElementById('appointmentCount');

            if (countElement) {
                countElement.textContent =
                    `${currentAppointments.length} appointments`;
            }

        } else if (response.status === 401) {

            tableBody.innerHTML =
                '<tr><td colspan="8" class="text-center">Please login again</td></tr>';

        } else {

            tableBody.innerHTML =
                '<tr><td colspan="8" class="text-center">' +
                'Failed to load appointments (Status: ' +
                response.status +
                ')</td></tr>';
        }

    } catch (error) {

        console.error('❌ Error loading appointments:', error);

        tableBody.innerHTML =
            '<tr><td colspan="8" class="text-center">' +
            'Error loading appointments: ' +
            error.message +
            '</td></tr>';
    }
}


// ==================== RENDER APPOINTMENTS ====================

function renderAppointments(appointments) {

    const tableBody =
        document.getElementById('appointmentsTableBody');

    if (!tableBody) return;

    if (!appointments || appointments.length === 0) {

        tableBody.innerHTML =
            '<tr><td colspan="8" class="text-center">' +
            'No appointments found for this date' +
            '</td></tr>';

        return;
    }

    tableBody.innerHTML = appointments.map(app => {

        const patientName =
            app.patient
                ? app.patient.fullName
                : `Patient #${app.patientId}`;

        const dentistName =
            app.dentist
                ? app.dentist.fullName
                : `Dentist #${app.dentistId}`;

        const treatmentName =
            app.treatment
                ? app.treatment.treatmentName
                : `Treatment #${app.treatmentId}`;

        return `
            <tr>

                <td>
                    <strong>${app.appointmentNumber || '-'}</strong>
                </td>

                <td>
                    ${patientName}
                </td>

                <td>
                    ${dentistName}
                </td>

                <td>
                    ${treatmentName}
                </td>

                <td>
                    ${formatDate(app.appointmentDate)}
                </td>

                <td>
                    ${safeFormatTime(app.appointmentTime)}
                </td>

                <td>

                    <select
                        class="status-select"
                        onchange="updateStatus(${app.appointmentId}, this.value)"
                    >

                        <option
                            value="SCHEDULED"
                            ${app.status === 'SCHEDULED' ? 'selected' : ''}
                        >
                            Scheduled
                        </option>

                        <option
                            value="CONFIRMED"
                            ${app.status === 'CONFIRMED' ? 'selected' : ''}
                        >
                            Confirmed
                        </option>

                        <option
                            value="COMPLETED"
                            ${app.status === 'COMPLETED' ? 'selected' : ''}
                        >
                            Completed
                        </option>

                        <option
                            value="CANCELLED"
                            ${app.status === 'CANCELLED' ? 'selected' : ''}
                        >
                            Cancelled
                        </option>

                        <option
                            value="NO_SHOW"
                            ${app.status === 'NO_SHOW' ? 'selected' : ''}
                        >
                            No Show
                        </option>

                    </select>

                </td>

                <td>

                    <button
                        class="btn-sm btn-secondary"
                        onclick="editAppointment(${app.appointmentId})"
                    >
                        ✏️
                    </button>

                    <button
                        class="btn-sm btn-danger"
                        onclick="cancelAppointment(${app.appointmentId})"
                    >
                        ❌
                    </button>

                </td>

            </tr>
        `;

    }).join('');
}


// ==================== FILTERS ====================

function setupAppointmentFilters() {

    const filterDate =
        document.getElementById('filterDate');

    const filterStatus =
        document.getElementById('filterStatus');

    if (filterDate) {

        filterDate.addEventListener('change', function () {

            console.log(
                '📅 Date filter changed to:',
                this.value
            );

            loadAppointments();
        });
    }

    if (filterStatus) {

        filterStatus.addEventListener('change', function () {

            console.log(
                '📋 Status filter changed to:',
                this.value
            );

            loadAppointments();
        });
    }
}


// ==================== UPDATE STATUS ====================

async function updateStatus(appointmentId, status) {

    try {

        const response =
            await apiPut(
                `/appointments/${appointmentId}/status?status=${status}`
            );

        if (response.status === 200) {

            showSuccess('Status updated successfully!');

            await loadAppointments();

        } else {

            alert('Failed to update status');
        }

    } catch (error) {

        console.error(
            'Error updating status:',
            error
        );

        alert('Failed to update status');
    }
}


// ==================== DENTIST DROPDOWN ====================

async function loadDentistDropdown() {

    try {

        console.log('🦷 Loading dentists...');

        const response =
            await apiGet('/dentists/available');

        console.log(
            '🦷 Dentist response:',
            response
        );

        if (response.status === 200) {

            const dentists =
                response.data || [];

            const select =
                document.getElementById('dentistId');

            if (!select) return;

            select.innerHTML =
                '<option value="">Select Dentist</option>';

            dentists.forEach(dentist => {

                const option =
                    document.createElement('option');

                option.value =
                    dentist.dentistId;

                option.textContent =
                    dentist.fullName +
                    ' (' +
                    dentist.specialization +
                    ')';

                select.appendChild(option);
            });
        }

    } catch (error) {

        console.error(
            'Error loading dentists:',
            error
        );
    }
}


// ==================== TREATMENT DROPDOWN ====================

async function loadTreatmentDropdown() {

    try {

        console.log('💊 Loading treatments...');

        const response =
            await apiGet('/treatments/active');

        console.log(
            '💊 Treatment response:',
            response
        );

        if (response.status === 200) {

            const treatments =
                response.data || [];

            const select =
                document.getElementById('treatmentId');

            if (!select) return;

            select.innerHTML =
                '<option value="">Select Treatment</option>';

            treatments.forEach(treatment => {

                const option =
                    document.createElement('option');

                option.value =
                    treatment.treatmentId;

                const cost =
                    treatment.treatmentCost
                        ? ' (LKR ' +
                        treatment.treatmentCost +
                        ')'
                        : '';

                option.textContent =
                    treatment.treatmentName +
                    cost;

                select.appendChild(option);
            });
        }

    } catch (error) {

        console.error(
            'Error loading treatments:',
            error
        );
    }
}


// ==================== APPOINTMENT MODAL ====================

function openAppointmentModal(appointmentId = null) {

    const modal =
        document.getElementById('appointmentModal');

    const form =
        document.getElementById('appointmentForm');

    const title =
        document.getElementById('modalTitle');

    if (!modal || !form) return;

    form.reset();

    const appointmentIdElement =
        document.getElementById('appointmentId');

    if (appointmentIdElement) {
        appointmentIdElement.value = '';
    }

    clearError('appointmentFormError');

    const today =
        new Date().toISOString().split('T')[0];

    const dateElement =
        document.getElementById('appointmentDate');

    if (dateElement) {
        dateElement.value = today;
    }

    loadDentistDropdown();
    loadTreatmentDropdown();

    if (appointmentId) {

        if (title) {
            title.textContent = 'Edit Appointment';
        }

        loadAppointmentData(appointmentId);

    } else {

        if (title) {
            title.textContent = 'Create Appointment';
        }
    }

    modal.classList.add('show');
}


function closeAppointmentModal() {

    const modal =
        document.getElementById('appointmentModal');

    if (modal) {
        modal.classList.remove('show');
    }
}


// ==================== LOAD APPOINTMENT DATA ====================

async function loadAppointmentData(appointmentId) {

    try {

        const response =
            await apiGet(`/appointments/${appointmentId}`);

        if (response.status === 200) {

            const app = response.data;

            document.getElementById('appointmentId').value =
                app.appointmentId || '';

            document.getElementById('patientNumber').value =
                app.patient
                    ? app.patient.patientNumber
                    : '';

            document.getElementById('dentistId').value =
                app.dentistId || '';

            document.getElementById('treatmentId').value =
                app.treatmentId || '';

            document.getElementById('appointmentDate').value =
                formatDateInput(app.appointmentDate);

            // FIXED: Handle LocalTime array/object/string
            document.getElementById('appointmentTime').value =
                formatTimeForInput(app.appointmentTime);

            document.getElementById('appointmentNotes').value =
                app.notes || '';
        }

    } catch (error) {

        console.error(
            'Error loading appointment:',
            error
        );

        showError(
            'appointmentFormError',
            'Failed to load appointment data'
        );
    }
}


// ==================== APPOINTMENT FORM ====================

function setupAppointmentForm() {

    const form =
        document.getElementById('appointmentForm');

    if (form) {

        form.addEventListener(
            'submit',
            async function (e) {

                e.preventDefault();

                await saveAppointment();
            }
        );
    }
}


// ==================== SAVE APPOINTMENT ====================

async function saveAppointment() {

    const appointmentId =
        document.getElementById('appointmentId').value;

    const data = {

        patientNumber:
            document
                .getElementById('patientNumber')
                .value
                .trim(),

        dentistId:
            parseInt(
                document
                    .getElementById('dentistId')
                    .value
            ),

        treatmentId:
            parseInt(
                document
                    .getElementById('treatmentId')
                    .value
            ),

        appointmentDate:
        document
            .getElementById('appointmentDate')
            .value,

        appointmentTime:
        document
            .getElementById('appointmentTime')
            .value,

        notes:
            document
                .getElementById('appointmentNotes')
                .value
                .trim()
    };

    clearError('appointmentFormError');

    if (!data.patientNumber) {

        showError(
            'appointmentFormError',
            'Patient number is required.'
        );

        return;
    }

    if (!data.dentistId) {

        showError(
            'appointmentFormError',
            'Please select a dentist.'
        );

        return;
    }

    if (!data.treatmentId) {

        showError(
            'appointmentFormError',
            'Please select a treatment.'
        );

        return;
    }

    if (!data.appointmentDate) {

        showError(
            'appointmentFormError',
            'Appointment date is required.'
        );

        return;
    }

    if (!data.appointmentTime) {

        showError(
            'appointmentFormError',
            'Appointment time is required.'
        );

        return;
    }

    try {

        console.log(
            '📝 Saving appointment:',
            data
        );

        let response;

        if (appointmentId) {

            response =
                await apiPut(
                    `/appointments/${appointmentId}`,
                    data
                );

        } else {

            response =
                await apiPost(
                    '/appointments',
                    data
                );
        }

        console.log(
            '📝 Save response:',
            response
        );

        if (
            response.status === 200 ||
            response.status === 201
        ) {

            closeAppointmentModal();

            showSuccess(
                appointmentId
                    ? 'Appointment updated successfully!'
                    : 'Appointment created successfully!'
            );

            await loadAppointments();

        } else {

            const errorMsg =
                response.data &&
                response.data.error
                    ? response.data.error
                    : 'Failed to save appointment';

            showError(
                'appointmentFormError',
                errorMsg
            );
        }

    } catch (error) {

        console.error(
            '❌ Error saving appointment:',
            error
        );

        showError(
            'appointmentFormError',
            'Failed to save appointment. Please try again.'
        );
    }
}


// ==================== EDIT APPOINTMENT ====================

function editAppointment(appointmentId) {
    openAppointmentModal(appointmentId);
}


// ==================== CANCEL APPOINTMENT ====================

async function cancelAppointment(appointmentId) {

    const reason =
        prompt('Please enter cancellation reason:');

    if (reason === null) {
        return;
    }

    if (!reason.trim()) {

        alert(
            'Cancellation reason is required.'
        );

        return;
    }

    try {

        const response =
            await apiDelete(
                `/appointments/${appointmentId}?reason=${encodeURIComponent(reason)}`
            );

        if (response.status === 200) {

            await loadAppointments();

            showSuccess(
                'Appointment cancelled successfully!'
            );

        } else {

            alert(
                'Failed to cancel appointment'
            );
        }

    } catch (error) {

        console.error(
            'Error cancelling appointment:',
            error
        );

        alert(
            'Failed to cancel appointment'
        );
    }
}


// ==================== APPOINTMENT SEARCH ====================

function setupSearch() {

    const searchInput =
        document.getElementById(
            'searchAppointmentNumber'
        );

    if (searchInput) {

        searchInput.addEventListener(
            'keypress',
            function (e) {

                if (e.key === 'Enter') {
                    searchAppointment();
                }
            }
        );
    }
}


// ==================== SEARCH APPOINTMENT ====================

async function searchAppointment() {

    const number =
        document
            .getElementById(
                'searchAppointmentNumber'
            )
            .value
            .trim();

    const resultDiv =
        document.getElementById('searchResult');

    const notFoundDiv =
        document.getElementById('notFound');

    resultDiv.style.display = 'none';
    notFoundDiv.style.display = 'none';

    if (!number) {

        alert(
            'Please enter an appointment number.'
        );

        return;
    }

    try {

        console.log(
            '🔍 Searching appointment:',
            number
        );

        const response =
            await apiGet(
                `/appointments/search?number=${number}`
            );

        console.log(
            '🔍 Search response:',
            response
        );

        if (response.status === 200) {

            const app = response.data;

            displayAppointmentDetails(app);

            resultDiv.style.display = 'block';

        } else if (response.status === 404) {

            notFoundDiv.style.display = 'block';

        } else {

            alert(
                'Failed to search appointment'
            );
        }

    } catch (error) {

        console.error(
            'Error searching appointment:',
            error
        );

        alert(
            'Failed to search appointment'
        );
    }
}


// ==================== DISPLAY APPOINTMENT DETAILS ====================

function displayAppointmentDetails(app) {

    document.getElementById(
        'resultNumber'
    ).textContent =
        app.appointmentNumber || '-';

    document.getElementById(
        'resultDate'
    ).textContent =
        formatDate(app.appointmentDate);

    // FIXED: Safe time formatting
    document.getElementById(
        'resultTime'
    ).textContent =
        safeFormatTime(app.appointmentTime);

    document.getElementById(
        'resultStatus'
    ).textContent =
        app.status || 'SCHEDULED';

    document.getElementById(
        'resultStatus'
    ).className =
        getStatusBadgeClass(app.status);

    const patient =
        app.patient || {};

    document.getElementById(
        'resultPatient'
    ).textContent =
        patient.fullName ||
        `Patient #${app.patientId}`;

    document.getElementById(
        'resultPatientId'
    ).textContent =
        patient.patientNumber ||
        app.patientId ||
        '-';

    document.getElementById(
        'resultContact'
    ).textContent =
        patient.contactNumber || '-';

    const dentist =
        app.dentist || {};

    document.getElementById(
        'resultDentist'
    ).textContent =
        dentist.fullName ||
        `Dentist #${app.dentistId}`;

    const treatment =
        app.treatment || {};

    document.getElementById(
        'resultTreatment'
    ).textContent =
        treatment.treatmentName ||
        `Treatment #${app.treatmentId}`;

    document.getElementById(
        'resultNotes'
    ).textContent =
        app.notes || '-';
}


// ==================== CLEAR SEARCH ====================

function clearSearch() {

    document.getElementById(
        'searchAppointmentNumber'
    ).value = '';

    document.getElementById(
        'searchResult'
    ).style.display = 'none';

    document.getElementById(
        'notFound'
    ).style.display = 'none';
}