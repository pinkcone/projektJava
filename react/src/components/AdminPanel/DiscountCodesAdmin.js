import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import './DiscountCodesAdmin.css';

function DiscountCodesAdmin() {
  const { token } = useContext(AuthContext);
  const [discountCodes, setDiscountCodes] = useState([]);
  const [loading, setLoading] = useState(true);

  const [isEditing, setIsEditing] = useState(false);
  const [currentCode, setCurrentCode] = useState({
    id: null,
    kod: '',
    typ: 'PERCENTAGE',
    wartosc: '',
    dataWaznosci: '',
  });
  const [formError, setFormError] = useState('');

  const backendUrl = 'http://localhost:8080';

  useEffect(() => {
    fetchDiscountCodes();
  }, []);

  const fetchDiscountCodes = async () => {
    try {
      const response = await axios.get('/api/discount-codes', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setDiscountCodes(response.data);
    } catch (error) {
      console.error('Błąd podczas pobierania kuponów rabatowych:', error);
      alert('Nie udało się załadować kuponów rabatowych.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setCurrentCode({
      ...currentCode,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!currentCode.kod || !currentCode.typ || !currentCode.wartosc || !currentCode.dataWaznosci) {
      setFormError('Wszystkie pola są wymagane.');
      return;
    }

    if (currentCode.wartosc <= 0) {
      setFormError('Wartość rabatu musi być dodatnia.');
      return;
    }

    const today = new Date();
    const expiryDate = new Date(currentCode.dataWaznosci);
    if (expiryDate <= today) {
      setFormError('Data ważności musi być w przyszłości.');
      return;
    }

    try {
      if (isEditing) {
        await axios.put(`/api/discount-codes/${currentCode.id}`, {
          kod: currentCode.kod,
          typ: currentCode.typ,
          wartosc: parseFloat(currentCode.wartosc),
          dataWaznosci: currentCode.dataWaznosci,
        }, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        alert('Kod rabatowy został zaktualizowany.');
      } else {
        await axios.post('/api/discount-codes', {
          kod: currentCode.kod,
          typ: currentCode.typ,
          wartosc: parseFloat(currentCode.wartosc),
          dataWaznosci: currentCode.dataWaznosci,
        }, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        alert('Nowy kod rabatowy został dodany.');
      }

      setCurrentCode({
        id: null,
        kod: '',
        typ: 'PERCENTAGE',
        wartosc: '',
        dataWaznosci: '',
      });
      setIsEditing(false);
      setFormError('');

      fetchDiscountCodes();
    } catch (error) {
      console.error('Błąd podczas zapisywania kodu rabatowego:', error);
      if (error.response && error.response.data) {
        setFormError(error.response.data);
      } else {
        setFormError('Nie udało się zapisać kodu rabatowego.');
      }
    }
  };

  const handleEdit = (code) => {
    setIsEditing(true);
    setCurrentCode({
      id: code.id,
      kod: code.kod,
      typ: code.typ,
      wartosc: code.wartosc,
      dataWaznosci: code.dataWaznosci,
    });
    setFormError('');
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Czy na pewno chcesz usunąć ten kod rabatowy?')) {
      return;
    }

    try {
      await axios.delete(`/api/discount-codes/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      alert('Kod rabatowy został usunięty.');
      fetchDiscountCodes();
    } catch (error) {
      console.error('Błąd podczas usuwania kodu rabatowego:', error);
      alert('Nie udało się usunąć kodu rabatowego.');
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setCurrentCode({
      id: null,
      kod: '',
      typ: 'PERCENTAGE',
      wartosc: '',
      dataWaznosci: '',
    });
    setFormError('');
  };

  if (loading) {
    return <p>Ładowanie kuponów rabatowych...</p>;
  }

  return (
    <div className="discount-codes-admin-container">
      <h2>Zarządzaj Kuponami Rabatowymi</h2>
      <div className="discount-form">
        <h3>{isEditing ? 'Edytuj Kod Rabatowy' : 'Dodaj Nowy Kod Rabatowy'}</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Kod Rabatowy:</label>
            <input
              type="text"
              name="kod"
              value={currentCode.kod}
              onChange={handleInputChange}
              required
              disabled={isEditing}
            />
          </div>
          <div className="form-group">
            <label>Typ Rabatu:</label>
            <select
              name="typ"
              value={currentCode.typ}
              onChange={handleInputChange}
              required
            >
              <option value="PERCENTAGE">Procentowy</option>
              <option value="FIXED_AMOUNT">Stała Kwota</option>
            </select>
          </div>
          <div className="form-group">
            <label>Wartość Rabatu:</label>
            <input
              type="number"
              name="wartosc"
              value={currentCode.wartosc}
              onChange={handleInputChange}
              min="0"
              step="0.01"
              required
            />
          </div>
          <div className="form-group">
            <label>Data Ważności:</label>
            <input
              type="date"
              name="dataWaznosci"
              value={currentCode.dataWaznosci}
              onChange={handleInputChange}
              required
            />
          </div>
          {formError && <p className="error-message">{formError}</p>}
          <div className="form-actions">
            <button type="submit">{isEditing ? 'Zaktualizuj' : 'Dodaj'}</button>
            {isEditing && <button type="button" onClick={handleCancelEdit}>Anuluj</button>}
          </div>
        </form>
      </div>

      <div className="discount-codes-list">
        <h3>Lista Kuponów Rabatowych</h3>
        {discountCodes.length === 0 ? (
          <p>Brak dostępnych kuponów rabatowych.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Kod</th>
                <th>Typ Rabatu</th>
                <th>Wartość</th>
                <th>Data Ważności</th>
                <th>Akcje</th>
              </tr>
            </thead>
            <tbody>
              {discountCodes.map((code) => (
                <tr key={code.id}>
                  <td>{code.id}</td>
                  <td>{code.kod}</td>
                  <td>{code.typ === 'PERCENTAGE' ? 'Procentowy' : 'Stała Kwota'}</td>
                  <td>
                    {code.typ === 'PERCENTAGE' ? `${code.wartosc}%` : `${code.wartosc} zł`}
                  </td>
                  <td>{new Date(code.dataWaznosci).toLocaleDateString()}</td>
                  <td>
                    <button onClick={() => handleEdit(code)}>Edytuj</button>
                    <button onClick={() => handleDelete(code.id)}>Usuń</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default DiscountCodesAdmin;
