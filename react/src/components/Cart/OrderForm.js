import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './OrderForm.css';

function OrderForm({ totalPrice, cartItems, token, navigate }) {
  const [adres, setAdres] = useState('');
  const [numerTelefonu, setNumerTelefonu] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await axios.get('/api/users/me', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setAdres(response.data.adres || '');
        setNumerTelefonu(response.data.numerTelefonu || '');
      } catch (error) {
        console.error('Error fetching user data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [token]);

  const handlePlaceOrder = async () => {
    if (!adres || !numerTelefonu) {
      alert('Podaj adres oraz numer telefonu.');
      return;
    }

    try {
      await axios.post(
        '/api/orders/place',
        {
          adres,
          numerTelefonu,
          totalPrice
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      alert('Zamówienie zostało złożone pomyślnie.');
      navigate('/orders');
    } catch (error) {
      console.error('Error placing order:', error);

      if (error.response && error.response.data) {
        alert('Error: ' + error.response.data);
      } else if (error.message) {
        alert('Error: ' + error.message);
      } else {
        alert('Nie udało się złożyć zamówienia. Spróbuj ponownie.');
      }
    }
  };

  if (loading) {
    return <p>Loading data...</p>;
  }

  return (
    <div className="order-form">
      <h3>Informacje o wysyłce</h3>
      <label>
        Adres:
        <input
          type="text"
          value={adres}
          onChange={(e) => setAdres(e.target.value)}
        />
      </label>
      <label>
        Numer telefonu:
        <input
          type="text"
          value={numerTelefonu}
          onChange={(e) => setNumerTelefonu(e.target.value)}
        />
      </label>
      <h3>Podsumowanie zamówienia</h3>
      <p>Cena zamówienia: {totalPrice.toFixed(2)} zł</p>
      <p>Metoda płatności: Płatność gotówką</p>
      <p>Sposób dostawy: Kurierem</p>
      <button onClick={handlePlaceOrder}>Złóż zamówienie</button>
    </div>
  );
}

export default OrderForm;
