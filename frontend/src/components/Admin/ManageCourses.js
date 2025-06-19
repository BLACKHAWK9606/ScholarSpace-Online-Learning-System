import React, { useState, useEffect } from 'react';
import { 
  Container, Row, Col, Card, Button, Form, Table, 
  Modal, Spinner, Alert, Badge
} from 'react-bootstrap';
import { FaEdit, FaTrash, FaPlus, FaCheck, FaTimes } from 'react-icons/fa';
import { adminService } from '../../services/api';

function ManageCourses() {
  // State for courses and UI
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [currentCourse, setCurrentCourse] = useState({
    courseId: null,
    courseCode: '',
    courseName: '',
    description: '',
    creditHours: 3,
    active: true,
    department: ''
  });
  const [isEditing, setIsEditing] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState('');

  // Fetch all courses and departments when component mounts
  useEffect(() => {
    fetchCourses();
    fetchDepartments();
  }, []);

  // Function to fetch all courses
  const fetchCourses = async () => {
    setLoading(true);
    try {
      const response = await adminService.getAllCourses();
      console.log('Fetched courses:', response);
      
      // Ensure we're working with an array
      const coursesData = response && response.data ? response.data : [];
      setCourses(coursesData);
      setError(null);
    } catch (err) {
      console.error("Error fetching courses:", err);
      setError("Failed to load courses. Please try again later.");
      setCourses([]); // Set to empty array on error
    } finally {
      setLoading(false);
    }
  };

  // Function to fetch all departments
  const fetchDepartments = async () => {
    try {
      const response = await adminService.getAllDepartments();
      console.log('Fetched departments:', response);
      
      // Ensure we're working with an array
      const departmentsData = response && response.data ? response.data : [];
      setDepartments(departmentsData);
    } catch (err) {
      console.error("Error fetching departments:", err);
      // Don't set error state here to avoid blocking the course display
      setDepartments([]); // Set to empty array on error
    }
  };

  // Handle form input changes
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setCurrentCourse({
      ...currentCourse,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  // Validate form
  const validateForm = () => {
    const errors = {};
    if (!currentCourse.courseCode.trim()) errors.courseCode = "Course code is required";
    if (!currentCourse.courseName.trim()) errors.courseName = "Course name is required";
    if (!currentCourse.description.trim()) errors.description = "Description is required";
    if (!currentCourse.department) errors.department = "Department is required";
    if (!currentCourse.creditHours || currentCourse.creditHours < 1) errors.creditHours = "Credit hours must be at least 1";
    
    console.log('Form validation errors:', errors);
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    setLoading(true);
    try {
      // Format course data to match backend expectations
      const courseData = {
        code: currentCourse.courseCode,
        title: currentCourse.courseName,
        description: currentCourse.description || '',
        creditHours: parseInt(currentCourse.creditHours, 10),
        departmentId: parseInt(currentCourse.department, 10),
        isActive: currentCourse.active
      };

      console.log('Submitting course data:', courseData);
      
      if (isEditing) {
        await adminService.updateCourse(currentCourse.courseId, courseData);
        setSuccessMessage("Course updated successfully!");
      } else {
        await adminService.createCourse(courseData);
        setSuccessMessage("Course created successfully!");
      }
      
      // Update the courses list
      fetchCourses();
      
      // Close the modal and reset form
      handleCloseModal();
    } catch (err) {
      console.error("Error saving course:", err);
      setError("Failed to save course. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Open modal for creating a new course
  const handleAddCourse = () => {
    console.log('Available departments:', departments);
    
    // Find a valid department ID from the departments array
    let defaultDepartmentId = '';
    if (departments.length > 0) {
      const firstDept = departments[0];
      defaultDepartmentId = firstDept.departmentId || firstDept.id || '';
    }
    
    setCurrentCourse({
      courseId: null,
      courseCode: '',
      courseName: '',
      description: '',
      creditHours: 3,
      active: true,
      department: defaultDepartmentId
    });
    
    setIsEditing(false);
    setFormErrors({});
    setShowModal(true);
  };

  // Open modal for editing an existing course
  const handleEditCourse = (course) => {
    console.log('Editing course:', course);
    
    // Make sure we have all the required fields with proper naming
    setCurrentCourse({
      courseId: course.courseId || course.id,
      courseCode: course.courseCode || course.code,
      courseName: course.courseName || course.title,
      description: course.description || '',
      creditHours: course.creditHours,
      active: course.isActive !== undefined ? course.isActive : (course.active !== undefined ? course.active : true),
      department: course.department ? 
        (course.department.departmentId ? course.department.departmentId : 
         (course.department.id ? course.department.id : course.department)) : 
        (course.departmentId ? course.departmentId : '')
    });
    
    setIsEditing(true);
    setFormErrors({});
    setShowModal(true);
  };

  // Close the modal
  const handleCloseModal = () => {
    setShowModal(false);
  };

  // Toggle course active status
  const toggleCourseStatus = async (courseId, currentStatus) => {
    try {
      if (currentStatus) {
        await adminService.deactivateCourse(courseId);
      } else {
        await adminService.activateCourse(courseId);
      }
      // Refresh the courses list
      fetchCourses();
      setSuccessMessage(`Course ${currentStatus ? 'deactivated' : 'activated'} successfully!`);
    } catch (err) {
      console.error("Error toggling course status:", err);
      setError("Failed to update course status. Please try again.");
    }
  };

  // Delete a course
  const handleDeleteCourse = async (courseId) => {
    if (window.confirm("Are you sure you want to delete this course? This action cannot be undone.")) {
      try {
        await adminService.deleteCourse(courseId);
        // Refresh the courses list
        fetchCourses();
        setSuccessMessage("Course deleted successfully!");
      } catch (err) {
        console.error("Error deleting course:", err);
        setError("Failed to delete course. Please try again.");
      }
    }
  };

  // Render loading spinner
  if (loading && courses.length === 0) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <Spinner animation="border" variant="primary" />
      </Container>
    );
  }

  return (
    <Container fluid className="p-4">
      <Card className="shadow-sm">
        <Card.Header className="bg-white">
          <div className="d-flex justify-content-between align-items-center">
            <h4 className="mb-0">Manage Courses</h4>
            <Button variant="primary" onClick={handleAddCourse}>
              <FaPlus className="me-2" /> Add New Course
            </Button>
          </div>
        </Card.Header>
        <Card.Body>
          {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}
          {successMessage && <Alert variant="success" dismissible onClose={() => setSuccessMessage(null)}>{successMessage}</Alert>}
          
          <Table responsive hover className="align-middle">
            <thead className="bg-light">
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Department</th>
                <th>Credit Hours</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {courses.length === 0 ? (
                <tr>
                  <td colSpan="6" className="text-center py-4">
                    No courses found. Click "Add New Course" to create one.
                  </td>
                </tr>
              ) : (
                courses.map(course => (
                  <tr key={course.courseId || course.id}>
                    <td>{course.courseCode || course.code}</td>
                    <td>{course.courseName || course.title}</td>
                    <td>{course.department ? (course.department.name || 'Unknown') : 'Not assigned'}</td>
                    <td>{course.creditHours}</td>
                    <td>
                      <Badge bg={course.active || course.isActive ? "success" : "secondary"}>
                        {course.active || course.isActive ? "Active" : "Inactive"}
                      </Badge>
                    </td>
                    <td>
                      <div className="d-flex">
                        <Button 
                          variant="outline-primary" 
                          size="sm" 
                          className="me-2" 
                          onClick={() => handleEditCourse(course)}
                        >
                          <FaEdit />
                        </Button>
                        <Button 
                          variant="outline-success" 
                          size="sm" 
                          className="me-2" 
                          onClick={() => toggleCourseStatus(course.courseId || course.id, course.active || course.isActive)}
                        >
                          {course.active || course.isActive ? <FaTimes /> : <FaCheck />}
                        </Button>
                        <Button 
                          variant="outline-danger" 
                          size="sm" 
                          onClick={() => handleDeleteCourse(course.courseId || course.id)}
                        >
                          <FaTrash />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {/* Add/Edit Course Modal */}
      <Modal show={showModal} onHide={handleCloseModal} backdrop="static" size="lg">
        <Modal.Header closeButton>
          <Modal.Title>{isEditing ? 'Edit Course' : 'Add New Course'}</Modal.Title>
        </Modal.Header>
        <Form onSubmit={handleSubmit}>
          <Modal.Body>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Course Code</Form.Label>
                  <Form.Control 
                    type="text" 
                    name="courseCode" 
                    value={currentCourse.courseCode} 
                    onChange={handleInputChange}
                    isInvalid={!!formErrors.courseCode}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.courseCode}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Course Name</Form.Label>
                  <Form.Control 
                    type="text" 
                    name="courseName" 
                    value={currentCourse.courseName} 
                    onChange={handleInputChange}
                    isInvalid={!!formErrors.courseName}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.courseName}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
            
            <Form.Group className="mb-3">
              <Form.Label>Description</Form.Label>
              <Form.Control 
                as="textarea" 
                rows={3} 
                name="description" 
                value={currentCourse.description} 
                onChange={handleInputChange}
                isInvalid={!!formErrors.description}
              />
              <Form.Control.Feedback type="invalid">
                {formErrors.description}
              </Form.Control.Feedback>
            </Form.Group>
            
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Credit Hours</Form.Label>
                  <Form.Control 
                    type="number" 
                    name="creditHours" 
                    value={currentCourse.creditHours} 
                    onChange={handleInputChange}
                    min={1}
                    max={6}
                    isInvalid={!!formErrors.creditHours}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.creditHours}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Department</Form.Label>
                  <Form.Select 
                    name="department" 
                    value={currentCourse.department} 
                    onChange={handleInputChange}
                    isInvalid={!!formErrors.department}
                  >
                    <option value="">Select Department</option>
                    {departments.map(dept => (
                      <option key={dept.departmentId} value={dept.departmentId}>
                        {dept.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {formErrors.department}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
            
            <Form.Group className="mb-3">
              <Form.Check 
                type="switch"
                id="active-switch"
                label="Active"
                name="active"
                checked={currentCourse.active}
                onChange={handleInputChange}
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={handleCloseModal}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                    className="me-2"
                  />
                  Saving...
                </>
              ) : (
                'Save Course'
              )}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </Container>
  );
}

export default ManageCourses;