/**
 * Patient Management Logic
 * Sunrise Dental Clinic Management System
 */

let currentPatients = [];

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('patients.html')) {
        loadPatients();
        setupPatientSearch();
        setupPatientForm();
    }
});

// Load all patients
async function loadPatients() {
    const tableBody = document.getElementById('patientsTableBody');
    if (!tableBody) return;

    showLoading('patientsTableBody');

    try {
        const response = await apiGet('/patients');
        if (response.status === 200) {
            currentPatients = response.data || [];
            renderPatients(currentPatients);
            document.getElementById('patientCount').textContent = `${currentPatients.length} patients`;
        } else {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Failed to load patients</td></tr>';
        }
    } catch (error) {
        console.error('Error loading patients:', error);
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Error loading patients</td></tr>';
    }
}

// Render patients table
function renderPatients(patients) {
    const tableBody = document.getElementById('patientsTableBody');
    if (!tableBody) return;

    if (patients.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No patients found</td></tr>';
        return;
    }

    tableBody.innerHTML = patients.map(patient => `
        <tr>
            <td><strong>${patient.patientNumber || '-'}</strong></td>
            <td>${patient.fullName || '-'}</td>
            <td>${patient.contactNumber || '-'}</td>
            <td>${patient.email || '-'}</td>
            <td>${formatDate(patient.registrationDate)}</td>
            <td>
                <button class="btn-sm btn-primary" onclick="editPatient(${patient.patientId})">✏️</button>
                <button class="btn-sm btn-danger" onclick="deletePatient(${patient.patientId})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// Search patients
function searchPatients() {
    const searchTerm = document.getElementById('searchPatient').value.trim();
    if (!searchTerm) {
        renderPatients(currentPatients);
        return;
    }

    const filtered = currentPatients.filter(p =>
        p.fullName && p.fullName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    renderPatients(filtered);
}

// Setup patient search
function setupPatientSearch() {
    const searchInput = document.getElementById('searchPatient');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') {
                searchPatients();
            }
        });
    }
}

// Open patient modal
function openPatientModal(patientId = null) {
    const modal = document.getElementById('patientModal');
    const form = document.getElementById('patientForm');
    const title = document.getElementById('modalTitle');

    form.reset();
    document.getElementById('patientId').value = '';
    clearError('patientFormError');

    if (patientId) {
        title.textContent = 'Edit Patient';
        loadPatientData(patientId);
    } else {
        title.textContent = 'Register Patient';
    }

    modal.classList.add('show');
}

// Close patient modal
function closePatientModal() {
    document.getElementById('patientModal').classList.remove('show');
}

// Load patient data for editing
async function loadPatientData(patientId) {
    try {
        const response = await apiGet(`/patients/${patientId}`);
        if (response.status === 200) {
            const patient = response.data;
            document.getElementById('patientId').value = patient.patientId;
            document.getElementById('fullName').value = patient.fullName || '';
            document.getElementById('contactNumber').value = patient.contactNumber || '';
            document.getElementById('address').value = patient.address || '';
            document.getElementById('email').value = patient.email || '';
            document.getElementById('dateOfBirth').value = patient.dateOfBirth ? formatDateInput(patient.dateOfBirth) : '';
            document.getElementById('gender').value = patient.gender || '';
            document.getElementById('notes').value = patient.notes || '';
        }
    } catch (error) {
        console.error('Error loading patient:', error);
        showError('patientFormError', 'Failed to load patient data');
    }
}

// Setup patient form submission
function setupPatientForm() {
    const form = document.getElementById('patientForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await savePatient();
        });
    }
}

// Save patient (create or update)
async function savePatient() {
    const patientId = document.getElementById('patientId').value;
    const data = {
        fullName: document.getElementById('fullName').value.trim(),
        contactNumber: document.getElementById('contactNumber').value.trim(),
        address: document.getElementById('address').value.trim(),
        email: document.getElementById('email').value.trim(),
        dateOfBirth: document.getElementById('dateOfBirth').value || null,
        gender: document.getElementById('gender').value || null,
        notes: document.getElementById('notes').value.trim()
    };

    clearError('patientFormError');

    // Validate
    if (!data.fullName) {
        showError('patientFormError', 'Full name is required.');
        return;
    }
    if (!data.contactNumber) {
        showError('patientFormError', 'Contact number is required.');
        return;
    }

    try {
        let response;
        if (patientId) {
            // Update existing patient
            response = await apiPut(`/patients/${patientId}`, data);
        } else {
            // Create new patient
            response = await apiPost('/patients', data);
        }

        if (response.status === 200 || response.status === 201) {
            closePatientModal();
            await loadPatients();
            showSuccess(patientId ? 'Patient updated successfully!' : 'Patient registered successfully!');
        } else {
            const errorMsg = response.data && response.data.error ? response.data.error : 'Failed to save patient';
            showError('patientFormError', errorMsg);
        }
    } catch (error) {
        console.error('Error saving patient:', error);
        showError('patientFormError', 'Failed to save patient. Please try again.');
    }
}

// Edit patient
function editPatient(patientId) {
    openPatientModal(patientId);
}

// Delete patient (deactivate)
async function deletePatient(patientId) {
    if (!confirm('Are you sure you want to deactivate this patient?')) {
        return;
    }

    try {
        const response = await apiDelete(`/patients/${patientId}`);
        if (response.status === 200) {
            await loadPatients();
            showSuccess('Patient deactivated successfully!');
        } else {
            alert('Failed to deactivate patient');
        }
    } catch (error) {
        console.error('Error deleting patient:', error);
        alert('Failed to deactivate patient. Please try again.');
    }
}