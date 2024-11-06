import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../contexts/AuthContext';
import { Link } from 'react-router-dom';
import axios from 'axios';
import './Home.css';
import { Fade } from 'react-slideshow-image';
import 'react-slideshow-image/dist/styles.css'

function Home() {
  const { token, user, logout, hasRole } = useContext(AuthContext);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const backendUrl = 'http://localhost:8080';
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await axios.get('/api/categories');
        setCategories(response.data);
      } catch (error) {
        console.error('Error fetching categories:', error);
      }
    };

    fetchCategories();
  }, []);
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        let url = '/api/products';

        const params = {};
        if (selectedCategory) {
          params.category = selectedCategory;
        }
        if (searchQuery) {
          params.search = searchQuery;
        }
        

        const response = await axios.get(url, { params });
        setProducts(response.data);
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };

    fetchProducts();
  }, [selectedCategory, searchQuery]);


  const fadeImages = [
    {
      url: 'https://static.korso.pl/korsosanockie/articles/image/eb83f413-6b09-4ff5-bf43-7286e5ed2894',
      caption: 'First Slide'
    },
    {
      url: 'https://elite-cukiernia.pl/wp-content/uploads/2023/05/slider_5.webp',
      caption: 'Second Slide'
    },
    {
      url: 'https://www.cukiernianaczasie.pl/wp-content/uploads/2022/05/4.jpg',
      caption: 'Third Slide'
    },
  ];

  return (
    <div className="home-container">
      <div className="slide-container">
      <Fade>
        {fadeImages.map((fadeImage, index) => (
          <div key={index}>
            <img style={{ maxWidth: '100%', maxHeight: '500px' }} src={fadeImage.url} />
            
          </div>
        ))}
      </Fade>
    </div>
      <h2>Nasze Produkty</h2>
      <div className='row'>
        <div className="sidebar">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Wyszukaj produkty..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <h3>Kategorie</h3>
          <ul>
            <li
              className={!selectedCategory ? 'active' : ''}
              onClick={() => setSelectedCategory(null)}
            >
              Wszystkie
            </li>
            {categories.map((category) => (
              <li
                key={category.id}
                className={selectedCategory === category.id ? 'active' : ''}
                onClick={() => setSelectedCategory(category.id)}
              >
                {category.nazwa}
              </li>
            ))}
          </ul>
        </div>

        <div className="main-content">
          <div className="product-list">
            {products.length === 0 ? (
              <p>Brak produktów spełniających kryteria wyszukiwania.</p>
            ) : (
              products.map((product) => (
                <div key={product.id} className="product-item">
                  <Link to={`/products/${product.id}`}>
                    <div
                      className="product-image-div"
                      style={{ backgroundImage: `url(${backendUrl}/uploads/images/${product.zdjecie || 'default.jpg'})` }}
                      aria-label={product.nazwa}
                    />
                    <h3>{product.nazwa}</h3>
                    <p><strong>Cena:</strong> {product.cena.toFixed(2)} zł</p>
                  </Link>
                </div>
              ))
            )}
          </div>
        </div>
        <div className='reklama'>MIEJSCE NA TWOJĄ REKLAMĘ</div>
      </div>
    </div>
  );
}

export default Home;
