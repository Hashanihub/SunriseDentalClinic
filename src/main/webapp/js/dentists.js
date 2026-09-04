/**
 * Dentist Management Logic
 * Sunrise Dental Clinic Management System
 */

let currentDentists = [];

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('dentists.html')) {
        loadDentists();
        setupDentistSearch();
        setupDentistForm();
        toggleAdminElements();
    }
});

// Load dentists
async function loadDentists() {
    const tableBody = document.getElementById('dentistsTableBody');
    if (!tableBody) return;

    try {
        const response = await apiGet('/dentists');
        if (response.status === 200) {
            currentDentists = response.data || [];
            renderDentists(currentDentists);
            document.getElementById('dentistCount').textContent = `${currentDentists.length} dentists`;
        } else {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Failed to load dentists</td></tr>';
        }
    } catch (error) {
        console.error('Error loading dentists:', error);
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Error loading dentists</td></tr>';
    }
}

// Render dentists table
function renderDentists(dentists) {
    const tableBody = document.getElementById('dentistsTableBody');
    if (!tableBody) return;

    if (dentists.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No dentists found</td></tr>';
        return;
    }

    tableBody.innerHTML = dentists.map(d => `
        <tr>
            <td><strong>${d.dentistNumber || '-'}</strong></td>
            <td>${d.fullName || '-'}</td>
            <td>${d.specialization || '-'}</td>
            <td>${d.contactNumber || '-'}</td>
            <td>${formatCurrency(d.consultationFee)}</td>
            <td><span class="${getStatusBadgeClass(d.availabilityStatus)}">${d.availabilityStatus || 'AVAILABLE'}</span></td>
            <td>
                <button class="btn-sm btn-primary" onclick="editDentist(${d.dentistId})">✏️</button>
                <button class="btn-sm btn-danger" onclick="deleteDentist(${d.dentistId})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// Search dentists
function searchDentists() {
    const searchTerm = document.getElementById('searchDentist').value.trim();
    if (!searchTerm) {
        renderDentists(currentDentists);
        return;
    }

    const filtered = currentDentists.filter(d =>
        d.fullName && d.fullName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    renderDentists(filtered);
}

// Setup dentist search
function setupDentistSearch() {
    const searchInput = document.getElementById('searchDentist');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') {
                searchDentists();
            }
        });
    }
}

// Open dentist modal
function openDentistModal(dentistId = null) {
    const modal = document.getElementById('dentistModal');
    const form = document.getElementById('dentistForm');
    const title = document.getElementById('dentistModalTitle');

    form.reset();
    document.getElementById('dentistId').value = '';
    clearError('dentistFormError');

    if (dentistId) {
        title.textContent = 'Edit Dentist';
        loadDentistData(dentistId);
    } else {
        title.textContent = 'Add Dentist';
    }

    modal.classList.add('show');
}

// Close dentist modal
function closeDentistModal() {
    document.getElementById('dentistModal').classList.remove('show');
}

// Load dentist data
async function loadDentistData(dentistId) {
    try {
        const response = await apiGet(`/dentists/${dentistId}`);
        if (response.status === 200) {
            const d = response.data;
            document.getElementById('dentistId').value = d.dentistId;
            document.getElementById('dentistFullName').value = d.fullName || '';
            document.getElementById('dentistSpecialization').value = d.specialization || '';
            document.getElementById('dentistContact').value = d.contactNumber || '';
            document.getElementById('dentistEmail').value = d.email || '';
            document.getElementById('dentistFee').value = d.consultationFee || 0;
            document.getElementById('dentistStatus').value = d.availabilityStatus || 'AVAILABLE';
            document.getElementById('dentistNotes').value = d.notes || '';
        }
    } catch (error) {
        console.error('Error loading dentist:', error);
        showError('dentistFormError', 'Failed to load dentist data');
    }
}

// Setup dentist form
function setupDentistForm() {
    const form = document.getElementById('dentistForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveDentist();
        });
    }
}

// Save dentist
async function saveDentist() {
    const dentistId = document.getElementById('dentistId').value;
    const data = {
        fullName: document.getElementById('dentistFullName').value.trim(),
        specialization: document.getElementById('dentistSpecialization').value.trim(),
        contactNumber: document.getElementById('dentistContact').value.trim(),
        email: document.getElementById('dentistEmail').value.trim(),
        consultationFee: parseFloat(document.getElementById('dentistFee').value) || 0,
        availabilityStatus: document.getElementById('dentistStatus').value,
        notes: document.getElementById('dentistNotes').value.trim()
    };

    clearError('dentistFormError');

    if (!data.fullName) {
        showError('dentistFormError', 'Full name is required.');
        return;
    }

    try {
        let response;
        if (dentistId) {
            response = await apiPut(`/dentists/${dentistId}`, data);
        } else {
            response = await apiPost('/dentists', data);
        }

        if (response.status === 200 || response.status === 201) {
            closeDentistModal();
            await loadDentists();
            showSuccess(dentistId ? 'Dentist updated successfully!' : 'Dentist added successfully!');
        } else {
            const errorMsg = response.data && response.data.error ? response.data.error : 'Failed to save dentist';
            showError('dentistFormError', errorMsg);
        }
    } catch (error) {
        console.error('Error saving dentist:', error);
        showError('dentistFormError', 'Failed to save dentist. Please try again.');
    }
}

// Edit dentist
function editDentist(dentistId) {
    if (!isAdmin()) {
        alert('Admin privileges required.');
        return;
    }
    openDentistModal(dentistId);
}

// Delete dentist
async function deleteDentist(dentistId) {
    if (!isAdmin()) {
        alert('Admin privileges required.');
        return;
    }

    if (!confirm('Are you sure you want to deactivate this dentist?')) {
        return;
    }

    try {
        const response = await apiDelete(`/dentists/${dentistId}`);
        if (response.status === 200) {
            await loadDentists();
            showSuccess('Dentist deactivated successfully!');
        } else {
            alert('Failed to deactivate dentist');
        }
    } catch (error) {
        console.error('Error deleting dentist:', error);
        alert('Failed to deactivate dentist');
    }
}