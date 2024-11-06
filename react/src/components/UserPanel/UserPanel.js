import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate, Link} from 'react-router-dom';
import './UserPanel.css';

function UserPanel() {
    const { token, user } = useContext(AuthContext);
    const navigate = useNavigate();

    const [userData, setUserData] = useState({
        imie: '',
        nazwisko: '',
        email: '',
        adres: '',
        numerTelefonu: '',
        haslo: '',
    });

    const [message, setMessage] = useState('');
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (!token || !user) {
            navigate('/login');
            return;
        }

        axios
            .get(`/api/users/${user.id}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((response) => {
                setUserData({
                    imie: response.data.imie || '',
                    nazwisko: response.data.nazwisko || '',
                    email: response.data.email || '',
                    adres: response.data.adres || '',
                    numerTelefonu: response.data.numerTelefonu || '',
                });
            })
            .catch((error) => {
                console.error('Błąd podczas pobierania danych użytkownika:', error);
            });
    }, [token, user, navigate]);

    const handleChange = (e) => {
        setUserData({
            ...userData,
            [e.target.name]: e.target.value,
        });
    };

    const validatePhoneNumber = (number) => {
        const phoneRegex = /^\d{9}$/;
        return phoneRegex.test(number);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setErrors({});
        setMessage('');

        let validationErrors = {};

        if (!userData.imie.trim()) {
            validationErrors.imie = 'Imię jest wymagane.';
        }

        if (!userData.nazwisko.trim()) {
            validationErrors.nazwisko = 'Nazwisko jest wymagane.';
        }

        if (!userData.email.trim()) {
            validationErrors.email = 'Email jest wymagany.';
        } else {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(userData.email)) {
                validationErrors.email = 'Nieprawidłowy format email.';
            }
        }

        if (userData.numerTelefonu && !validatePhoneNumber(userData.numerTelefonu)) {
            validationErrors.numerTelefonu = 'Numer telefonu musi składać się z 9 cyfr.';
        }
        if (userData.haslo && userData.haslo.length < 6) {
            validationErrors.haslo = 'Hasło musi mieć co najmniej 6 znaków.';
        }

        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        axios
            .put(`/api/users/${user.id}`, userData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((response) => {
                setMessage('Dane zostały zaktualizowane pomyślnie.');
                setUserData({ ...userData, haslo: '' });
            })
            .catch((error) => {
                if (error.response && error.response.data) {
                    setErrors({ serverError: error.response.data.message });
                    setMessage('');
                } else {
                    console.error('Błąd:', error);
                    setErrors({ serverError: 'Wystąpił błąd podczas aktualizacji danych.' });
                }
            });
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        axios
            .put(`/api/users/${user.id}`, userData, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            })
            .then((response) => {
                setMessage('Dane zostały zaktualizowane pomyślnie.');
            })
            .catch((error) => {
                if (error.response && error.response.data) {
                    setErrors({ serverError: error.response.data.message });
                    setMessage('');
                } else {
                    console.error('Błąd:', error);
                    setErrors({ serverError: 'Wystąpił błąd podczas aktualizacji danych.' });
                }
            });
    };

    return (
        <div className="user-panel-container">
            <h2>Moje dane</h2>
            {message && <p className="message">{message}</p>}
            {errors.serverError && <p className="error">{errors.serverError}</p>}
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label>Imię:</label>
                    <input
                        type="text"
                        name="imie"
                        value={userData.imie}
                        onChange={handleChange}
                        required
                    />
                    {errors.imie && <p className="error">{errors.imie}</p>}
                </div>

                <div className="form-group">
                    <label>Nazwisko:</label>
                    <input
                        type="text"
                        name="nazwisko"
                        value={userData.nazwisko}
                        onChange={handleChange}
                        required
                    />
                    {errors.nazwisko && <p className="error">{errors.nazwisko}</p>}
                </div>

                <div className="form-group">
                    <label>Email:</label>
                    <input
                        type="email"
                        name="email"
                        value={userData.email}
                        onChange={handleChange}
                        required
                    />
                    {errors.email && <p className="error">{errors.email}</p>}
                </div>
                <div className="form-group">
                    <label>Nowe Hasło (pozostaw puste, jeśli nie chcesz zmieniać):</label>
                    <input
                        type="password"
                        name="haslo"
                        value={userData.haslo}
                        onChange={handleChange}
                    />
                    {errors.haslo && <p className="error">{errors.haslo}</p>}
                </div>
                <div className="form-group">
                    <label>Adres:</label>
                    <input
                        type="text"
                        name="adres"
                        value={userData.adres}
                        onChange={handleChange}
                    />
                    {errors.adres && <p className="error">{errors.adres}</p>}
                </div>

                <div className="form-group">
                    <label>Numer Telefonu:</label>
                    <input
                        type="text"
                        name="numerTelefonu"
                        value={userData.numerTelefonu}
                        onChange={handleChange}
                    />
                    {errors.numerTelefonu && <p className="error">{errors.numerTelefonu}</p>}
                </div>

                <div className="button-group">
                    <button type="submit" className="btn-update">Zaktualizuj Dane</button>
                    <Link to="/orders" className="btn-orders">Moje zamówienia</Link>
                </div>
            </form>
        </div>
    );
}

export default UserPanel;
