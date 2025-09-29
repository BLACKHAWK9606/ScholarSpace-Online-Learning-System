import React from 'react';
import { Navbar, Nav, Container, Button, NavDropdown } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { FaUser, FaBell } from 'react-icons/fa';
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
              {/* Dashboard link based on role */}
              <Nav.Link as={Link} to={getDashboardRoute()} className="me-3">
                Dashboard
              </Nav.Link>

              {/* Notifications */}
              <Nav.Link href="#" className="me-3">
                <FaBell />
              </Nav.Link>

              {/* Profile Dropdown */}
              <NavDropdown 
                title={<><FaUser className="me-1" />{user.name}</>} 
                id="profile-dropdown"
                align="end"
              >
                <NavDropdown.Item as={Link} to="/profile">
                  My Profile
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>
                  Logout
                </NavDropdown.Item>
              </NavDropdown>
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