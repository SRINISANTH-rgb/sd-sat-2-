const API_URL = '/users';
let allUsers = [];
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    setupTheme();
});
async function loadUsers() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`Server returned HTTP ${response.status}`);
        }
        allUsers = await response.json();
        renderTable(allUsers);
        updateStats(allUsers);
    } catch (error) {
        console.error('Error fetching users:', error);
        showToast('Failed to connect to Java Servlet API: ' + error.message, 'error');
    }
}
function renderTable(users) {
    const tbody = document.getElementById('userTableBody');
    const emptyState = document.getElementById('emptyState');
    tbody.innerHTML = '';
    if (!users || users.length === 0) {
        emptyState.style.display = 'block';
        return;
    }
    emptyState.style.display = 'none';
    users.forEach(user => {
        const tr = document.createElement('tr');
        const statusClass = user.status === 'Active' ? 'active-status' : 'inactive-status';
        tr.innerHTML = `
            <td><strong>#${user.id}</strong></td>
            <td>${escapeHtml(user.name)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="badge role-badge">${escapeHtml(user.role || 'Student')}</span></td>
            <td><span class="badge ${statusClass}">${escapeHtml(user.status || 'Active')}</span></td>
            <td>
                <button class="btn-action-edit" onclick="editUser(${user.id})" title="Edit User">
                    <i class="fa-solid fa-pen"></i> Edit
                </button>
                <button class="btn-action-delete" onclick="confirmDeleteUser(${user.id}, '${escapeHtml(user.name)}')" title="Delete User">
                    <i class="fa-solid fa-trash"></i> Delete
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}
function updateStats(users) {
    const total = users.length;
    const active = users.filter(u => u.status === 'Active').length;
    const inactive = users.filter(u => u.status === 'Inactive').length;
    document.getElementById('totalUsersCount').textContent = total;
    document.getElementById('activeUsersCount').textContent = active;
    document.getElementById('inactiveUsersCount').textContent = inactive;
}
async function handleFormSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('userId').value;
    const name = document.getElementById('userName').value;
    const email = document.getElementById('userEmail').value;
    const role = document.getElementById('userRole').value;
    const status = document.getElementById('userStatus').value;
    const payload = { name, email, role, status };
    try {
        let response;
        if (id) {
            payload.id = parseInt(id);
            response = await fetch(`${API_URL}?action=update`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(payload)
            });
        } else {
            response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams(payload)
            });
        }
        const data = await response.json();
        if (response.ok) {
            showToast(data.message || (id ? 'User updated successfully!' : 'User created successfully!'), 'success');
            resetForm();
            loadUsers();
            logConsoleOutput(data);
        } else {
            showToast(data.error || 'Operation failed', 'error');
            logConsoleOutput(data);
        }
    } catch (error) {
        console.error('Submission error:', error);
        showToast('Error communicating with servlet: ' + error.message, 'error');
    }
}
function editUser(id) {
    const user = allUsers.find(u => u.id === id);
    if (!user) return;
    document.getElementById('userId').value = user.id;
    document.getElementById('userName').value = user.name;
    document.getElementById('userEmail').value = user.email;
    document.getElementById('userRole').value = user.role || 'Student';
    document.getElementById('userStatus').value = user.status || 'Active';
    document.getElementById('formTitle').innerHTML = `<i class="fa-solid fa-user-pen"></i> Edit User #${user.id}`;
    document.getElementById('submitBtn').innerHTML = `<i class="fa-solid fa-floppy-disk"></i> Update User`;
    document.getElementById('resetFormBtn').style.display = 'inline-flex';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}
function resetForm() {
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('formTitle').innerHTML = `<i class="fa-solid fa-user-plus"></i> Create New User`;
    document.getElementById('submitBtn').innerHTML = `<i class="fa-solid fa-paper-plane"></i> Save User Details`;
    document.getElementById('resetFormBtn').style.display = 'none';
}
async function confirmDeleteUser(id, name) {
    if (confirm(`Are you sure you want to delete user "${name}" (ID: ${id})?`)) {
        try {
            const response = await fetch(`${API_URL}?action=delete&id=${id}`, {
                method: 'POST'
            });
            const data = await response.json();
            if (response.ok) {
                showToast(`User "${name}" deleted successfully`, 'success');
                loadUsers();
                logConsoleOutput(data);
            } else {
                showToast(data.error || 'Failed to delete user', 'error');
            }
        } catch (error) {
            showToast('Delete failed: ' + error.message, 'error');
        }
    }
}
function filterUsers() {
    const query = document.getElementById('searchInput').value.toLowerCase();
    const filtered = allUsers.filter(u => 
        u.name.toLowerCase().includes(query) || 
        u.email.toLowerCase().includes(query) ||
        (u.role && u.role.toLowerCase().includes(query))
    );
    renderTable(filtered);
}
async function testApiGet() {
    try {
        const res = await fetch(API_URL);
        const data = await res.json();
        logConsoleOutput({ method: 'GET /users', statusCode: res.status, response: data });
    } catch (err) {
        logConsoleOutput({ error: err.message });
    }
}
async function testApiPost() {
    try {
        const randomId = Math.floor(Math.random() * 900) + 100;
        const testPayload = {
            name: `Test User ${randomId}`,
            email: `test${randomId}@example.com`,
            role: 'Student',
            status: 'Active'
        };
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(testPayload)
        });
        const data = await res.json();
        logConsoleOutput({ method: 'POST /users', statusCode: res.status, response: data });
        loadUsers();
    } catch (err) {
        logConsoleOutput({ error: err.message });
    }
}
function logConsoleOutput(obj) {
    const outputElem = document.getElementById('apiConsoleOutput');
    outputElem.textContent = JSON.stringify(obj, null, 2);
}
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
        <span>${escapeHtml(message)}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
function setupTheme() {
    const btn = document.getElementById('themeToggle');
    btn.addEventListener('click', () => {
        const isDark = document.body.getAttribute('data-theme') === 'dark';
        if (isDark) {
            document.body.removeAttribute('data-theme');
            btn.innerHTML = `<i class="fa-solid fa-moon"></i>`;
        } else {
            document.body.setAttribute('data-theme', 'dark');
            btn.innerHTML = `<i class="fa-solid fa-sun"></i>`;
        }
    });
}
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
