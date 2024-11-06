import React, { useEffect, useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate, Navigate } from 'react-router-dom';


function AdminOrders() {
  const { token, hasRole } = useContext(AuthContext);
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [statusChanges, setStatusChanges] = useState({});

  useEffect(() => {
    if (!hasRole('ADMIN')) {
      navigate('/login');
      return;
    }

    const fetchOrders = async () => {
      try {
        const response = await axios.get('/api/orders', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setOrders(response.data);
      } catch (error) {
        console.error('Błąd podczas pobierania zamówień:', error);
        alert('Nie udało się pobrać zamówień.');
      }
    };

    fetchOrders();
  }, [token, hasRole, navigate]);

  const handleStatusChange = (orderId, newStatus) => {
    setStatusChanges((prevStatusChanges) => ({
      ...prevStatusChanges,
      [orderId]: newStatus,
    }));
  };

  const confirmStatusChange = async (orderId) => {
    const newStatus = statusChanges[orderId];
    try {
      await axios.put(
        `/api/orders/${orderId}/status`,
        { status: newStatus },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setOrders((prevOrders) =>
        prevOrders.map((order) =>
          order.id === orderId ? { ...order, status: newStatus } : order
        )
      );
      setStatusChanges((prevStatusChanges) => {
        const newStatusChanges = { ...prevStatusChanges };
        delete newStatusChanges[orderId];
        return newStatusChanges;
      });
      alert('Status zamówienia został zaktualizowany.');
    } catch (error) {
      console.error('Błąd podczas aktualizacji statusu zamówienia:', error);
      alert('Nie udało się zaktualizować statusu zamówienia.');
    }
  };

  if (!hasRole('ADMIN')) {
    return <Navigate to="/login" />;
  }

  return (
    <div className="admin-orders-container">
      <h2>Wszystkie zamówienia</h2>
      <table className="admin-orders-table">
        <thead>
          <tr>
            <th>ID Zamówienia</th>
            <th>Data Zamówienia</th>
            <th>Status</th>
            <th>Klient</th>
            <th>Adres</th>
            <th>Numer Telefonu</th>
            <th>Produkty</th>
            <th>Całkowita Cena</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => {
            const currentStatus = statusChanges[order.id] || order.status;
            return (
              <tr key={order.id}>
                <td>{order.id}</td>
                <td>{new Date(order.datazamowienia).toLocaleString()}</td>
                <td>
                  <select
                    value={currentStatus}
                    onChange={(e) =>
                      handleStatusChange(order.id, e.target.value)
                    }
                  >
                    <option value="NOWE">NOWE</option>
                    <option value="W_TRAKCIE_PRZETWARZANIA">
                      W TRAKCIE PRZETWARZANIA
                    </option>
                    <option value="WYSŁANE">WYSŁANE</option>
                    <option value="DOSTARCZONE">DOSTARCZONE</option>
                    <option value="ANULOWANE">ANULOWANE</option>
                  </select>
                </td>
                <td>
                  {order.uzytkownikImie} {order.uzytkownikNazwisko}
                  <br />
                  {order.uzytkownikEmail}
                </td>
                <td>{order.adres}</td>
                <td>{order.numerTelefonu}</td>
                <td>
                  <ul>
                    {order.pozycjeZamowienia.map((item) => (
                      <li key={item.id}>
                        {item.produkt.nazwa} x {item.ilosc}
                      </li>
                    ))}
                  </ul>
                </td>
                <td>{order.calkowitaCena.toFixed(2)} zł</td>
                <td>
                  {statusChanges[order.id] &&
                  statusChanges[order.id] !== order.status ? (
                    <button onClick={() => confirmStatusChange(order.id)}>
                      Potwierdź
                    </button>
                  ) : null}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default AdminOrders;
