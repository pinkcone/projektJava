import React, { useEffect, useState, useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './ProductDetail.css';
import { AuthContext } from '../../contexts/AuthContext';

function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { token } = useContext(AuthContext);

    const [product, setProduct] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const backendUrl = 'http://localhost:8080';

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const response = await axios.get(`/api/products/${id}`);
                setProduct(response.data);
            } catch (error) {
                console.error('Error fetching product:', error);
                setError('Failed to load product.');
            } finally {
                setLoading(false);
            }
        };

        fetchProduct();
    }, [id]);

    const handleAddToCart = async () => {
        if (!token) {
            alert('Musisz być zalogowany, aby dodać produkt do koszyka.');
            navigate('/login');
            return;
        }

        try {
            await axios.post(
                '/api/carts/add',
                {
                    productId: product.id,
                    quantity: parseInt(quantity, 10),
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            alert('Produkt został dodany do koszyka.');
        } catch (error) {
            console.error('Błąd podczas dodawania do koszyka:', error);
            if (error.response && error.response.status === 400) {
                alert(error.response.data);
            } else {
                alert('Nie udało się dodać produktu do koszyka.');
            }
        }
    };

    if (loading) {
        return <p>Loading product...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div className="product-detail-container">
            <div className='left'>
                <img
                    src={`${backendUrl}/uploads/images/${product.zdjecie}`}
                    alt={product.nazwa}
                    className="product-image"
                />
            </div>
            <div className='right'>
                <h1>{product.nazwa}</h1>
                <p>Cena: {product.cena.toFixed(2)} zł</p>
                <p className='desc'>{product.opis}</p>
                <p>Waga: {product.gramatura} g</p>
                
                <p>Ilość na stanie: {product.iloscNaStanie}</p>
                <div className="add-to-cart">
                    <label>
                        Ilość: 
                        <input
                            type="number"
                            min="1"
                            max={product.iloscNaStanie}
                            value={quantity}
                            onChange={(e) => {
                                const value = Math.min(e.target.value, product.iloscNaStanie);
                                setQuantity(value);
                            }}
                        />

                    </label>
                    <button onClick={handleAddToCart}>Dodaj do koszyka</button>
                </div>
            </div>
        </div>
    );
}

export default ProductDetail;
