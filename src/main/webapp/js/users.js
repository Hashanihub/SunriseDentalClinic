/**
 * User Management Logic - Admin Only
 */

let allUsers = [];

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('users.html')) {
        loadUsers();
        setupUserForm();
    }
});

async function loadUsers() {
    const tableBody = document.getElementById('usersTableBody');
    if (!tableBody) return;

    try {
        const response = await apiGet('/users');
        if (response.status === 200) {
            allUsers = response.data || [];
            renderUsers(allUsers);
            document.getElementById('userCount').textContent = `${allUsers.length} users`;
        } else if (response.status === 403) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center" style="color:red;">Admin access required</td></tr>';
        } else {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Failed to load users</td></tr>';
        }
    } catch (error) {
        console.error('Error loading users:', error);
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">Error loading users</td></tr>';
    }
}

function renderUsers(users) {
    const tableBody = document.getElementById('usersTableBody');
    if (!tableBody) return;

    if (users.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No users found</td></tr>';
        return;
    }

    tableBody.innerHTML = users.map(user => `
        <tr>
            <td>${user.userId || '-'}</td>
            <td><strong>${user.username || '-'}</strong></td>
            <td>${user.fullName || '-'}</td>
            <td>${user.email || '-'}</td>
            <td><span class="role-badge">${user.role || '-'}</span></td>
            <td>
                <span class="status-badge ${user.active ? 'status-paid' : 'status-cancelled'}">
                    ${user.active ? 'Active' : 'Inactive'}
                </span>
            </td>
            <td>
                <button class="btn-sm btn-primary" onclick="editUser(${user.userId})">&#9999;</button>
                <button class="btn-sm btn-danger" onclick="deleteUser(${user.userId})">&#128465;</button>
            </td>
        </tr>
    `).join('');
}

function searchUsers() {
    const term = document.getElementById('searchUser').value.trim().toLowerCase();
    if (!term) {
        renderUsers(allUsers);
        return;
    }
    const filtered = allUsers.filter(u =>
        (u.username && u.username.toLowerCase().includes(term)) ||
        (u.fullName && u.fullName.toLowerCase().includes(term))
    );
    renderUsers(filtered);
}

function openUserModal(userId = null) {
    const modal = document.getElementById('userModal');
    const form = document.getElementById('userForm');
    const title = document.getElementById('userModalTitle');

    form.reset();
    document.getElementById('userId').value = '';
    document.getElementById('password').value = 'password123';
    clearError('userFormError');

    if (userId) {
        title.textContent = 'Edit User';
        loadUserData(userId);
    } else {
        title.textContent = 'Add User';
    }

    modal.classList.add('show');
}

function closeUserModal() {
    document.getElementById('userModal').classList.remove('show');
}

async function loadUserData(userId) {
    try {
        const response = await apiGet(`/users/${userId}`);
        if (response.status === 200) {
            const user = response.data;
            document.getElementById('userId').value = user.userId;
            document.getElementById('username').value = user.username || '';
            document.getElementById('username').readOnly = true;
            document.getElementById('fullName').value = user.fullName || '';
            document.getElementById('email').value = user.email || '';
            document.getElementById('role').value = user.role || '';
        }
    } catch (error) {
        console.error('Error loading user:', error);
    }
}

function setupUserForm() {
    const form = document.getElementById('userForm');
    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await saveUser();
        });
    }
}

async function saveUser() {
    const userId = document.getElementById('userId').value;
    const data = {
        username: document.getElementById('username').value.trim(),
        fullName: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        role: document.getElementById('role').value
    };

    clearError('userFormError');

    if (!data.username) {
        showError('userFormError', 'Username is required');
        return;
    }
    if (!data.fullName) {
        showError('userFormError', 'Full Name is required');
        return;
    }
    if (!data.role) {
        showError('userFormError', 'Role is required');
        return;
    }

    try {
        let response;
        if (userId) {
            response = await apiPut(`/users/${userId}`, data);
        } else {
            response = await apiPost('/users', data);
        }

        if (response.status === 200 || response.status === 201) {
            closeUserModal();
            showSuccess(userId ? 'User updated successfully!' : 'User created successfully!');
            await loadUsers();
        } else {
            const err = response.data && response.data.error ? response.data.error : 'Failed to save user';
            showError('userFormError', err);
        }
    } catch (error) {
        console.error('Error saving user:', error);
        showError('userFormError', 'Failed to save user');
    }
}

function editUser(userId) {
    document.getElementById('username').readOnly = false;
    openUserModal(userId);
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to deactivate this user?')) return;

    try {
        const response = await apiDelete(`/users/${userId}`);
        if (response.status === 200) {
            await loadUsers();
            showSuccess('User deactivated successfully!');
        } else {
            alert('Failed to deactivate user');
        }
    } catch (error) {
        console.error('Error deactivating user:', error);
        alert('Failed to deactivate user');
    }
}