// API сервис для взаимодействия с backend
const API = {
    token: localStorage.getItem('token') || null,
    baseUrl: '/api',

    // === AUTHENTICATION ===
    login: async (username, password) => {
        try {
            const response = await fetch(`${API.baseUrl}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const data = await response.json();
            if (response.ok) {
                API.token = data.token;
                localStorage.setItem('token', data.token);
                return { success: true, data };
            }
            return { success: false, message: data.message || 'Ошибка входа' };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    logout: () => {
        API.token = null;
        localStorage.removeItem('token');
    },

    changePassword: async (oldPassword, newPassword) => {
        try {
            const response = await fetch(`${API.baseUrl}/auth/change-password`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${API.token}`
                },
                body: JSON.stringify({ oldPassword, newPassword })
            });
            const data = await response.json();
            return { success: response.ok, message: data.message || (response.ok ? 'Пароль изменён' : 'Ошибка') };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    getCurrentUser: async () => {
    try {
        const response = await fetch(`${API.baseUrl}/auth/info`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${API.token}` }
        });
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error('Ошибка получения пользователя:', error);
        return null;
    }
},


    // === BUSES ===
    getBuses: async () => {
        try {
            const response = await fetch(`${API.baseUrl}/buses`, {
                headers: { 'Authorization': `Bearer ${API.token}` }
            });
            return response.ok ? await response.json() : [];
        } catch (error) {
            console.error('Ошибка получения автобусов:', error);
            return [];
        }
    },

    createBus: async (model) => {
        try {
            const response = await fetch(`${API.baseUrl}/buses`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${API.token}`
                },
                body: JSON.stringify({ model })
            });
            return { success: response.ok, data: response.ok ? await response.json() : null };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    updateBus: async (id, model) => {
        try {
            const response = await fetch(`${API.baseUrl}/buses/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${API.token}`
                },
                body: JSON.stringify({ model })
            });
            return { success: response.ok, data: response.ok ? await response.json() : null };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    deleteBus: async (id) => {
        try {
            const response = await fetch(`${API.baseUrl}/buses/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${API.token}` }
            });
            return { success: response.ok };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    // === SENSORS ===
    getSensors: async () => {
        try {
            const response = await fetch(`${API.baseUrl}/sensors`, {
                headers: { 'Authorization': `Bearer ${API.token}` }
            });
            return response.ok ? await response.json() : [];
        } catch (error) {
            console.error('Ошибка получения датчиков:', error);
            return [];
        }
    },

    createSensor: async (busId, sensorType, value, anomaly) => {
        try {
            const response = await fetch(`${API.baseUrl}/sensors`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${API.token}`
                },
                body: JSON.stringify({ busId, sensorType, value, anomaly })
            });
            return { success: response.ok, data: response.ok ? await response.json() : null };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    updateSensor: async (id, value, anomaly) => {
        try {
            const response = await fetch(`${API.baseUrl}/sensors/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${API.token}`
                },
                body: JSON.stringify({ value, anomaly })
            });
            return { success: response.ok, data: response.ok ? await response.json() : null };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    deleteSensor: async (id) => {
        try {
            const response = await fetch(`${API.baseUrl}/sensors/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${API.token}` }
            });
            return { success: response.ok };
        } catch (error) {
            return { success: false, message: error.message };
        }
    },

    // === REPORTS ===
    getAnomalyReport: async (from, to) => {
        try {
            const response = await fetch(
                `${API.baseUrl}/reports/anomalies?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
                { headers: { 'Authorization': `Bearer ${API.token}` } }
            );
            return response.ok ? await response.json() : [];
        } catch (error) {
            console.error('Ошибка получения отчёта:', error);
            return [];
        }
    },

    downloadReportAsCSV: (data) => {
        let csv = 'ID Автобуса,Количество аномалий\n';
        data.forEach(row => {
            csv += `${row.busId},${row.anomaliesCount}\n`;
        });

        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `report_${new Date().toISOString().split('T')[0]}.csv`);
        link.click();
    }
};
