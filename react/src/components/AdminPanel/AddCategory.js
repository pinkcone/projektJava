import React, { useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';


function AddCategory() {
  const { token } = useContext(AuthContext);

  const [categoryData, setCategoryData] = useState({
    nazwa: '',
    opis: '',
  });

  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setCategoryData({
      ...categoryData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrors({});
    setMessage('');

    let validationErrors = {};

    if (!categoryData.nazwa.trim()) {
      validationErrors.nazwa = 'Nazwa kategorii jest wymagana.';
    }

    if (!categoryData.opis.trim()) {
      validationErrors.opis = 'Opis kategorii jest wymagany.';
    }

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    axios
      .post('/api/categories', categoryData, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((response) => {
        setMessage('Kategoria została dodana pomyślnie.');
        setCategoryData({
          nazwa: '',
          opis: '',
        });
      })
      .catch((error) => {
        if (error.response && error.response.data) {
          setErrors({ serverError: error.response.data.message });
        } else {
          console.error('Błąd:', error);
          setErrors({ serverError: 'Wystąpił błąd podczas dodawania kategorii.' });
        }
      });
  };

  return (
    <div className="add-category-container">
      <h2>Dodaj Nową Kategorię</h2>
      {message && <p className="message">{message}</p>}
      {errors.serverError && <p className="error">{errors.serverError}</p>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Nazwa:</label>
          <input
            type="text"
            name="nazwa"
            value={categoryData.nazwa}
            onChange={handleChange}
            required
          />
          {errors.nazwa && <p className="error">{errors.nazwa}</p>}
        </div>

        <div className="form-group">
          <label>Opis:</label>
          <textarea
            name="opis"
            value={categoryData.opis}
            onChange={handleChange}
            required
          ></textarea>
          {errors.opis && <p className="error">{errors.opis}</p>}
        </div>

        <button type="submit">Dodaj Kategorię</button>
      </form>
    </div>
  );
}

export default AddCategory;
