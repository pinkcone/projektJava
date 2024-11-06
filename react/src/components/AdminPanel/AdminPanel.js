import React from 'react';
import { Link } from 'react-router-dom';
import './AdminPanel.css';

function AdminPanel() {
  return (
    <div className="admin-panel-container">
      <h2>Panel Administratora</h2>
      <nav>
        <ul>
          <li>
            <Link to="/admin/products">Lista Produktów</Link>
          </li>
          <li>
            <Link to="/admin/add-product">Dodaj Produkt</Link>
          </li>
          <li>
            <Link to="/admin/categories">Kategorie</Link>
          </li>
          <li>
            <Link to="/admin/add-category">Dodaj Kategorię</Link>
          </li>
          <li>
            <Link to="/admin/orders">Zamówienia</Link>
          </li>
          <li>
            <Link to="/admin/discount-codes">Zarządzaj Kuponami Rabatowymi</Link>
          </li>
        </ul>
      </nav>
    </div>
  );
}

export default AdminPanel;
