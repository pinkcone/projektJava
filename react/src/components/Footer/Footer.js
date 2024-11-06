import React from 'react';
import './Footer.css';

function Footer() {
    return (
        <footer className="footer">
            <div className="footer-content">
                <div className="footer-logo">
                    <span role="img" aria-label="cookie">🍪</span> CookieWarehouse
                </div>

                <div className="footer-contact">
                    <h4>Kontakt</h4>
                    <p>Email: support@cookiewarehouse.com</p>
                    <p>Nr tel.: +48 123 456 789</p>
                </div>
            </div>
            <div className="footer-copyright">
                <p>&copy; {new Date().getFullYear()} CookieWarehouse Szymon Dudek, Nazar Filipchuk, Adrian Gazdowicz</p>
            </div>
        </footer>
    );
}

export default Footer;
