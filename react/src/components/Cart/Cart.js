import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import './Cart.css';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import OrderForm from './OrderForm';

function Cart() {
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();

  const [cartItems, setCartItems] = useState([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const [loading, setLoading] = useState(true);
  const [showOrderForm, setShowOrderForm] = useState(false);

  const [discountCodeInput, setDiscountCodeInput] = useState('');
  const [appliedDiscount, setAppliedDiscount] = useState(null);
  const [discountError, setDiscountError] = useState('');
  const [discountedTotalPrice, setDiscountedTotalPrice] = useState(0);

  const backendUrl = 'http://localhost:8080';

  const handleShowOrderForm = () => {
    setShowOrderForm(true);
  };

  useEffect(() => {
    if (!token) {
      alert('Musisz być zalogowany, aby zobaczyć koszyk.');
      navigate('/login');
      return;
    }

    const fetchCart = async () => {
      try {
        const response = await axios.get('/api/carts/my', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setCartItems(response.data.pozycjeKoszyka);
        setTotalPrice(response.data.cenaCalkowita);
        setDiscountedTotalPrice(response.data.cenaCalkowita);
      } catch (error) {
        console.error('Błąd podczas pobierania koszyka:', error);
        alert('Nie udało się załadować koszyka.');
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, [token, navigate]);

  const handleRemove = async (productId) => {
    try {
      const response = await axios.delete(`/api/carts/remove/${productId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setCartItems(response.data.pozycjeKoszyka);
      setTotalPrice(response.data.cenaCalkowita);

      if (appliedDiscount) {
        applyDiscount(appliedDiscount, response.data.cenaCalkowita);
      } else {
        setDiscountedTotalPrice(response.data.cenaCalkowita);
      }
    } catch (error) {
      console.error('Błąd podczas usuwania produktu z koszyka:', error);
      alert('Nie udało się usunąć produktu z koszyka.');
    }
  };

  const handleQuantityChange = async (productId, newQuantity) => {
    if (newQuantity < 1) {
      alert('Ilość musi być przynajmniej 1.');
      return;
    }

    const cartItem = cartItems.find((item) => item.produkt.id === productId);
    if (newQuantity > cartItem.produkt.iloscNaStanie) {
      alert(`Maksymalna dostępna ilość tego produktu to ${cartItem.produkt.iloscNaStanie}.`);
      return;
    }

    try {
      const response = await axios.put(
        '/api/carts/update',
        {
          productId: productId,
          quantity: parseInt(newQuantity, 10),
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setCartItems(response.data.pozycjeKoszyka);
      setTotalPrice(response.data.cenaCalkowita);

      if (appliedDiscount) {
        applyDiscount(appliedDiscount, response.data.cenaCalkowita);
      } else {
        setDiscountedTotalPrice(response.data.cenaCalkowita);
      }
    } catch (error) {
      console.error('Błąd podczas aktualizacji ilości:', error);
      if (error.response && error.response.status === 400) {
        alert(error.response.data);
      } else {
        alert('Nie udało się zaktualizować ilości.');
      }
    }
  };

  const applyDiscount = (discount, currentTotal) => {
    let newTotal = currentTotal;
    if (discount.typ === 'PERCENTAGE') {
      newTotal = currentTotal - (currentTotal * discount.wartosc) / 100;
    } else if (discount.typ === 'FIXED_AMOUNT') {
      newTotal = currentTotal - discount.wartosc;
    }

    newTotal = newTotal < 0 ? 0 : newTotal;
    setDiscountedTotalPrice(newTotal);
  };

  const handleApplyDiscount = async () => {
    if (!discountCodeInput.trim()) {
      setDiscountError('Proszę wprowadzić kod rabatowy.');
      return;
    }

    try {
      const response = await axios.get(`/api/discount-codes/code/${discountCodeInput.trim()}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const discount = response.data;
      setAppliedDiscount(discount);
      setDiscountError('');

      applyDiscount(discount, totalPrice);
      alert('Kod rabatowy został zastosowany.');
    } catch (error) {
      console.error('Błąd podczas weryfikacji kodu rabatowego:', error);
      if (error.response) {
        setDiscountError(error.response.data);
      } else {
        setDiscountError('Nie udało się zastosować kodu rabatowego.');
      }
      setAppliedDiscount(null);
      setDiscountedTotalPrice(totalPrice);
    }
  };

  const handleRemoveDiscount = () => {
    setAppliedDiscount(null);
    setDiscountedTotalPrice(totalPrice);
    setDiscountCodeInput('');
    setDiscountError('');
  };

  if (loading) {
    return <p>Ładowanie koszyka...</p>;
  }

  if (cartItems.length === 0) {
    return <p>Twój koszyk jest pusty.</p>;
  }

  return (
    <div className="cart-container">
      <h2>Twój Koszyk</h2>
      <table className="cart-table">
        <thead>
          <tr>
            <th>Produkt</th>
            <th>Cena za Jednostkę</th>
            <th>Ilość</th>
            <th>Całkowita Cena</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {cartItems.map((item) => (
            <tr key={item.id}>
              <td>
                <img
                  src={`${backendUrl}/uploads/images/${item.produkt.zdjecie}`}
                  alt={item.produkt.nazwa}
                  className="cart-product-image"
                />
                {item.produkt.nazwa}
              </td>
              <td>{item.cena.toFixed(2)} zł</td>
              <td>
                <input
                  type="number"
                  min="1"
                  max={item.produkt.iloscNaStanie}
                  value={item.ilosc}
                  onChange={(e) =>
                    handleQuantityChange(item.produkt.id, e.target.value)
                  }
                />
              </td>
              <td>{(item.cena * item.ilosc).toFixed(2)} zł</td>
              <td>
                <button onClick={() => handleRemove(item.produkt.id)}>
                  Usuń
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="discount-section">
        <h3>Kod Rabatowy</h3>
        <input
          type="text"
          placeholder="Wprowadź kod rabatowy"
          value={discountCodeInput}
          onChange={(e) => setDiscountCodeInput(e.target.value)}
        />
        <button onClick={handleApplyDiscount}>Zastosuj Kod</button>
        {appliedDiscount && (
          <div className="applied-discount">
            <p>
              Zastosowano kod: <strong>{appliedDiscount.kod}</strong> (
              {appliedDiscount.typ === 'PERCENTAGE'
                ? `${appliedDiscount.wartosc}%`
                : `${appliedDiscount.wartosc} zł`}
              )
            </p>
            <button onClick={handleRemoveDiscount}>Usuń Rabat</button>
          </div>
        )}
        {discountError && <p className="error-message">{discountError}</p>}
      </div>

      <h3>Całkowita Cena: {discountedTotalPrice.toFixed(2)} zł</h3>
      <button onClick={handleShowOrderForm}>Przejdź do Zamówienia</button>

      {showOrderForm && (
        <OrderForm
          totalPrice={discountedTotalPrice}
          cartItems={cartItems}
          token={token}
          navigate={navigate}
          appliedDiscount={appliedDiscount}
        />
      )}
    </div>
  );
}

export default Cart;
