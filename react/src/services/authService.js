import api from './api';


export const login = async (email, haslo) => {
  try {
    console.log('Wysyłam żądanie logowania z danymi:', { email, haslo });
    const response = await api.post('/api/auth/login', { email, haslo });
    console.log('Otrzymano odpowiedź z logowania:', response.data);
    return response.data;
  } catch (error) {
    console.error('Błąd w usłudze login:', error);
    throw error;
  }
};

export const register = async (email, haslo) => {
  try {
    console.log('Wysyłam żądanie rejestracji z danymi:', { email, haslo });
    const response = await api.post('/api/auth/register', { email, haslo });
    console.log('Otrzymano odpowiedź z rejestracji:', response.data);
    return response.data;
  } catch (error) {
    console.error('Błąd w usłudze register:', error);
    throw error;
  }
};
