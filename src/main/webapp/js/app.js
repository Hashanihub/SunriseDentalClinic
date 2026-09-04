/**
 * Application Initialization and Common Functions
 * Sunrise Dental Clinic Management System
 */

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    setupLogout();
    setupSidebar();
    updateMenuBasedOnRole();
});

// Check if user is authenticated
async function checkAuth() {
    try {
        const response = await apiGet('/auth/current-user');
        if (response.status === 200 && response.data && response.data.success) {
            // User is authenticated
            updateUserInfo(response.data);
            updateMenuBasedOnRole();
            return true;
        } else {
            if (!window.location.pathname.includes('login.html')) {
                window.location.href = 'login.html';
            }
            return false;
        }
    } catch (error) {
        console.error('Auth check failed:', error);
        if (!window.location.pathname.includes('login.html')) {
            window.location.href = 'login.html';
        }
        return false;
    }
}

// Update user info in header
function updateUserInfo(userData) {
    const userNameEl = document.getElementById('userName');
    const userRoleEl = document.getElementById('userRole');

    if (userNameEl) {
        userNameEl.textContent = userData.fullName || userData.username || 'User';
    }

    if (userRoleEl) {
        userRoleEl.textContent = userData.role || 'User';
    }
}

// Update menu based on user role
function updateMenuBasedOnRole() {
    const roleEl = document.getElementById('userRole');
    if (!roleEl) return;

    const role = roleEl.textContent;

    // Get all menu items that should be hidden for Dentist
    const dentistRestricted = [
        'patients.html',
        'dentists.html',
        'treatments.html',
        'billing.html'
    ];

    // Get all sidebar links
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');

    sidebarLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (role === 'DENTIST') {
            // Hide restricted links for Dentist
            if (dentistRestricted.some(restricted => href && href.includes(restricted))) {
                link.parentElement.style.display = 'none';
            }
        } else {
            // Show all links for ADMIN and RECEPTIONIST
            link.parentElement.style.display = '';
        }
    });
}

// Setup logout button
function setupLogout() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async function(e) {
            e.preventDefault();
            if (confirm('Are you sure you want to logout?')) {
                await logout();
            }
        });
    }
}

// Logout function
async function logout() {
    try {
        const response = await apiPost('/auth/logout');
        if (response.status === 200) {
            window.location.href = 'login.html';
        } else {
            alert('Logout failed. Please try again.');
        }
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = 'login.html';
    }
}

// Setup sidebar navigation
function setupSidebar() {
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            sidebarLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
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
    if (!timeString) return '-';
    const parts = timeString.split(':');
    if (parts.length >= 2) {
        const hour = parseInt(parts[0]);
        const minute = parts[1];
        const ampm = hour >= 12 ? 'PM' : 'AM';
        const hour12 = hour % 12 || 12;
        return `${hour12}:${minute} ${ampm}`;
    }
    return timeString;
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
    alert(message);
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

// Check if user is dentist
function isDentist() {
    const roleEl = document.getElementById('userRole');
    if (roleEl) {
        return roleEl.textContent === 'DENTIST';
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