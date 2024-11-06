import React, { useEffect, useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';


function CategoryList() {
  const { token } = useContext(AuthContext);
  const [categories, setCategories] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await axios.get('/api/categories', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setCategories(response.data);
      } catch (error) {
        console.error('Błąd podczas pobierania kategorii:', error);
      }
    };

    fetchCategories();
  }, [token]);

  const handleDelete = async (id) => {
    if (window.confirm('Czy na pewno chcesz usunąć tę kategorię?')) {
      try {
        await axios.delete(`/api/categories/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setCategories(categories.filter((category) => category.id !== id));
      } catch (error) {
        console.error('Błąd podczas usuwania kategorii:', error);
      }
    }
  };

  return (
    <div className="category-list-container">
      <h2>Lista Kategorii</h2>
      <table className="category-table">
        <thead>
          <tr>
            <th>Nazwa</th>
            <th>Opis</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {categories.map((category) => (
            <tr key={category.id}>
              <td>{category.nazwa}</td>
              <td>{category.opis}</td>
              <td>
                <button
                  onClick={() => navigate(`/admin/categories/edit/${category.id}`)}
                >
                  Edytuj
                </button>
                <button onClick={() => handleDelete(category.id)}>Usuń</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default CategoryList;
