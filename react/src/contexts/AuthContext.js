import React, { createContext, useState } from 'react';
import { login as loginService, register as registerService } from '../services/authService';
import { jwtDecode } from 'jwt-decode';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const storedToken = localStorage.getItem('token');
  const [token, setToken] = useState(storedToken || null);
  const [user, setUser] = useState(() => {
    if (storedToken) {
      const decodedToken = jwtDecode(storedToken);
      console.log('Decoded Token:', decodedToken);

      const roles = decodedToken.roles
        ? decodedToken.roles.map((role) => role.authority.replace('ROLE_', ''))
        : [];

      return {
        id: decodedToken.id,
        email: decodedToken.sub,
        roles: roles,
      };
    }
    return null;
  });
  const [error, setError] = useState(null);

  const login = async (emailInput, haslo) => {
    try {
      const data = await loginService(emailInput, haslo);
      setToken(data.token);
      localStorage.setItem('token', data.token);

      const decodedToken = jwtDecode(data.token);
      console.log('Decoded Token:', decodedToken);

      const roles = decodedToken.roles
        ? decodedToken.roles.map((role) => role.authority.replace('ROLE_', ''))
        : [];

      setUser({
        id: decodedToken.id,
        email: decodedToken.sub,
        roles: roles,
      });
      setError(null);
    } catch (err) {
      setError('Nieprawidłowy email lub hasło.');
    }
  };

  const register = async (emailInput, haslo) => {
    try {
      await registerService(emailInput, haslo);
      setError(null);
    } catch (err) {
      if (err.response && err.response.data && err.response.data.message) {
        setError(err.response.data.message);
      } else {
        setError('Rejestracja nie powiodła się. Spróbuj ponownie.');
      }
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
  };

  const hasRole = (role) => {
    return user && user.roles && user.roles.includes(role);
  };

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, error, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};
