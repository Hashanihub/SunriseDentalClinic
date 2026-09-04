/**
 * Authentication Logic
 * Sunrise Dental Clinic Management System
 */

document.addEventListener('DOMContentLoaded', function() {
    // Setup login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Check if already logged in
    checkAuthStatus();
});

// Handle login form submission
async function handleLogin(e) {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorElement = document.getElementById('loginError');

    clearError('loginError');

    if (!username || !password) {
        showError('loginError', 'Please enter both username and password.');
        return;
    }

    // Show loading state
    const submitBtn = document.querySelector('.btn-login');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Logging in...';
    submitBtn.disabled = true;

    try {
        const response = await apiPost('/auth/login', {
            username: username,
            password: password
        });

        console.log('Login response:', response);

        if (response.status === 200 && response.data && response.data.success) {
            // Login successful
            window.location.href = 'dashboard.html';
        } else {
            // Login failed
            const message = response.data && response.data.message ? response.data.message : 'Invalid username or password.';
            showError('loginError', message);
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('loginError', 'Connection error. Please try again.');
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
    }
}

// Check if user is already authenticated
async function checkAuthStatus() {
    // Only check on login page
    if (!window.location.pathname.includes('login.html')) {
        return;
    }

    try {
        const response = await apiGet('/auth/current-user');
        if (response.status === 200 && response.data && response.data.success) {
            // Already logged in - redirect to dashboard
            window.location.href = 'dashboard.html';
        }
    } catch (error) {
        // Not logged in - stay on login page
        console.log('Not authenticated');
    }
}

// Enter key support for login
document.addEventListener('keydown', function(e) {
    if (e.key === 'Enter' && window.location.pathname.includes('login.html')) {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.dispatchEvent(new Event('submit'));
        }
    }
});