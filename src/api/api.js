import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080', // Порт вашего Spring Boot приложения
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response.status === 401) {
      localStorage.removeItem('accessToken');
      // Здесь можно перенаправить на страницу входа
    }
    return Promise.reject(error);
  }
);

export default api;
