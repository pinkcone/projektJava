import React, { useEffect, useState, useContext, useRef, useCallback } from 'react';
import axios from 'axios';
import { AuthContext } from '../../contexts/AuthContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBell } from '@fortawesome/free-solid-svg-icons';
import './Notifications.css';

function Notifications() {
  const { token } = useContext(AuthContext);
  const [notifications, setNotifications] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  const markAllAsRead = useCallback(async () => {
    try {
      await axios.post('/api/notifications/markAllAsRead', {}, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setNotifications([]);
    } catch (error) {
      console.error('Błąd podczas oznaczania powiadomień jako przeczytanych:', error);
    }
  }, [token]);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const response = await axios.get('/api/notifications', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setNotifications(response.data);
      } catch (error) {
        console.error('Błąd podczas pobierania powiadomień:', error);
      }
    };

    fetchNotifications();

    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, [token]);

  const toggleDropdown = () => {
    setIsDropdownOpen((prev) => !prev);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        if (isDropdownOpen) {
          setIsDropdownOpen(false);
          markAllAsRead();
        }
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isDropdownOpen, markAllAsRead]);

  const unreadCount = notifications.length;

  return (
    <div className="notifications-container" ref={dropdownRef}>
      <div className="bell-icon" onClick={toggleDropdown}>
        <FontAwesomeIcon icon={faBell} className={unreadCount > 0 ? 'bell-active' : ''} />
        {unreadCount > 0 && <span className="notification-dot"></span>}
      </div>

      {isDropdownOpen && (
        <div className="notifications-dropdown">
          <h3>Powiadomienia</h3>
          {notifications.length === 0 ? (
            <p>Brak nowych powiadomień.</p>
          ) : (
            <ul>
              {notifications.map((notification) => (
                <li key={notification.id}>
                  <p>{notification.tresc}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export default Notifications;
