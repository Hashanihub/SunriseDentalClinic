/**
 * Application Initialization and Common Functions
 * Sunrise Dental Clinic Management System
 */

// ==================== INITIALIZATION ====================

document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    setupLogout();
    setupSidebar();
});

// ==================== AUTHENTICATION ====================

async function checkAuth() {
    try {
        const response = await apiGet('/auth/current-user');
        if (response.status === 200 && response.data && response.data.success) {
            // User is authenticated - update UI
            updateUserInfo(response.data);

            // ✅ Wait for role to be set in DOM, then update menu
            setTimeout(updateMenuBasedOnRole, 200);
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

// ==================== ROLE-BASED MENU ====================

/**
 * Update menu items based on user role
 *
 * ADMIN:        All items visible (including Users)
 * RECEPTIONIST: Hide Users, Dentists, Treatments
 * DENTIST:      Hide everything except Dashboard, Appointment Search
 */
function updateMenuBasedOnRole() {
    const roleEl = document.getElementById('userRole');
    if (!roleEl) return;

    const role = roleEl.textContent.trim();

    // ✅ Wait until role is loaded
    if (role === 'Loading...' || role === '' || role === 'User') {
        setTimeout(updateMenuBasedOnRole, 300);
        return;
    }

    console.log('🔐 Updating menu for role:', role);

    // ✅ STEP 1: Show/Hide ADMIN-ONLY items (Users link)
    const adminOnlyItems = document.querySelectorAll('.admin-only');
    adminOnlyItems.forEach(item => {
        if (role === 'ADMIN') {
            item.style.display = ''; // Show for Admin
        } else {
            item.style.display = 'none'; // Hide for Dentist & Receptionist
        }
    });

    // ✅ STEP 2: Reset all sidebar items (so we can re-apply restrictions)
    const sidebarItems = document.querySelectorAll('.sidebar-nav li');
    sidebarItems.forEach(li => {
        // Don't reset admin-only items (they were set in step 1)
        if (!li.classList.contains('admin-only')) {
            li.style.display = '';
        }
    });

    // ✅ STEP 3: Apply DENTIST restrictions
    if (role === 'DENTIST') {
        const dentistRestricted = [
            'patients.html',
            'dentists.html',
            'treatments.html',
            'billing.html',
            'reports.html',
            'appointments.html',
            'help.html',
            'users.html'
        ];

        sidebarItems.forEach(li => {
            const link = li.querySelector('a');
            if (link) {
                const href = link.getAttribute('href');
                if (href && dentistRestricted.some(r => href.includes(r))) {
                    li.style.display = 'none';
                }
            }
        });
    }

    // ✅ STEP 4: Apply RECEPTIONIST restrictions
    if (role === 'RECEPTIONIST') {
        const receptionistRestricted = [
            'users.html',
            'dentists.html',
            'treatments.html'
        ];

        sidebarItems.forEach(li => {
            const link = li.querySelector('a');
            if (link) {
                const href = link.getAttribute('href');
                if (href && receptionistRestricted.some(r => href.includes(r))) {
                    li.style.display = 'none';
                }
            }
        });
    }

    // ✅ ADMIN: No restrictions - all items visible
}

// ==================== LOGOUT ====================

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

async function logout() {
    try {
        await apiPost('/auth/logout');
    } catch (error) {
        console.error('Logout error:', error);
    }
    window.location.href = 'login.html';
}

// ==================== SIDEBAR ====================

function setupSidebar() {
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            sidebarLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });
}

// ==================== FORMATTING HELPERS ====================

function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return dateString;
        return date.toLocaleDateString('en-LK', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    } catch (e) {
        return dateString;
    }
}

function formatDateInput(dateString) {
    if (!dateString) return '';
    try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';
        return date.toISOString().split('T')[0];
    } catch (e) {
        return '';
    }
}

function formatTime(timeString) {
    if (!timeString) return '-';
    try {
        const str = String(timeString);
        if (str === 'null' || str === 'undefined' || str === '') return '-';
        const parts = str.split(':');
        if (parts.length >= 2) {
            const hour = parseInt(parts[0]);
            const minute = parts[1];
            if (isNaN(hour)) return str;
            const ampm = hour >= 12 ? 'PM' : 'AM';
            const hour12 = hour % 12 || 12;
            return `${hour12}:${minute} ${ampm}`;
        }
        return str;
    } catch (e) {
        return timeString;
    }
}

function formatCurrency(amount) {
    if (!amount) return 'LKR 0.00';
    return `LKR ${parseFloat(amount).toFixed(2)}`;
}

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

// ==================== UI HELPERS ====================

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

function clearError(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.style.display = 'none';
        element.textContent = '';
    }
}

function showSuccess(message) {
    alert(message);
}

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

// ==================== ROLE CHECKS ====================

function isAdmin() {
    const roleEl = document.getElementById('userRole');
    return roleEl && roleEl.textContent.trim() === 'ADMIN';
}

function isDentist() {
    const roleEl = document.getElementById('userRole');
    return roleEl && roleEl.textContent.trim() === 'DENTIST';
}

function isReceptionist() {
    const roleEl = document.getElementById('userRole');
    return roleEl && roleEl.textContent.trim() === 'RECEPTIONIST';
}

// ==================== LOADING ====================

function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<tr><td colspan="8" class="text-center">Loading...</td></tr>';
    }
}