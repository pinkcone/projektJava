import React, { useState, useContext } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

function Register() {
  const { register, error } = useContext(AuthContext);
  const navigate = useNavigate();

  const [credentials, setCredentials] = useState({
    email: '',
    haslo: '',
    potwierdzHaslo: '',
  });

  const [localError, setLocalError] = useState(null);

  const handleChange = (e) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalError(null);

    if (credentials.haslo !== credentials.potwierdzHaslo) {
      setLocalError('Hasła nie są zgodne.');
      return;
    }

    await register(credentials.email, credentials.haslo);
    if (!error) {
      navigate('/login');
    }
  };

  return (
    <div className="register-container">
      <h2>Rejestracja</h2>
      {localError && <p className="error">{localError}</p>}
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Email:</label>
          <input type="email" name="email" value={credentials.email} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Hasło:</label>
          <input type="password" name="haslo" value={credentials.haslo} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Potwierdź Hasło:</label>
          <input type="password" name="potwierdzHaslo" value={credentials.potwierdzHaslo} onChange={handleChange} required />
        </div>
        <button type="submit">Zarejestruj</button>
      </form>
    </div>
  );
}

export default Register;
