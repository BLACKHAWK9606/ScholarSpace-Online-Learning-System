import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

function FirstTimeLoginCheck({ children }) {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && user && user.role === 'INSTRUCTOR' && user.isFirstLogin) {
      navigate('/change-password');
    }
  }, [isAuthenticated, user, navigate]);

  // If user needs to change password, don't render children
  if (isAuthenticated && user && user.role === 'INSTRUCTOR' && user.isFirstLogin) {
    return null;
  }

  return children;
}

export default FirstTimeLoginCheck;