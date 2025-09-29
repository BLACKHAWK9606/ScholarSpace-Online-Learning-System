import React, { useState } from 'react';
import { Form, Button, Container, Row, Col, Card, Alert, InputGroup } from 'react-bootstrap';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { FaEnvelope, FaLock, FaSignInAlt } from 'react-icons/fa';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  
  // Check if there's a message from redirect (e.g., after registration)
  const message = location.state?.message || '';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const userData = await login(email, password);
      
      // Ensure role matching is case-insensitive
      const role = userData.role.toLowerCase();

      // Determine dashboard based on user role
      switch(role) {
        case 'admin':
          navigate('/admin/dashboard');
          break;
        case 'instructor':
          navigate('/instructor/dashboard');
          break;
        case 'student':
          navigate('/student/dashboard');
          break;
        default:
          navigate('/login');
          setError('Invalid user role');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.error || 'Invalid email or password');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container fluid className="py-5 login-container bg-light min-vh-100 d-flex align-items-center">
      <Row className="justify-content-center w-100">
        <Col md={8} lg={6} xl={5}>
          <Card className="shadow border-0 rounded-lg overflow-hidden">
            <Card.Header className="bg-primary text-white text-center py-4">
              <h2 className="fw-bold mb-0">
                <FaSignInAlt className="me-2" />
                Login to ScholarSpace
              </h2>
              <p className="text-white-50 mt-2 mb-0">Access your learning dashboard</p>
            </Card.Header>
            
            <Card.Body className="px-4 py-5">
              {message && (
                <Alert variant="success" className="animate__animated animate__fadeIn">
                  {message}
                </Alert>
              )}
              
              {error && (
                <Alert variant="danger" className="animate__animated animate__shakeX">
                  {error}
                </Alert>
              )}
              
              <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-4">
                  <Form.Label>Email address</Form.Label>
                  <InputGroup>
                    <InputGroup.Text>
                      <FaEnvelope />
                    </InputGroup.Text>
                    <Form.Control
                      type="email"
                      placeholder="Enter your email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      className="py-2"
                    />
                  </InputGroup>
                </Form.Group>

                <Form.Group className="mb-4">
                  <div className="d-flex justify-content-between">
                    <Form.Label>Password</Form.Label>
                    <Link to="/forgot-password" className="text-decoration-none small">
                      Forgot password?
                    </Link>
                  </div>
                  <InputGroup>
                    <InputGroup.Text>
                      <FaLock />
                    </InputGroup.Text>
                    <Form.Control
                      type="password"
                      placeholder="Enter your password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="py-2"
                    />
                  </InputGroup>
                </Form.Group>

                <Button 
                  variant="primary" 
                  type="submit" 
                  disabled={isLoading}
                  className="w-100 py-2 mt-3 fw-bold"
                >
                  {isLoading ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      Logging in...
                    </>
                  ) : (
                    'Login'
                  )}
                </Button>
              </Form>
            </Card.Body>
            
            <Card.Footer className="text-center py-4 bg-light">
              <div className="border-bottom pb-3 mb-3">
                <small className="text-muted">Admin or Instructor?</small>
                <div className="mt-1">
                  <small>Contact your institution administrator for account setup</small>
                </div>
              </div>
              
              <div className="d-flex align-items-center justify-content-center">
                <span className="text-muted me-2">🎓</span>
                <span className="me-2">New Student?</span>
                <Link to="/register" className="btn btn-outline-primary btn-sm fw-bold">
                  Create Student Account
                </Link>
              </div>
            </Card.Footer>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}

export default Login;