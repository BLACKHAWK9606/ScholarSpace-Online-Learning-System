import React, { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Badge, Nav, Tab, Alert, Spinner } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import { FaBook, FaInfoCircle, FaClipboardList, FaUsers } from 'react-icons/fa';
import FileManagement from './FileManagement';
import axios from 'axios';

const CourseDetails = () => {
  const { courseId } = useParams();
  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('overview');

  const fetchCourseDetails = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(`http://localhost:9090/courses/${courseId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const courseData = response.data;
      if (courseData) {
        setCourse(courseData);
      } else {
        setError('Course not found');
      }
    } catch (error) {
      console.error('Error fetching course details:', error);
      setError(error.response?.data?.message || 'Failed to load course details');
    } finally {
      setLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    fetchCourseDetails();
  }, [fetchCourseDetails]);

  // Get file type based on active tab
  const getFileTypeForTab = (tab) => {
    switch (tab) {
      case 'materials':
        return ['DOCUMENT', 'PDF', 'PRESENTATION'];
      case 'assignments':
        return ['ASSIGNMENT'];
      case 'resources':
        return ['RESOURCE', 'LINK'];
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <div className="text-center">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <p className="mt-3">Loading course details...</p>
        </div>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="mt-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Course</Alert.Heading>
          <p>{error}</p>
        </Alert>
      </Container>
    );
  }

  if (!course) {
    return (
      <Container className="mt-5">
        <Alert variant="warning">
          <Alert.Heading>Course Not Found</Alert.Heading>
          <p>The requested course could not be found.</p>
        </Alert>
      </Container>
    );
  }

  return (
    <Container fluid className="py-4">
      <Card className="shadow-sm mb-4">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h2 className="mb-1">{course.title}</h2>
              <div className="d-flex align-items-center">
                <Badge bg="primary" className="me-2">
                  {course.code}
                </Badge>
                <Badge bg={course.active ? 'success' : 'secondary'}>
                  {course.active ? 'Active' : 'Inactive'}
                </Badge>
              </div>
            </div>
          </div>

          <Tab.Container id="course-tabs" activeKey={activeTab} onSelect={(k) => setActiveTab(k)}>
            <Nav variant="tabs" className="mb-3">
              <Nav.Item>
                <Nav.Link eventKey="overview">
                  <FaInfoCircle className="me-1" />
                  Overview
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="materials">
                  <FaBook className="me-1" />
                  Materials
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="assignments">
                  <FaClipboardList className="me-1" />
                  Assignments
                </Nav.Link>
              </Nav.Item>
              <Nav.Item>
                <Nav.Link eventKey="participants">
                  <FaUsers className="me-1" />
                  Participants
                </Nav.Link>
              </Nav.Item>
            </Nav>
            
            <Tab.Content>
              <Tab.Pane eventKey="overview">
                <Card>
                  <Card.Body>
                    <h4>Course Description</h4>
                    <p>{course.description || 'No description available.'}</p>
                    
                    <Row className="mt-4">
                      <Col md={6}>
                        <h5>Course Details</h5>
                        <ul className="list-unstyled">
                          <li><strong>Credits:</strong> {course.credits}</li>
                          <li><strong>Department:</strong> {course.department?.name || 'N/A'}</li>
                          <li><strong>Semester:</strong> {course.semester || 'N/A'}</li>
                        </ul>
                      </Col>
                      <Col md={6}>
                        <h5>Course Schedule</h5>
                        <p>Schedule information will be displayed here.</p>
                      </Col>
                    </Row>
                  </Card.Body>
                </Card>
              </Tab.Pane>
              
              <Tab.Pane eventKey="materials">
                <FileManagement 
                  courseId={courseId} 
                  allowedFileTypes={getFileTypeForTab('materials')} 
                />
              </Tab.Pane>
              
              <Tab.Pane eventKey="assignments">
                <FileManagement 
                  courseId={courseId} 
                  allowedFileTypes={getFileTypeForTab('assignments')} 
                />
              </Tab.Pane>
              
              <Tab.Pane eventKey="participants">
                <Card>
                  <Card.Body>
                    <h4>Course Participants</h4>
                    <p>Participant information will be displayed here.</p>
                  </Card.Body>
                </Card>
              </Tab.Pane>
            </Tab.Content>
          </Tab.Container>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default CourseDetails;
