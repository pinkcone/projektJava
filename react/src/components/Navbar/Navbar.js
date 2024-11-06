import React, { useContext, useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { AuthContext } from '../../contexts/AuthContext';
import { FaShoppingCart } from 'react-icons/fa';
import Notifications from '../Notifications/Notifications';
import './Navbar.css';

function Navbar() {
    const { user, hasRole, logout } = useContext(AuthContext);
    const location = useLocation();
    const [dropdownOpen, setDropdownOpen] = useState(false);
    const [showOnlyRegister, setShowOnlyRegister] = useState(false);
    const [showOnlyLogin, setShowOnlyLogin] = useState(false);

    useEffect(() => {
        if (location.pathname === '/login') {
            setShowOnlyRegister(true);
            setShowOnlyLogin(false);
        } else if (location.pathname === '/register') {
            setShowOnlyLogin(true);
            setShowOnlyRegister(false);
        } else {
            setShowOnlyRegister(false);
            setShowOnlyLogin(false);
        }
    }, [location.pathname]);

    const toggleDropdown = () => setDropdownOpen(!dropdownOpen);
    const handleLogout = () => {
        logout();
    };

    return (
        <nav className="navbar">
            <div className="navbar-logo">
                <Link to="/">🍪 CookieWarehouse</Link>
            </div>
            <ul className="navbar-links">
                {user ? (
                    <>
                    <li><Notifications/></li>
                        <li>
                            <Link to="/cart" className="cart-icon" title="Mój koszyk">
                                <FaShoppingCart/>
                            </Link>
                        </li>
                        <li className="dropdown" onClick={toggleDropdown}>
                            <span className="dropdown-toggle">{user.email}</span>
                            {dropdownOpen && (
                                <div className="dropdown-menu">
                                    {hasRole('ADMIN') && <li><Link to="/admin">Panel administratora</Link></li>}
                                    <li><Link to="/user-panel">Moje dane</Link></li>
                                    <li><span className="dropdown-item" onClick={handleLogout}>Wyloguj</span></li>
                                </div>
                            )}
                        </li>
                    </>
                ) : (
                    <>
                        {!showOnlyRegister && !showOnlyLogin && (
                            <>
                                <li><Link to="/login">Logowanie</Link></li>
                                <li><Link to="/register">Rejestracja</Link></li>
                            </>
                        )}
                        {showOnlyRegister && (
                            <li><Link to="/register">Rejestracja</Link></li>
                        )}
                        {showOnlyLogin && (
                            <li><Link to="/login">Logowanie</Link></li>
                        )}
                    </>
                )}
            </ul>
        </nav>
    );
}

export default Navbar;
