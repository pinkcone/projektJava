import axios from 'axios';

const api = axios.create({
  baseURL: '/',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    console.log('Wysyłam żądanie:', config);
    return config;
  },
  (error) => {
    console.error('Błąd żądania:', error);
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    console.log('Otrzymano odpowiedź:', response);
    return response;
  },
  (error) => {
    console.error('Błąd odpowiedzi:', error);
    return Promise.reject(error);
  }
);

export default api;
