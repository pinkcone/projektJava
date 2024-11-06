import React, { useState, useContext, useEffect } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import './AddProduct.css';

function AddProduct() {
  const { token } = useContext(AuthContext);

  const [productData, setProductData] = useState({  
    nazwa: '',
    opis: '',
    cena: '',
    gramatura: '',
    iloscNaStanie: '',
    kategorieIds: [],
  });

  const [selectedFile, setSelectedFile] = useState(null);
  const [kategorie, setKategorie] = useState([]);
  const [message, setMessage] = useState('');
  const [errors, setErrors] = useState({});


  useEffect(() => {
    axios
      .get('/api/categories', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((response) => {
        setKategorie(response.data);
      })
      .catch((error) => {
        console.error('Błąd podczas pobierania kategorii:', error);
      });
  }, [token]);

  const handleChange = (e) => {
    setProductData({
      ...productData,
      [e.target.name]: e.target.value,
    });
  };

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
  };

  const handleKategorieChange = (e) => {
    const options = e.target.options;
    const selectedCategories = [];
    for (let i = 0; i < options.length; i++) {
      if (options[i].selected) {
        selectedCategories.push(parseInt(options[i].value));
      }
    }
    setProductData({
      ...productData,
      kategorieIds: selectedCategories,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrors({});
    setMessage('');

    let validationErrors = {};

    if (!productData.nazwa.trim()) {
      validationErrors.nazwa = 'Nazwa produktu jest wymagana.';
    }

    if (!productData.cena) {
      validationErrors.cena = 'Cena jest wymagana.';
    } else if (isNaN(productData.cena) || Number(productData.cena) <= 0) {
      validationErrors.cena = 'Cena musi być liczbą większą od zera.';
    }

    if (productData.iloscNaStanie && (isNaN(productData.iloscNaStanie) || Number(productData.iloscNaStanie) < 0)) {
      validationErrors.iloscNaStanie = 'Ilość na stanie musi być liczbą nieujemną.';
    }

    if (productData.gramatura && (isNaN(productData.gramatura) || Number(productData.gramatura) <= 0)) {
      validationErrors.gramatura = 'Gramatura musi być liczbą większą od zera.';
    }

    if (!selectedFile) {
      validationErrors.zdjecie = 'Zdjęcie jest wymagane.';
    }

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    const formData = new FormData();
    formData.append('nazwa', productData.nazwa);
    formData.append('opis', productData.opis);
    formData.append('cena', productData.cena);
    formData.append('gramatura', productData.gramatura || '');
    formData.append('iloscNaStanie', productData.iloscNaStanie || '');
    formData.append('zdjecie', selectedFile);

    productData.kategorieIds.forEach((id) => {
      formData.append('kategorieIds', id);
    });

    axios
      .post('/api/products', formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
      })
      .then((response) => {
        setMessage('Produkt został dodany pomyślnie.');
        setProductData({
          nazwa: '',
          opis: '',
          cena: '',
          gramatura: '',
          iloscNaStanie: '',
          kategorieIds: [],
        });
        setSelectedFile(null);
      })
      .catch((error) => {
        if (error.response && error.response.data) {
          setErrors({ serverError: error.response.data.message });
        } else {
          console.error('Błąd:', error);
          setErrors({ serverError: 'Wystąpił błąd podczas dodawania produktu.' });
        }
      });
  };

  return (
    <div className="add-product-container">
      <h2>Dodaj Nowy Produkt</h2>
      {message && <p className="message">{message}</p>}
      {errors.serverError && <p className="error">{errors.serverError}</p>}
      <form onSubmit={handleSubmit}>
      <div className="form-group">
          <label>Nazwa:</label>
          <input
            type="text"
            name="nazwa"
            value={productData.nazwa}
            onChange={handleChange}
            required
          />
          {errors.nazwa && <p className="error">{errors.nazwa}</p>}
        </div>

        <div className="form-group">
          <label>Opis:</label>
          <textarea
            name="opis"
            value={productData.opis}
            onChange={handleChange}
          ></textarea>
          {errors.opis && <p className="error">{errors.opis}</p>}
        </div>

        <div className="form-group">
          <label>Cena:</label>
          <input
            type="number"
            step="0.01"
            name="cena"
            value={productData.cena}
            onChange={handleChange}
            required
          />
          {errors.cena && <p className="error">{errors.cena}</p>}
        </div>

        <div className="form-group">
          <label>Gramatura (g):</label>
          <input
            type="number"
            step="0.01"
            name="gramatura"
            value={productData.gramatura}
            onChange={handleChange}
          />
          {errors.gramatura && <p className="error">{errors.gramatura}</p>}
        </div>

        <div className="form-group">
          <label>Ilość na stanie:</label>
          <input
            type="number"
            name="iloscNaStanie"
            value={productData.iloscNaStanie}
            onChange={handleChange}
          />
          {errors.iloscNaStanie && <p className="error">{errors.iloscNaStanie}</p>}
        </div>
        <div className="form-group">
          <label>Zdjęcie:</label>
          <input
            type="file"
            name="zdjecie"
            accept="image/*"
            onChange={handleFileChange}
          />
          {errors.zdjecie && <p className="error">{errors.zdjecie}</p>}
        </div>

        <div className="form-group">
          <label>Kategorie:</label>
          <select
            multiple
            name="kategorieIds"
            value={productData.kategorieIds}
            onChange={handleKategorieChange}
          >
            {kategorie.map((kategoria) => (
              <option key={kategoria.id} value={kategoria.id}>
                {kategoria.nazwa}
              </option>
            ))}
          </select>
          {errors.kategorieIds && <p className="error">{errors.kategorieIds}</p>}
        </div>

        <button type="submit">Dodaj Produkt</button>
      </form>
    </div>
  );
}

export default AddProduct;
