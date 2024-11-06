import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import './UserOrders.css';

function UserOrders() {
  const { token } = useContext(AuthContext);
  const [orders, setOrders] = useState([]);
  const [expandedOrderId, setExpandedOrderId] = useState(null);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const response = await axios.get('/api/orders/my', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setOrders(response.data);
      } catch (error) {
        console.error('Błąd podczas pobierania zamówień:', error);
      }
    };

    fetchOrders();
  }, [token]);

  const toggleOrderDetails = (orderId) => {
    setExpandedOrderId(expandedOrderId === orderId ? null : orderId);
  };

  const handleCancelOrder = async (orderId) => {
    if (
      !window.confirm(
        'Czy na pewno chcesz anulować to zamówienie? Ta operacja nie może zostać cofnięta.'
      )
    ) {
      return;
    }

    try {
      await axios.post(
        `/api/orders/${orderId}/cancel`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId ? { ...order, status: 'ANULOWANE' } : order
        )
      );
      alert('Zamówienie zostało anulowane.');
    } catch (error) {
      console.error('Błąd podczas anulowania zamówienia:', error);
      alert('Nie udało się anulować zamówienia.');
    }
  };

  return (
    <div className="user-orders">
      <h2>Moje zamówienia</h2>
      {orders.length === 0 ? (
        <p>Nie masz jeszcze żadnych zamówień.</p>
      ) : (
        orders.map((order) => (
          <div key={order.id} className="order-item">
            <div
              className="order-summary"
              onClick={() => toggleOrderDetails(order.id)}
            >
              <p>Zamówienie ID: {order.id}</p>
              <p>Status: {order.status}</p>
              <p>Data zamówienia: {new Date(order.datazamowienia).toLocaleString()}</p>
              <p>Łączna cena: {order.calkowitaCena.toFixed(2)} zł</p>
            </div>
            {expandedOrderId === order.id && (
              <div className="order-details">
                <h4>Pozycje zamówienia:</h4>
                <ul>
                  {order.pozycjeZamowienia.map((item) => (
                    <li key={item.id} className="order-item-detail">
                      <img
                        src={`http://localhost:8080/uploads/images/${item.produkt.zdjecie}`}
                        alt={item.produkt.nazwa}
                        className="order-product-image"
                      />
                      <a href={`/products/${item.produkt.id}`}>{item.produkt.nazwa}</a>
                      <p>Ilość: {item.ilosc}</p>
                      <p>Cena: {item.cena.toFixed(2)} zł</p>
                    </li>
                  ))}
                </ul>
                {(order.status === 'NOWE' ||
                  order.status === 'W_TRAKCIE_PRZETWARZANIA') && (
                  <button onClick={() => handleCancelOrder(order.id)}>
                    Anuluj zamówienie
                  </button>
                )}
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default UserOrders;
