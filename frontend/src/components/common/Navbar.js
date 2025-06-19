import React from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

function AppNavbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Determine dashboard route based on user role
  const getDashboardRoute = () => {
    if (!user) return '/login';
    
    // Convert role to lowercase for case-insensitive comparison
    const role = user.role.toLowerCase();
    
    switch(role) {
      case 'admin':
        return '/admin/dashboard';
      case 'instructor':
        return '/instructor/dashboard';
      case 'student':
        return '/student/dashboard';
      default:
        return '/login';
    }
  };

  // Handle logout
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container>
        {/* Brand/Logo */}
        <Navbar.Brand as={Link} to={user ? getDashboardRoute() : '/'}>
          ScholarSpace
        </Navbar.Brand>

        {/* Responsive toggle */}
        <Navbar.Toggle aria-controls="basic-navbar-nav" />

        <Navbar.Collapse id="basic-navbar-nav">
          {user ? (
            // Authenticated user navigation
            <Nav className="ms-auto align-items-center">
              {/* User greeting - use name from our backend format */}
              <Navbar.Text className="me-3">
                Welcome, {user.name}
              </Navbar.Text>

              {/* Dashboard link based on role */}
              <Nav.Link as={Link} to={getDashboardRoute()} className="me-3">
                Dashboard
              </Nav.Link>

              {/* Logout button */}
              <Button variant="outline-light" onClick={handleLogout}>
                Logout
              </Button>
            </Nav>
          ) : (
            // Unauthenticated user navigation
            <Nav className="ms-auto">
              <Nav.Link as={Link} to="/login" className="me-3">
                Login
              </Nav.Link>
              <Nav.Link as={Link} to="/register">
                Register
              </Nav.Link>
            </Nav>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default AppNavbar;