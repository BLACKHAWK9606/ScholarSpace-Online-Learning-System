import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

function PrivateRoute({ children, allowedRoles }) {
  const { user, isAuthenticated, isLoading } = useAuth();

  // Show loading indicator while authentication state is being determined
  if (isLoading) {
    return <div>Loading...</div>;
  }

  // Check if user is authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // If allowed roles are specified, check user's role
  if (allowedRoles && user && user.role) {
    // Convert roles to lowercase for case-insensitive comparison
    const userRole = user.role.toLowerCase();
    const normalizedAllowedRoles = allowedRoles.map(role => role.toLowerCase());
    
    if (!normalizedAllowedRoles.includes(userRole)) {
      // Redirect to appropriate dashboard based on user's role
      switch(userRole) {
        case 'admin':
          return <Navigate to="/admin/dashboard" replace />;
        case 'instructor':
          return <Navigate to="/instructor/dashboard" replace />;
        case 'student':
          return <Navigate to="/student/dashboard" replace />;
        default:
          return <Navigate to="/login" replace />;
      }
    }
  }

  // If all checks pass, render the children
  return children;
}

export default PrivateRoute;