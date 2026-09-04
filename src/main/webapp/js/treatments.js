/**
 * Treatment Management Logic
 * Sunrise Dental Clinic Management System
 */

let currentTreatments = [];

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('treatments.html')) {
        loadTreatments();
        setupTreatmentSearch();
        setupTreatmentForm();
        toggleAdminElements();
    }
});

// Load treatments
async function loadTreatments() {
    const tableBody = document.getElementById('treatmentsTableBody');
    if (!tableBody) return;

    try {
        const response = await apiGet('/treatments');
        if (response.status === 200) {
            currentTreatments = response.data || [];
            renderTreatments(currentTreatments);
            document.getElementById('treatmentCount').textContent = `${currentTreatments.length} treatments`;
        } else {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Failed to load treatments</td></tr>';
        }
    } catch (error) {
        console.error('Error loading treatments:', error);
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Error loading treatments</td></tr>';
    }
}

// Render treatments table
function renderTreatments(treatments) {
    const tableBody = document.getElementById('treatmentsTableBody');
    if (!tableBody) return;

    if (treatments.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No treatments found</td></tr>';
        return;
    }

    tableBody.innerHTML = treatments.map(t => `
        <tr>
            <td><strong>${t.treatmentCode || '-'}</strong></td>
            <td>${t.treatmentName || '-'}</td>
            <td>${t.description || '-'}</td>
            <td>${formatCurrency(t.treatmentCost)}</td>
            <td>${formatCurrency(t.consultationFee)}</td>
            <td><span class="status-badge ${t.active ? 'status-paid' : 'status-cancelled'}">${t.active ? 'Active' : 'Inactive'}</span></td>
            <td>
                <button class="btn-sm btn-primary" onclick="editTreatment(${t.treatmentId})">✏️</button>
                <button class="btn-sm btn-danger" onclick="deleteTreatment(${t.treatmentId})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// Search treatments
function searchTreatments() {
    const searchTerm = document.getElementById('searchTreatment').value.trim();
    if (!searchTerm) {
        renderTreatments(currentTreatments);
        return;
    }

    const filtered = currentTreatments.filter(t =>
        t.treatmentName && t.treatmentName.toLowerCase().includes(searchTerm.toLowerCase())
    );
    renderTreatments(filtered);
}

// Setup treatment search
function setupTreatmentSearch() {
    const searchInput = document.getElementById('searchTreatment');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') {
                searchTreatments();
            }
        });
    }
}

// Open treatment modal
function openTreatmentModal(treatmentId = null) {
    const modal = document.getElementById('treatmentModal');
    const form = document.getElementById('treatmentForm');
    const title = document.getElementById('treatmentModalTitle');

    form.reset();
    document.getElementById('treatmentId').value = '';
    clearError('treatmentFormError');

    if (treatmentId) {
        title.textContent = 'Edit Treatment';
        loadTreatmentData(treatmentId);
    } else {
        title.textContent = 'Add Treatment';
    }

    modal.classList.add('show');
}

// Close treatment modal
function closeTreatmentModal() {
    document.getElementById('treatmentModal').classList.remove('show');
}

// Load treatment data
async function loadTreatmentData(treatmentId) {
    try {
        const response = await apiGet(`/treatments/${treatmentId}`);
        if (response.status === 200) {
            const t = response.data;
            document.getElementById('treatmentId').value = t.treatmentId;
            document.getElementById('treatmentCode').value = t.treatmentCode || '';
            document.getElementById('treatmentName').value = t.treatmentName || '';
            document.getElementById('treatmentDescription').value = t.description || '';
            document.getElementById('treatmentCost').value = t.treatmentCost || 0;
            document.getElementById('treatmentConsultationFee').value = t.consultationFee || 0;
        }
    } catch (error) {
        console.error('Error loading treatment:', error);
        showError('treatmentFormError', 'Failed to load treatment data');
    }
}

// Setup treatment form
function setupTreatmentForm() {
    const form = document.getElementById('treatmentForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveTreatment();
        });
    }
}

// Save treatment
async function saveTreatment() {
    const treatmentId = document.getElementById('treatmentId').value;
    const data = {
        treatmentCode: document.getElementById('treatmentCode').value.trim().toUpperCase(),
        treatmentName: document.getElementById('treatmentName').value.trim(),
        description: document.getElementById('treatmentDescription').value.trim(),
        treatmentCost: parseFloat(document.getElementById('treatmentCost').value) || 0,
        consultationFee: parseFloat(document.getElementById('treatmentConsultationFee').value) || 0
    };

    clearError('treatmentFormError');

    if (!data.treatmentCode) {
        showError('treatmentFormError', 'Treatment code is required.');
        return;
    }
    if (!data.treatmentName) {
        showError('treatmentFormError', 'Treatment name is required.');
        return;
    }
    if (data.treatmentCost < 0) {
        showError('treatmentFormError', 'Treatment cost cannot be negative.');
        return;
    }

    try {
        let response;
        if (treatmentId) {
            response = await apiPut(`/treatments/${treatmentId}`, data);
        } else {
            response = await apiPost('/treatments', data);
        }

        if (response.status === 200 || response.status === 201) {
            closeTreatmentModal();
            await loadTreatments();
            showSuccess(treatmentId ? 'Treatment updated successfully!' : 'Treatment added successfully!');
        } else {
            const errorMsg = response.data && response.data.error ? response.data.error : 'Failed to save treatment';
            showError('treatmentFormError', errorMsg);
        }
    } catch (error) {
        console.error('Error saving treatment:', error);
        showError('treatmentFormError', 'Failed to save treatment. Please try again.');
    }
}

// Edit treatment
function editTreatment(treatmentId) {
    if (!isAdmin()) {
        alert('Admin privileges required.');
        return;
    }
    openTreatmentModal(treatmentId);
}

// Delete treatment
async function deleteTreatment(treatmentId) {
    if (!isAdmin()) {
        alert('Admin privileges required.');
        return;
    }

    if (!confirm('Are you sure you want to deactivate this treatment?')) {
        return;
    }

    try {
        const response = await apiDelete(`/treatments/${treatmentId}`);
        if (response.status === 200) {
            await loadTreatments();
            showSuccess('Treatment deactivated successfully!');
        } else {
            alert('Failed to deactivate treatment');
        }
    } catch (error) {
        console.error('Error deleting treatment:', error);
        alert('Failed to deactivate treatment');
    }
}