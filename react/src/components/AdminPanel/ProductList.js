import React, { useEffect, useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import './ProductList.css';

function ProductList() {
  const { token } = useContext(AuthContext);
  const [products, setProducts] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axios.get('/api/products/all', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setProducts(response.data);
      } catch (error) {
        console.error('Błąd podczas pobierania produktów:', error);
      }
    };

    fetchProducts();
  }, [token]);

  const handleDelete = async (id) => {
    if (window.confirm('Czy na pewno chcesz usunąć ten produkt?')) {
      try {
        await axios.delete(`/api/products/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setProducts(products.filter((product) => product.id !== id));
      } catch (error) {
        console.error('Błąd podczas usuwania produktu:', error);
      }
    }
  };

  return (
    <div className="product-list-container">
      <h2>Lista Produktów</h2>
      <table className="product-table">
        <thead>
          <tr>
            <th>Tytuł</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => (
            <tr key={product.id}>
              <td>{product.nazwa}</td>
              <td>
                <button
                  onClick={() => navigate(`/admin/products/edit/${product.id}`)}
                >
                  Edytuj
                </button>
                <button onClick={() => handleDelete(product.id)}>Usuń</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default ProductList;
