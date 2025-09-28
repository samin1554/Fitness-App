import axios from "axios";

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL
});

api.interceptors.request.use((config) => {
    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    if (userId) {
        config.headers['X-User-ID'] = userId;
    }
    
    console.log('API Request:', config);
    return config;
});

api.interceptors.response.use(
    (response) => {
        console.log('API Response:', response);
        return response;
    },
    (error) => {
        console.error('API Error:', error);
        console.error('Error Response:', error.response);
        return Promise.reject(error);
    }
);


export const getActivities = () => api.get('/activities');
export const addActivity = (activity) => api.post('/activities', activity);
export const getActivityDetail = (id) => api.get(`/recommendations/activity/${id}`);