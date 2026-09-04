/**
 * API Configuration and Helper Functions
 * Sunrise Dental Clinic Management System
 */

// API Base URL - Update this based on your deployment
const API_BASE_URL = window.location.origin + '/dental-clinic/api';

// Helper function to get the current session token
function getSessionToken() {
    // For session-based authentication, we rely on cookies
    // The session is maintained by the server
    return null;
}

// Generic API request function
async function apiRequest(endpoint, method = 'GET', data = null) {
    const url = API_BASE_URL + endpoint;

    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include' // Include cookies for session
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);

        // Handle non-JSON responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const result = await response.json();
            return { status: response.status, data: result };
        }

        return { status: response.status, data: null };

    } catch (error) {
        console.error('API Request Error:', error);
        throw error;
    }
}

// GET request helper
async function apiGet(endpoint) {
    return apiRequest(endpoint, 'GET');
}

// POST request helper
async function apiPost(endpoint, data) {
    return apiRequest(endpoint, 'POST', data);
}

// PUT request helper
async function apiPut(endpoint, data) {
    return apiRequest(endpoint, 'PUT', data);
}

// DELETE request helper
async function apiDelete(endpoint) {
    return apiRequest(endpoint, 'DELETE');
}

// Error handling helper
function handleApiError(error, defaultMessage = 'An error occurred. Please try again.') {
    console.error('API Error:', error);

    if (error.status === 401) {
        window.location.href = 'login.html';
        return 'Session expired. Please login again.';
    }

    if (error.data && error.data.error) {
        return error.data.error;
    }

    if (error.data && error.data.message) {
        return error.data.message;
    }

    return defaultMessage;
}

// Show loading indicator
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="loading-spinner">Loading...</div>';
    }
}

// Hide loading indicator
function hideLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        // Don't clear if we're showing data
    }
}

// Show error message
function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = message;
        element.style.display = 'block';
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000);
    }
}

// Clear error message
function clearError(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
        element.textContent = '';
    }
}

// Show success message
function showSuccess(message) {
    // You can implement a toast notification system here
    alert(message);
}

// Format date for display
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-LK', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Format date for input
function formatDateInput(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().split('T')[0];
}

// Format time for display
function formatTime(timeString) {
    // If null or undefined
    if (!timeString) return '-';

    // If it's already a string
    if (typeof timeString === 'string') {
        const str = timeString.trim();
        if (str === '' || str === 'null' || str === 'undefined') return '-';

        // Check if it's a time string (HH:MM:SS or HH:MM)
        const parts = str.split(':');
        if (parts.length >= 2) {
            const hour = parseInt(parts[0]);
            const minute = parts[1];
            if (!isNaN(hour) && !isNaN(minute)) {
                const ampm = hour >= 12 ? 'PM' : 'AM';
                const hour12 = hour % 12 || 12;
                return `${hour12}:${minute.padStart(2, '0')} ${ampm}`;
            }
        }
        return str;
    }

    // If it's a Date object or something else
    try {
        const date = new Date(timeString);
        if (!isNaN(date.getTime())) {
            const hours = date.getHours();
            const minutes = date.getMinutes();
            const ampm = hours >= 12 ? 'PM' : 'AM';
            const hour12 = hours % 12 || 12;
            return `${hour12}:${String(minutes).padStart(2, '0')} ${ampm}`;
        }
    } catch (e) {
        // ignore
    }

    // If all else fails, convert to string
    return String(timeString);
}



// Format currency
function formatCurrency(amount) {
    if (!amount) return 'LKR 0.00';
    return `LKR ${parseFloat(amount).toFixed(2)}`;
}

// Get status badge class
function getStatusBadgeClass(status) {
    if (!status) return 'status-badge';
    const statusMap = {
        'SCHEDULED': 'status-scheduled',
        'CONFIRMED': 'status-confirmed',
        'COMPLETED': 'status-completed',
        'CANCELLED': 'status-cancelled',
        'NO_SHOW': 'status-no_show',
        'PAID': 'status-paid',
        'UNPAID': 'status-unpaid',
        'PARTIAL': 'status-partial',
        'AVAILABLE': 'status-paid',
        'BUSY': 'status-warning',
        'ON_LEAVE': 'status-cancelled',
        'INACTIVE': 'status-cancelled'
    };
    return `status-badge ${statusMap[status] || ''}`;
}

// Populate select dropdown
function populateSelect(selectId, data, valueKey, textKey, defaultOption = 'Select...') {
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML = `<option value="">${defaultOption}</option>`;
    data.forEach(item => {
        const option = document.createElement('option');
        option.value = item[valueKey];
        option.textContent = item[textKey];
        select.appendChild(option);
    });
}

// Check if user has admin role
function isAdmin() {
    const roleEl = document.getElementById('userRole');
    if (roleEl) {
        return roleEl.textContent === 'ADMIN';
    }
    return false;
}

// Toggle visibility of admin-only elements
function toggleAdminElements() {
    const adminOnly = document.querySelectorAll('.admin-only');
    const isAdminUser = isAdmin();
    adminOnly.forEach(el => {
        el.style.display = isAdminUser ? '' : 'none';
    });
}