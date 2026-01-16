// Главное приложение
const App = {
    currentUser: null,
    currentReport: [],

    // === INIT ===
    init: async () => {
        App.attachEventListeners();
        if (API.token) {
            await App.loadCurrentUser();
            if (App.currentUser) {
                await App.showDashboard();
            } else {
                App.showLoginForm();
            }
        } else {
            App.showLoginForm();
        }
    },

    // === EVENT LISTENERS ===
    attachEventListeners: () => {
        // LOGIN
        document.getElementById('loginBtn').addEventListener('click', App.handleLogin);
        document.getElementById('loginInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') App.handleLogin();
        });
        document.getElementById('passwordInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') App.handleLogin();
        });

        // LOGOUT
        document.getElementById('logoutBtn').addEventListener('click', App.handleLogout);

        // CHANGE PASSWORD
        document.getElementById('changePasswordBtn').addEventListener('click', App.showChangePasswordModal);
        document.getElementById('submitChangePasswordBtn').addEventListener('click', App.handleChangePassword);
        document.querySelector('.close').addEventListener('click', App.hideChangePasswordModal);
        document.getElementById('changePasswordModal').addEventListener('click', (e) => {
            if (e.target.id === 'changePasswordModal') App.hideChangePasswordModal();
        });

        // TABS
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', App.handleTabSwitch);
        });

        // BUSES
        document.getElementById('createBusBtn').addEventListener('click', App.handleCreateBus);
        document.getElementById('busModelInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') App.handleCreateBus();
        });

        // SENSORS
        document.getElementById('createSensorBtn').addEventListener('click', App.handleCreateSensor);

        // REPORTS
        document.getElementById('generateReportBtn').addEventListener('click', App.handleGenerateReport);
        document.getElementById('downloadReportBtn').addEventListener('click', App.handleDownloadReport);
    },

    // === AUTH ===
    handleLogin: async () => {
        const username = document.getElementById('loginInput').value.trim();
        const password = document.getElementById('passwordInput').value.trim();

        if (!username || !password) {
            App.showAlert('Заполните логин и пароль', 'error');
            return;
        }

        const result = await API.login(username, password);
        if (result.success) {
            document.getElementById('loginInput').value = '';
            document.getElementById('passwordInput').value = '';
            await App.loadCurrentUser();
            if (!App.currentUser) {
                App.showAlert('Не удалось получить профиль пользователя', 'error');
                return;
            }
            await App.showDashboard();
        } else {
            App.showAlert(result.message, 'error');
        }
    },

    handleLogout: () => {
        API.logout();
        App.currentUser = null;
        App.showLoginForm();
        App.showAlert('Вы вышли из системы', 'success');
    },

    loadCurrentUser: async () => {
        App.currentUser = await API.getCurrentUser(); // /api/auth/info
        if (App.currentUser) {
            // backend возвращает { username, role, ... }
            document.getElementById('userName').textContent =
                `${App.currentUser.username} (${App.currentUser.role})`;
        }
    },

    showChangePasswordModal: () => {
        document.getElementById('changePasswordModal').classList.remove('hidden');
    },

    hideChangePasswordModal: () => {
        document.getElementById('changePasswordModal').classList.add('hidden');
        document.getElementById('oldPasswordInput').value = '';
        document.getElementById('newPasswordInput').value = '';
        document.getElementById('confirmPasswordInput').value = '';
    },

    handleChangePassword: async () => {
        const oldPassword = document.getElementById('oldPasswordInput').value;
        const newPassword = document.getElementById('newPasswordInput').value;
        const confirmPassword = document.getElementById('confirmPasswordInput').value;

        if (!oldPassword || !newPassword || !confirmPassword) {
            App.showAlert('Заполните все поля', 'error');
            return;
        }

        if (newPassword !== confirmPassword) {
            App.showAlert('Новые пароли не совпадают', 'error');
            return;
        }

        const result = await API.changePassword(oldPassword, newPassword);
        if (result.success) {
            App.showAlert(result.message, 'success');
            App.hideChangePasswordModal();
        } else {
            App.showAlert(result.message, 'error');
        }
    },

    // === UI NAVIGATION ===
    showLoginForm: () => {
        document.getElementById('loginForm').classList.remove('hidden');
        document.getElementById('userInfo').classList.add('hidden');
        document.getElementById('dashboardContent').classList.add('hidden');
        document.getElementById('notLoggedInMessage').classList.remove('hidden');
    },

    showDashboard: async () => {
        if (!App.currentUser) {
            App.showAlert('Не удалось получить данные пользователя', 'error');
            App.showLoginForm();
            return;
        }

        document.getElementById('loginForm').classList.add('hidden');
        document.getElementById('userInfo').classList.remove('hidden');
        document.getElementById('dashboardContent').classList.remove('hidden');
        document.getElementById('notLoggedInMessage').classList.add('hidden');

        const isAdmin = App.currentUser.role === 'ADMIN';
        document.getElementById('createBusSection').classList.toggle('hidden', !isAdmin);
        document.getElementById('createSensorSection').classList.toggle('hidden', !isAdmin);

        await App.loadDashboard();
        await App.loadBuses();
        await App.loadSensors();
    },

    handleTabSwitch: (e) => {
        const tabName = e.target.dataset.tab;

        document.querySelectorAll('.tab-content').forEach(tab => {
            tab.classList.remove('active');
        });

        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });

        document.getElementById(tabName).classList.add('active');
        e.target.classList.add('active');
    },

    // === DASHBOARD ===
    loadDashboard: async () => {
        const buses = await API.getBuses();
        const sensors = await API.getSensors();
        const anomalies = sensors.filter(s => s.anomaly).length;

        document.getElementById('totalBuses').textContent = buses.length;
        document.getElementById('totalAnomalies').textContent = anomalies;
        document.getElementById('totalSensors').textContent = sensors.length;
    },

    // === BUSES ===
    loadBuses: async () => {
        const buses = await API.getBuses();
        const tbody = document.getElementById('busesTable');

        if (buses.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">Автобусы не найдены</td></tr>';
            return;
        }

        const isAdmin = App.currentUser && App.currentUser.role === 'ADMIN';

        tbody.innerHTML = buses.map(bus => `
            <tr>
                <td>${bus.id}</td>
                <td>${bus.model}</td>
                <td>${new Date(bus.createdAt).toLocaleDateString('ru-RU')}</td>
                <td>
                    <div class="table-actions">
                        ${isAdmin ? `
                            <button class="btn-edit" onclick="App.handleEditBus(${bus.id}, '${bus.model}')">Редактировать</button>
                            <button class="btn-delete" onclick="App.handleDeleteBus(${bus.id})">Удалить</button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    handleCreateBus: async () => {
        const model = document.getElementById('busModelInput').value.trim();
        if (!model) {
            App.showAlert('Введите модель автобуса', 'error');
            return;
        }

        const result = await API.createBus(model);
        if (result.success) {
            document.getElementById('busModelInput').value = '';
            App.showAlert('Автобус добавлен', 'success');
            await App.loadBuses();
            await App.loadDashboard();
        } else {
            App.showAlert('Ошибка добавления автобуса', 'error');
        }
    },

    handleEditBus: async (id, currentModel) => {
        const newModel = prompt('Новая модель:', currentModel);
        if (newModel !== null && newModel.trim()) {
            const result = await API.updateBus(id, newModel);
            if (result.success) {
                App.showAlert('Автобус обновлён', 'success');
                await App.loadBuses();
            } else {
                App.showAlert('Ошибка обновления', 'error');
            }
        }
    },

    handleDeleteBus: async (id) => {
        if (confirm('Вы уверены?')) {
            const result = await API.deleteBus(id);
            if (result.success) {
                App.showAlert('Автобус удалён', 'success');
                await App.loadBuses();
                await App.loadDashboard();
            } else {
                App.showAlert('Ошибка удаления', 'error');
            }
        }
    },

    // === SENSORS ===
    loadSensors: async () => {
        const sensors = await API.getSensors();
        const tbody = document.getElementById('sensorsTable');

        if (sensors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7">Датчики не найдены</td></tr>';
            return;
        }

        const isAdmin = App.currentUser && App.currentUser.role === 'ADMIN';

        tbody.innerHTML = sensors.map(sensor => `
            <tr style="${sensor.anomaly ? 'background-color: #fee2e2;' : ''}">
                <td>${sensor.id}</td>
                <td>${sensor.busId}</td>
                <td>${sensor.sensorType}</td>
                <td>${sensor.value}</td>
                <td>${sensor.anomaly ? 'ДА' : 'НЕТ'}</td>
                <td>${new Date(sensor.timestamp).toLocaleString('ru-RU')}</td>
                <td>
                    <div class="table-actions">
                        ${isAdmin ? `
                            <button class="btn-delete" onclick="App.handleDeleteSensor(${sensor.id})">Удалить</button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    },

    handleCreateSensor: async () => {
        const busId = document.getElementById('sensorBusIdInput').value.trim();
        const sensorType = document.getElementById('sensorTypeInput').value.trim();
        const value = parseFloat(document.getElementById('sensorValueInput').value);
        const anomaly = document.getElementById('sensorAnomalyInput').checked;

        if (!busId || !sensorType || isNaN(value)) {
            App.showAlert('Заполните все поля датчика', 'error');
            return;
        }

        const result = await API.createSensor(parseInt(busId), sensorType, value, anomaly);
        if (result.success) {
            document.getElementById('sensorBusIdInput').value = '';
            document.getElementById('sensorTypeInput').value = '';
            document.getElementById('sensorValueInput').value = '';
            document.getElementById('sensorAnomalyInput').checked = false;
            App.showAlert('Датчик добавлен', 'success');
            await App.loadSensors();
            await App.loadDashboard();
        } else {
            App.showAlert('Ошибка добавления датчика', 'error');
        }
    },

    handleDeleteSensor: async (id) => {
        if (confirm('Удалить датчик?')) {
            const result = await API.deleteSensor(id);
            if (result.success) {
                App.showAlert('Датчик удалён', 'success');
                await App.loadSensors();
                await App.loadDashboard();
            } else {
                App.showAlert('Ошибка удаления', 'error');
            }
        }
    },

    // === REPORTS ===
    handleGenerateReport: async () => {
        const from = document.getElementById('reportFromInput').value;
        const to = document.getElementById('reportToInput').value;

        if (!from || !to) {
            App.showAlert('Укажите дату начала и окончания', 'error');
            return;
        }

        const fromISO = new Date(from).toISOString();
        const toISO = new Date(to).toISOString();

        App.currentReport = await API.getAnomalyReport(fromISO, toISO);

        const tbody = document.getElementById('reportsTable');
        if (App.currentReport.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2">Аномалий не найдено</td></tr>';
            document.getElementById('downloadReportBtn').style.display = 'none';
            return;
        }

        tbody.innerHTML = App.currentReport.map(row => `
            <tr>
                <td>${row.busId}</td>
                <td>${row.anomaliesCount}</td>
            </tr>
        `).join('');

        document.getElementById('downloadReportBtn').style.display = 'inline-block';
        App.showAlert('Отчёт сгенерирован', 'success');
    },

    handleDownloadReport: () => {
        if (App.currentReport.length > 0) {
            API.downloadReportAsCSV(App.currentReport);
            App.showAlert('Отчёт скачан', 'success');
        }
    },

    // === UTILITIES ===
    showAlert: (message, type = 'success') => {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type}`;
        alertDiv.textContent = message;

        const mainContent = document.querySelector('.main-content');
        mainContent.insertBefore(alertDiv, mainContent.firstChild);

        setTimeout(() => alertDiv.remove(), 4000);
    }
};

// Запуск приложения
document.addEventListener('DOMContentLoaded', App.init);
