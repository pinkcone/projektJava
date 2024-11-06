import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../contexts/AuthContext';

function AdminRoute({ children }) {
  const { token, hasRole } = useContext(AuthContext);

  if (!token || !hasRole('ADMIN')) {
    return <Navigate to="/" />;
  }

  return children;
}

export default AdminRoute;
