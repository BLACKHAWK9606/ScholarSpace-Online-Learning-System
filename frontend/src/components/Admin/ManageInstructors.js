import React, { useState, useEffect, useMemo } from 'react';
import { 
  Container, Card, Button, Table, Form, Modal, 
  Spinner, Alert, Row, Col
} from 'react-bootstrap';
import { FaEdit, FaUserPlus, FaChevronDown, FaChevronRight, FaBook, FaPlus, FaTimes } from 'react-icons/fa';
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
  const [courses, setCourses] = useState([]);
  const [instructorCourses, setInstructorCourses] = useState({});
  const [expandedInstructors, setExpandedInstructors] = useState(new Set());
  const [showCourseModal, setShowCourseModal] = useState(false);
  const [selectedInstructor, setSelectedInstructor] = useState(null);
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
    fetchCourses();
  }, []);

  // Function to fetch instructors
  const fetchInstructors = async () => {
    setLoading(true);
    try {
      const response = await adminService.getUsersByRole('INSTRUCTOR');
      const instructorUsers = response.data || [];
      // Transform user data to instructor format
      const transformedInstructors = instructorUsers.map(user => ({
        instructorId: user.userId, // Use userId as instructorId for now
        user: user,
        department: { name: 'Not Assigned' } // Default department
      }));
      setInstructors(transformedInstructors);
      setError(null);
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Failed to load instructors. Please try again later.";
      console.error("Error fetching instructors:", errorMessage, err);
      setError(errorMessage);
      setInstructors([]);
    } finally {
      setLoading(false);
    }
  };

  // Function to fetch departments
  const fetchDepartments = async () => {
    try {
      const response = await adminService.getAllDepartments();
      setDepartments(response.data || []);
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Failed to load departments";
      console.error("Error fetching departments:", errorMessage, err);
      setDepartments([]);
    }
  };

  // Function to fetch courses
  const fetchCourses = async () => {
    try {
      const response = await adminService.getAllCourses();
      setCourses(response.data || []);
    } catch (err) {
      console.error("Error fetching courses:", err);
      setCourses([]);
    }
  };

  // Toggle instructor expansion
  const toggleInstructor = async (instructorId) => {
    const newExpanded = new Set(expandedInstructors);
    if (newExpanded.has(instructorId)) {
      newExpanded.delete(instructorId);
    } else {
      newExpanded.add(instructorId);
      if (!instructorCourses[instructorId]) {
        // For now, set empty courses since we don't have instructor-course relationships
        setInstructorCourses(prev => ({ ...prev, [instructorId]: [] }));
      }
    }
    setExpandedInstructors(newExpanded);
  };

  // Handle course assignment
  const handleAssignCourse = (instructor) => {
    setSelectedInstructor(instructor);
    setShowCourseModal(true);
  };

  // Assign course to instructor (placeholder for now)
  const assignCourseToInstructor = async (courseId) => {
    setSuccessMessage('Course assignment feature coming soon');
    setShowCourseModal(false);
    setTimeout(() => setSuccessMessage(''), 3000);
  };

  // Remove course from instructor (placeholder for now)
  const removeCourseFromInstructor = async (instructorId, courseId) => {
    setSuccessMessage('Course removal feature coming soon');
    setTimeout(() => setSuccessMessage(''), 3000);
  };

  // Handle input change for form fields
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    const newValue = type === 'checkbox' ? checked : value;
    
    setCurrentInstructor(prev => ({
      ...prev,
      [name]: newValue
    }));
    
    // Clear validation error when field is edited
    if (formErrors[name]) {
      setFormErrors(prev => ({
        ...prev,
        [name]: null
      }));
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
        // Update the user information directly using the API
        const updateData = {
          name: `${currentInstructor.firstName} ${currentInstructor.lastName}`,
          email: currentInstructor.email,
          isActive: currentInstructor.isActive
        };
        
        await api.put(`users/${currentInstructor.userId}`, updateData);
        
        // Update the instructor's department
        if (currentInstructor.instructorId && currentInstructor.departmentId) {
          await adminService.updateInstructor(currentInstructor.instructorId, {
            departmentId: currentInstructor.departmentId,
            specialization: currentInstructor.specialization || '',
            officeLocation: currentInstructor.officeLocation || '',
            officeHours: currentInstructor.officeHours || ''
          });
        }
        
        // Update the instructor in the local state immediately
        const updatedName = `${currentInstructor.firstName} ${currentInstructor.lastName}`;
        setInstructors(prevInstructors => 
          prevInstructors.map(instructor => {
            const instructorUserId = instructor.user?.userId || instructor.userId;
            if (instructorUserId === currentInstructor.userId) {
              return {
                ...instructor,
                user: {
                  ...instructor.user,
                  name: updatedName,
                  email: currentInstructor.email,
                  isActive: currentInstructor.isActive,
                  userId: currentInstructor.userId
                }
              };
            }
            return instructor;
          })
        );
        
        setSuccessMessage("Instructor updated successfully!");
      } else {
        // Create new instructor user
        await api.post('users', {
          name: `${currentInstructor.firstName} ${currentInstructor.lastName}`,
          email: currentInstructor.email,
          password: currentInstructor.password,
          role: 'INSTRUCTOR',
          isActive: currentInstructor.isActive
        });
        
        setSuccessMessage("Instructor created successfully!");
      }
      
      handleCloseModal();
      
      // Refresh the instructor list to ensure consistency
      if (!isEditing) {
        // For new instructors, refresh the entire list
        setTimeout(() => {
          fetchInstructors();
        }, 100);
      }
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message || 'Unknown error';
      console.error(`Error saving instructor: ${errorMsg}`, {
        error: err,
        instructorData: currentInstructor,
        isEditing
      });
      setError(`Failed to save instructor: ${errorMsg}`);
    } finally {
      setSubmitLoading(false);
    }
  };

  // Handle instructor status toggle
  const handleToggleInstructorStatus = async (instructor, newStatus) => {
    try {
      const userId = instructor.user.userId;
      
      if (newStatus) {
        await adminService.activateUser(userId);
      } else {
        await adminService.deactivateUser(userId);
      }
      
      // Update the instructor in the local state immediately
      const targetUserId = instructor.user?.userId || instructor.userId;
      setInstructors(prevInstructors => 
        prevInstructors.map(inst => {
          const instUserId = inst.user?.userId || inst.userId;
          if (instUserId === targetUserId) {
            return {
              ...inst,
              user: {
                ...inst.user,
                isActive: newStatus
              },
              active: newStatus
            };
          }
          return inst;
        })
      );
      
      setSuccessMessage(`Instructor ${newStatus ? 'activated' : 'deactivated'} successfully!`);
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.message || err.message || 'Unknown error';
      const action = newStatus ? 'activate' : 'deactivate';
      console.error(`Error toggling instructor status: ${errorMsg}`, {
        error: err,
        instructorId: instructor.instructorId,
        newStatus,
        action
      });
      setError(`Failed to ${action} instructor: ${errorMsg}`);
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
    
    // Handle the active status - backend consistently uses 'isActive'
    const isActive = user.isActive ?? true;
    
    const instructorData = {
      instructorId: instructor.instructorId,
      userId: user.userId,
      firstName,
      lastName,
      email: user.email,
      departmentId: instructor.department?.departmentId || '',
      isActive
    };
    setCurrentInstructor(instructorData);
    setIsEditing(true);
    setShowModal(true);
  };



  // Open modal for adding new instructor
  const handleAddInstructor = () => {
    setCurrentInstructor({
      firstName: '',
      lastName: '',
      email: '',
      departmentId: '',
      password: 'instructor123', // Default password
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

  // Memoize instructor rows for performance
  const instructorRows = useMemo(() => {
    return instructors.map(instructor => {
      const user = instructor.user || instructor;
      const department = instructor.department || { name: 'Not Assigned' };
      const isActive = user.isActive ?? true;
      
      return (
        <React.Fragment key={user.userId}>
          <tr>
            <td>
              <div className="d-flex align-items-center">
                <Button
                  variant="link"
                  size="sm"
                  className="p-0 me-2"
                  onClick={() => toggleInstructor(instructor.instructorId)}
                >
                  {expandedInstructors.has(instructor.instructorId) ? 
                    <FaChevronDown /> : <FaChevronRight />
                  }
                </Button>
                <div className="fw-bold">
                  {user.name || `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Unknown'}
                </div>
              </div>
            </td>
            <td>{user.email}</td>
            <td>{department.name}</td>
            <td>
              <Form.Check
                type="switch"
                id={`instructor-status-${user.userId}`}
                checked={isActive}
                onChange={(e) => handleToggleInstructorStatus(instructor, e.target.checked)}
                label={isActive ? 'Active' : 'Inactive'}
              />
            </td>
            <td>
              <div className="btn-group">
                <Button
                  variant="outline-success"
                  size="sm"
                  onClick={() => handleAssignCourse(instructor)}
                  title="Assign Course"
                >
                  <FaBook />
                </Button>
                <Button 
                  variant="outline-primary" 
                  size="sm"
                  onClick={() => handleEditInstructor(instructor)}
                >
                  <FaEdit />
                </Button>
              </div>
            </td>
          </tr>
          {expandedInstructors.has(instructor.instructorId) && (
            <tr>
              <td colSpan="5" className="bg-light">
                <div className="p-3">
                  <div className="d-flex justify-content-between align-items-center mb-3">
                    <h6 className="mb-0">
                      <FaBook className="me-2" />
                      Assigned Courses ({instructorCourses[instructor.instructorId]?.length || 0})
                    </h6>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleAssignCourse(instructor)}
                    >
                      <FaPlus className="me-1" />
                      Assign Course
                    </Button>
                  </div>
                  {instructorCourses[instructor.instructorId]?.length > 0 ? (
                    <Row>
                      {instructorCourses[instructor.instructorId].map(courseInstructor => (
                        <Col md={4} key={courseInstructor.course.id} className="mb-2">
                          <Card size="sm">
                            <Card.Body className="p-2">
                              <div className="d-flex justify-content-between align-items-start">
                                <div>
                                  <strong>{courseInstructor.course.code}</strong>
                                  <div className="small">{courseInstructor.course.title}</div>
                                  <div className="small text-muted">{courseInstructor.role}</div>
                                </div>
                                <Button
                                  variant="outline-danger"
                                  size="sm"
                                  onClick={() => removeCourseFromInstructor(instructor.instructorId, courseInstructor.course.id)}
                                >
                                  <FaTimes />
                                </Button>
                              </div>
                            </Card.Body>
                          </Card>
                        </Col>
                      ))}
                    </Row>
                  ) : (
                    <div className="text-center py-3">
                      <p className="text-muted mb-2">No courses assigned</p>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => handleAssignCourse(instructor)}
                      >
                        <FaPlus className="me-1" />
                        Assign First Course
                      </Button>
                    </div>
                  )}
                </div>
              </td>
            </tr>
          )}
        </React.Fragment>
      );
    });
  }, [instructors]);

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
                instructorRows
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
                label={`Status: ${currentInstructor.isActive ? 'Active' : 'Inactive'}`}
                name="isActive"
                checked={currentInstructor.isActive}
                onChange={handleInputChange}
              />
              <Form.Text className="text-muted">
                {currentInstructor.isActive ? 
                  'Instructor can access the system and teach courses' : 
                  'Instructor cannot access the system'
                }
              </Form.Text>
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

      {/* Course Assignment Modal */}
      <Modal show={showCourseModal} onHide={() => setShowCourseModal(false)}>
        <Modal.Header closeButton>
          <Modal.Title>Assign Course to {selectedInstructor?.user?.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="mb-3">
            <strong>Available Courses:</strong>
          </div>
          {courses.filter(course => {
            const assignedCourses = instructorCourses[selectedInstructor?.instructorId] || [];
            return !assignedCourses.some(ic => ic.course.id === course.id);
          }).map(course => (
            <Card key={course.id} className="mb-2">
              <Card.Body className="p-2">
                <div className="d-flex justify-content-between align-items-center">
                  <div>
                    <strong>{course.code} - {course.title}</strong>
                    <div className="small text-muted">{course.description}</div>
                  </div>
                  <Button
                    variant="outline-primary"
                    size="sm"
                    onClick={() => assignCourseToInstructor(course.id)}
                  >
                    Assign
                  </Button>
                </div>
              </Card.Body>
            </Card>
          ))}
          {courses.filter(course => {
            const assignedCourses = instructorCourses[selectedInstructor?.instructorId] || [];
            return !assignedCourses.some(ic => ic.course.id === course.id);
          }).length === 0 && (
            <div className="text-center py-3">
              <p className="text-muted">All available courses are already assigned to this instructor.</p>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowCourseModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default ManageInstructors;