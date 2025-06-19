import React, { useState, useEffect } from 'react';
import { 
  Container, Card, Button, Table, Form, Modal, 
  Spinner, Alert, Row, Col, Badge
} from 'react-bootstrap';
import { FaEdit, FaUserPlus, FaCheck, FaTimes } from 'react-icons/fa';
import { adminService } from '../../services/api';
import axios from 'axios';

// Create a direct API instance for direct calls
const api = axios.create({
  baseURL: 'http://localhost:9090/api/',
  headers: {
    'Content-Type': 'application/json',
  }
});

// Add interceptor to include token in all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

function ManageInstructors() {
  // State for instructors and UI
  const [instructors, setInstructors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [currentInstructor, setCurrentInstructor] = useState({
    firstName: '',
    lastName: '',
    email: '',
    departmentId: '',
    password: 'defaultPassword123', // Default password
    isActive: true
  });
  const [isEditing, setIsEditing] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const [submitLoading, setSubmitLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // Fetch instructors and departments when component mounts
  useEffect(() => {
    fetchInstructors();
    fetchDepartments();
  }, []);

  // Function to fetch instructors
  const fetchInstructors = async () => {
    setLoading(true);
    try {
      const response = await adminService.getAllInstructors();
      setInstructors(response.data);
      setError(null);
    } catch (err) {
      console.error("Error fetching instructors:", err);
      setError("Failed to load instructors. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  // Function to fetch departments
  const fetchDepartments = async () => {
    try {
      const response = await adminService.getAllDepartments();
      setDepartments(response.data);
    } catch (err) {
      console.error("Error fetching departments:", err);
      // Don't set error here to avoid overriding instructor errors
    }
  };

  // Handle input change for form fields
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setCurrentInstructor({
      ...currentInstructor,
      [name]: type === 'checkbox' ? checked : value
    });
    
    // Clear validation error when field is edited
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: null
      });
    }
  };

  // Validate form before submission
  const validateForm = () => {
    const errors = {};
    if (!currentInstructor.firstName.trim()) errors.firstName = "First name is required";
    if (!currentInstructor.lastName.trim()) errors.lastName = "Last name is required";
    if (!currentInstructor.email.trim()) errors.email = "Email is required";
    if (!currentInstructor.email.includes('@')) errors.email = "Invalid email format";
    if (!currentInstructor.departmentId) errors.departmentId = "Department is required";
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setSubmitLoading(true);
    try {
      if (isEditing) {
        console.log('Updating instructor with data:', currentInstructor);
        
        // First update the user information directly using the API
        await api.put(`users/${currentInstructor.userId}`, {
          name: `${currentInstructor.firstName} ${currentInstructor.lastName}`,
          email: currentInstructor.email,
          isActive: currentInstructor.isActive
        });
        
        console.log('User information updated successfully');
        
        // Then update the instructor's department if changed
        if (currentInstructor.instructorId) {
          const instructor = await adminService.getInstructorById(currentInstructor.instructorId);
          if (instructor.data && instructor.data.department.departmentId !== currentInstructor.departmentId) {
            // Update the department
            await adminService.updateInstructor(currentInstructor.instructorId, {
              departmentId: currentInstructor.departmentId,
              specialization: currentInstructor.specialization || '',
              officeLocation: currentInstructor.officeLocation || '',
              officeHours: currentInstructor.officeHours || ''
            });
            console.log('Instructor department updated successfully');
          }
        }
        
        setSuccessMessage("Instructor updated successfully!");
      } else {
        // Create new instructor
        console.log("Creating instructor with data:", {
          firstName: currentInstructor.firstName,
          lastName: currentInstructor.lastName,
          email: currentInstructor.email,
          departmentId: currentInstructor.departmentId,
          password: currentInstructor.password,
          isActive: currentInstructor.isActive
        });
        
        const response = await adminService.createInstructor({
          firstName: currentInstructor.firstName,
          lastName: currentInstructor.lastName,
          email: currentInstructor.email,
          departmentId: currentInstructor.departmentId,
          password: currentInstructor.password,
          isActive: currentInstructor.isActive,
          specialization: currentInstructor.specialization || '',
          officeLocation: currentInstructor.officeLocation || '',
          officeHours: currentInstructor.officeHours || ''
        });
        
        console.log("Instructor created successfully:", response);
        setSuccessMessage("Instructor created successfully!");
      }
      
      // Refresh instructor list and close modal
      fetchInstructors();
      handleCloseModal();
    } catch (err) {
      console.error("Error saving instructor:", err);
      // Display more detailed error message if available
      if (err.response && err.response.data && err.response.data.error) {
        setError(`Failed to save instructor: ${err.response.data.error}`);
      } else {
        setError("Failed to save instructor. Please try again.");
      }
    } finally {
      setSubmitLoading(false);
    }
  };

  // Handle instructor edit
  const handleEditInstructor = (instructor) => {
    // Extract user data from the instructor object
    const user = instructor.user || instructor;
    // Split the name into first and last name if needed
    let firstName = '', lastName = '';
    if (user.name) {
      const nameParts = user.name.split(' ');
      firstName = nameParts[0] || '';
      lastName = nameParts.slice(1).join(' ') || '';
    } else {
      firstName = user.firstName || '';
      lastName = user.lastName || '';
    }
    
    setCurrentInstructor({
      instructorId: instructor.instructorId,
      userId: user.userId,
      firstName: firstName,
      lastName: lastName,
      email: user.email,
      departmentId: instructor.department?.departmentId || '',
      isActive: user.isActive === undefined ? true : user.isActive
    });
    setIsEditing(true);
    setShowModal(true);
  };

  // Handle instructor activation/deactivation
  const handleToggleStatus = async (instructor) => {
    console.log('Toggling status for instructor:', instructor);
    
    // Extract user ID from instructor
    // Make sure we get the correct user ID
    const userId = instructor.user?.userId || instructor.userId;
    const isActive = instructor.user?.isActive || instructor.isActive;
    
    console.log('User ID for toggle:', userId);
    console.log('Current active status:', isActive);
    
    if (!userId) {
      console.error('Missing user ID:', instructor);
      setError("Cannot toggle status: Missing user ID");
      return;
    }
    
    try {
      console.log(`Attempting to ${isActive ? 'deactivate' : 'activate'} user with ID:`, userId);
      
      // Use the user ID for activation/deactivation
      if (isActive) {
        await api.put(`users/${userId}/deactivate`);
      } else {
        await api.put(`users/${userId}/activate`);
      }
      
      // Refresh instructor list
      fetchInstructors();
      setSuccessMessage(`Instructor ${isActive ? 'deactivated' : 'activated'} successfully!`);
    } catch (err) {
      console.error("Error toggling instructor status:", err);
      setError(`Failed to ${isActive ? 'deactivate' : 'activate'} instructor. Please try again.`);
    }
  };

  // Open modal for adding new instructor
  const handleAddInstructor = () => {
    setCurrentInstructor({
      firstName: '',
      lastName: '',
      email: '',
      departmentId: '',
      password: 'defaultPassword123', // Default password
      isActive: true
    });
    setIsEditing(false);
    setShowModal(true);
  };

  // Close modal and reset form
  const handleCloseModal = () => {
    setShowModal(false);
    setFormErrors({});
  };

  // Clear success message after 5 seconds
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => {
        setSuccessMessage('');
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  // Render loading spinner
  if (loading && instructors.length === 0) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <Spinner animation="border" variant="primary" />
      </Container>
    );
  }

  return (
    <Container fluid className="p-4">
      {/* Success message */}
      {successMessage && (
        <Alert variant="success" dismissible onClose={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}
      
      {/* Error message */}
      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      
      <Card className="shadow-sm">
        <Card.Header className="bg-white">
          <div className="d-flex justify-content-between align-items-center">
            <h4 className="mb-0">Manage Instructors</h4>
            <Button variant="primary" onClick={handleAddInstructor}>
              <FaUserPlus className="me-2" /> Add Instructor
            </Button>
          </div>
        </Card.Header>
        <Card.Body>
          <Table responsive hover className="align-middle">
            <thead className="bg-light">
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {instructors.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center py-4">
                    No instructors found. Add your first instructor!
                  </td>
                </tr>
              ) : (
                instructors.map(instructor => {
                  // Handle both nested and flat instructor objects
                  const user = instructor.user || instructor;
                  const department = instructor.department || { name: 'Not Assigned' };
                  
                  return (
                    <tr key={user.userId}>
                      <td>
                        <div className="fw-bold">
                          {user.firstName} {user.lastName}
                        </div>
                      </td>
                      <td>{user.email}</td>
                      <td>{department.name}</td>
                      <td>
                        <Badge bg={user.isActive ? 'success' : 'danger'}>
                          {user.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </td>
                      <td>
                        <Button 
                          variant="outline-primary" 
                          size="sm" 
                          className="me-2"
                          onClick={() => handleEditInstructor(instructor)}
                        >
                          <FaEdit />
                        </Button>
                        <Button 
                          variant={user.isActive ? 'outline-danger' : 'outline-success'} 
                          size="sm"
                          onClick={() => handleToggleStatus(instructor)}
                        >
                          {user.isActive ? <FaTimes /> : <FaCheck />}
                        </Button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {/* Add/Edit Instructor Modal */}
      <Modal show={showModal} onHide={handleCloseModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>{isEditing ? 'Edit Instructor' : 'Add New Instructor'}</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleSubmit}>
          <Modal.Body>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>First Name</Form.Label>
                  <Form.Control
                    type="text"
                    name="firstName"
                    value={currentInstructor.firstName}
                    onChange={handleInputChange}
                    isInvalid={!!formErrors.firstName}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.firstName}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Last Name</Form.Label>
                  <Form.Control
                    type="text"
                    name="lastName"
                    value={currentInstructor.lastName}
                    onChange={handleInputChange}
                    isInvalid={!!formErrors.lastName}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.lastName}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
            
            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                name="email"
                value={currentInstructor.email}
                onChange={handleInputChange}
                isInvalid={!!formErrors.email}
              />
              <Form.Control.Feedback type="invalid">
                {formErrors.email}
              </Form.Control.Feedback>
            </Form.Group>
            
            <Form.Group className="mb-3">
              <Form.Label>Department</Form.Label>
              <Form.Select
                name="departmentId"
                value={currentInstructor.departmentId}
                onChange={handleInputChange}
                isInvalid={!!formErrors.departmentId}
              >
                <option value="">Select Department</option>
                {departments.map(dept => (
                  <option key={dept.departmentId} value={dept.departmentId}>
                    {dept.name}
                  </option>
                ))}
              </Form.Select>
              <Form.Control.Feedback type="invalid">
                {formErrors.departmentId}
              </Form.Control.Feedback>
            </Form.Group>
            
            {!isEditing && (
              <Form.Group className="mb-3">
                <Form.Label>Default Password</Form.Label>
                <Form.Control
                  type="text"
                  name="password"
                  value={currentInstructor.password}
                  onChange={handleInputChange}
                  disabled
                />
                <Form.Text className="text-muted">
                  Instructor will be required to change password on first login.
                </Form.Text>
              </Form.Group>
            )}
            
            <Form.Group className="mb-3">
              <Form.Check
                type="switch"
                id="instructor-status"
                label="Active"
                name="isActive"
                checked={currentInstructor.isActive}
                onChange={handleInputChange}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseModal}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={submitLoading}>
              {submitLoading ? (
                <>
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                  Saving...
                </>
              ) : (
                'Save Instructor'
              )}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}

export default ManageInstructors;