import React, { useState, useEffect } from 'react';
import { 
  Container, Row, Col, Card, Button, Spinner, Alert, 
  Badge, Table, Nav, Dropdown, ProgressBar
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { 
  FaBook, FaClipboardList, FaFileUpload, FaUser,
  FaGraduationCap, FaCalendarAlt, FaBell, FaEllipsisV
} from 'react-icons/fa';
import { instructorService } from '../../services/api';
import './InstructorDashboard.css'; // We'll create this file for custom styling

function InstructorDashboard() {
  // State to store instructor's courses and assignments
  const [courses, setCourses] = useState([]);
  const [pendingAssignments, setPendingAssignments] = useState([]);
  const [upcomingDeadlines, setUpcomingDeadlines] = useState([]);
  const [recentSubmissions, setRecentSubmissions] = useState([]);
  const [instructorProfile, setInstructorProfile] = useState({});

  // Loading and error states
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch instructor data when component mounts
  useEffect(() => {
    const fetchInstructorData = async () => {
      try {
        // Fetch courses assigned to the instructor
        const coursesResponse = await instructorService.getInstructorCourses();
        
        // Fetch pending assignments
        const assignmentsResponse = await instructorService.getPendingAssignments();
        
        // Update state with fetched data
        setCourses(coursesResponse.data || []);
        setPendingAssignments(assignmentsResponse.data || []);
        
        // Mock data for new UI elements
        setUpcomingDeadlines([
          { id: 1, title: 'Data Structures - Assignment 3', dueDate: '2023-04-20', course: 'Data Structures', submissionsReceived: 15, totalStudents: 30 },
          { id: 2, title: 'Database Systems - Final Project', dueDate: '2023-05-15', course: 'Database Systems', submissionsReceived: 8, totalStudents: 25 },
          { id: 3, title: 'Programming Fundamentals - Quiz 2', dueDate: '2023-04-25', course: 'Programming Fundamentals', submissionsReceived: 20, totalStudents: 35 }
        ]);
        
        setRecentSubmissions([
          { id: 1, studentName: 'John Doe', assignment: 'Assignment 2', course: 'Data Structures', submittedOn: '2023-04-08', status: 'pending' },
          { id: 2, studentName: 'Jane Smith', assignment: 'Final Project', course: 'Database Systems', submittedOn: '2023-04-07', status: 'pending' },
          { id: 3, studentName: 'Robert Johnson', assignment: 'Quiz 1', course: 'Programming Fundamentals', submittedOn: '2023-04-06', status: 'graded' }
        ]);
        
        setInstructorProfile({
          name: 'Dr. Sarah Wilson',
          department: 'Computer Science',
          courses: coursesResponse.data ? coursesResponse.data.length : 0,
          students: 90,
          avgRating: 4.8
        });
        
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching instructor data:", err);
        setError('Failed to load instructor dashboard');
        setIsLoading(false);
      }
    };

    fetchInstructorData();
  }, []);

  // Show loading spinner while fetching data
  if (isLoading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <div className="text-center">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <p className="mt-3">Loading dashboard data...</p>
        </div>
      </Container>
    );
  }

  // Show error message if data fetching failed
  if (error) {
    return (
      <Container className="mt-5">
        <Alert variant="danger">
          <Alert.Heading>Error Loading Dashboard</Alert.Heading>
          <p>{error}</p>
          <Button onClick={() => window.location.reload()} variant="outline-danger">Retry</Button>
        </Alert>
      </Container>
    );
  }

  return (
    <div className="instructor-dashboard">
      {/* Top navbar with notifications */}
      <div className="dashboard-header bg-white py-3 px-4 shadow-sm mb-4">
        <Row className="align-items-center">
          <Col>
            <h1 className="mb-0">Instructor Dashboard</h1>
            <p className="text-muted mb-0">Welcome back, {instructorProfile.name}</p>
          </Col>
          <Col xs="auto">
            <Dropdown align="end">
              <Dropdown.Toggle variant="light" id="notification-dropdown" className="position-relative">
                <FaBell />
                <Badge pill bg="danger" className="position-absolute top-0 start-100 translate-middle">
                  {pendingAssignments.length}
                </Badge>
              </Dropdown.Toggle>
              <Dropdown.Menu className="shadow-sm notification-menu">
                <Dropdown.Header>Notifications</Dropdown.Header>
                {recentSubmissions.map((submission, index) => (
                  <Dropdown.Item key={index} className="notification-item">
                    <div className="d-flex">
                      <div className={`notification-icon ${submission.status === 'pending' ? 'bg-warning' : 'bg-success'}`}>
                        <FaClipboardList />
                      </div>
                      <div className="ms-3">
                        <p className="mb-0 fw-bold">{submission.status === 'pending' ? 'New Submission' : 'Graded Assignment'}</p>
                        <p className="mb-0 small">
                          {submission.studentName} submitted {submission.assignment}
                        </p>
                        <small className="text-muted">{submission.submittedOn}</small>
                      </div>
                    </div>
                  </Dropdown.Item>
                ))}
                <Dropdown.Divider />
                <Dropdown.Item className="text-center text-primary">View All Notifications</Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          </Col>
        </Row>
      </div>

      <Container fluid className="px-4">
        {/* Main dashboard navigation */}
        <Nav variant="tabs" className="mb-4 dashboard-tabs">
          <Nav.Item>
            <Nav.Link 
              active={activeTab === 'overview'} 
              onClick={() => setActiveTab('overview')}
            >
              Overview
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link 
              active={activeTab === 'courses'} 
              onClick={() => setActiveTab('courses')}
            >
              My Courses
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link 
              active={activeTab === 'assignments'} 
              onClick={() => setActiveTab('assignments')}
            >
              Assignments
              <Badge bg="danger" className="ms-2">{pendingAssignments.length}</Badge>
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link 
              active={activeTab === 'materials'} 
              onClick={() => setActiveTab('materials')}
            >
              Course Materials
            </Nav.Link>
          </Nav.Item>
        </Nav>

        {/* Instructor Profile and Stats */}
        <Row className="mb-4">
          <Col lg={4} md={12}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Body className="d-flex flex-column">
                <div className="text-center mb-4">
                  <div className="avatar-placeholder mb-3">
                    <FaUser size={32} />
                  </div>
                  <h5 className="mb-1">{instructorProfile.name}</h5>
                  <p className="text-muted mb-0">{instructorProfile.department}</p>
                </div>
                
                <div className="instructor-stats">
                  <div className="stat-item">
                    <h6 className="stat-label">Courses</h6>
                    <p className="stat-value">{instructorProfile.courses}</p>
                  </div>
                  <div className="stat-item">
                    <h6 className="stat-label">Students</h6>
                    <p className="stat-value">{instructorProfile.students}</p>
                  </div>
                  <div className="stat-item">
                    <h6 className="stat-label">Rating</h6>
                    <p className="stat-value">{instructorProfile.avgRating}/5</p>
                  </div>
                </div>
              </Card.Body>
            </Card>
          </Col>
          
          <Col lg={8} md={12}>
            <Row className="g-3 h-100">
              <Col sm={6}>
                <Card className="border-0 shadow-sm h-100">
                  <Card.Body>
                    <div className="d-flex align-items-center mb-3">
                      <div className="stats-icon bg-primary bg-opacity-10 text-primary">
                        <FaBook />
                      </div>
                      <div className="ms-3">
                        <h6 className="card-subtitle text-muted mb-1">My Courses</h6>
                        <h3 className="card-title mb-0">{courses.length}</h3>
                      </div>
                    </div>
                    <div className="mt-3">
                      <Button 
                        variant="outline-primary" 
                        size="sm"
                        as={Link}
                        to="/instructor/courses"
                        className="w-100"
                      >
                        View All Courses
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
              
              <Col sm={6}>
                <Card className="border-0 shadow-sm h-100">
                  <Card.Body>
                    <div className="d-flex align-items-center mb-3">
                      <div className="stats-icon bg-danger bg-opacity-10 text-danger">
                        <FaClipboardList />
                      </div>
                      <div className="ms-3">
                        <h6 className="card-subtitle text-muted mb-1">Pending Reviews</h6>
                        <h3 className="card-title mb-0">{pendingAssignments.length}</h3>
                      </div>
                    </div>
                    <div className="mt-3">
                      <Button 
                        variant="outline-danger" 
                        size="sm"
                        as={Link}
                        to="/instructor/assignments"
                        className="w-100"
                      >
                        Review Assignments
                      </Button>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          </Col>
        </Row>

        {/* Courses Section */}
        <Row className="mb-4">
          <Col>
            <Card className="border-0 shadow-sm">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">My Courses</h5>
                <Link to="/instructor/courses" className="text-decoration-none">
                  View All
                </Link>
              </Card.Header>
              <Card.Body className="pt-0">
                {courses.length === 0 ? (
                  <Alert variant="info">No courses assigned</Alert>
                ) : (
                  <div className="course-grid">
                    {courses.slice(0, 3).map(course => (
                      <Card key={course.courseId} className="course-card border-0 shadow-sm">
                        <Card.Body>
                          <h5 className="course-title">{course.title}</h5>
                          <p className="course-code text-muted">{course.courseCode || 'CS' + course.courseId}</p>
                          <div className="d-flex justify-content-between align-items-center mb-3">
                            <Badge bg="light" text="dark" className="px-3 py-2">
                              <FaGraduationCap className="me-1" />
                              {course.studentsCount || '25'} Students
                            </Badge>
                            <Badge bg="light" text="dark" className="px-3 py-2">
                              <FaCalendarAlt className="me-1" />
                              {course.semester || 'Spring 2023'}
                            </Badge>
                          </div>
                          <Link 
                            to={`/instructor/course/${course.courseId}`} 
                            className="btn btn-primary w-100"
                          >
                            Manage Course
                          </Link>
                        </Card.Body>
                      </Card>
                    ))}
                  </div>
                )}
              </Card.Body>
            </Card>
          </Col>
        </Row>

        <Row className="g-4">
          {/* Upcoming Deadlines */}
          <Col lg={6}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Upcoming Deadlines</h5>
                <Dropdown align="end">
                  <Dropdown.Toggle variant="light" size="sm" className="btn-icon">
                    <FaEllipsisV />
                  </Dropdown.Toggle>
                  <Dropdown.Menu>
                    <Dropdown.Item>View All Deadlines</Dropdown.Item>
                    <Dropdown.Item>Add New Deadline</Dropdown.Item>
                    <Dropdown.Item>Export Schedule</Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              </Card.Header>
              <Card.Body className="pt-0">
                <Table responsive hover className="align-middle" style={{minWidth: "500px"}}>
                  <thead>
                    <tr>
                      <th>Assignment</th>
                      <th>Due Date</th>
                      <th>Progress</th>
                    </tr>
                  </thead>
                  <tbody>
                    {upcomingDeadlines.map((deadline) => (
                      <tr key={deadline.id}>
                        <td>
                          <div>
                            <h6 className="mb-0">{deadline.title}</h6>
                            <small className="text-muted">{deadline.course}</small>
                          </div>
                        </td>
                        <td>{deadline.dueDate}</td>
                        <td>
                          <div>
                            <div className="d-flex justify-content-between align-items-center">
                              <small className="text-muted">
                                {deadline.submissionsReceived}/{deadline.totalStudents} submitted
                              </small>
                              <small className="text-muted">
                                {Math.round((deadline.submissionsReceived / deadline.totalStudents) * 100)}%
                              </small>
                            </div>
                            <ProgressBar 
                              now={(deadline.submissionsReceived / deadline.totalStudents) * 100} 
                              variant="success" 
                              className="progress-sm mt-1"
                            />
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>

          {/* Recent Submissions */}
          <Col lg={6}>
            <Card className="border-0 shadow-sm h-100">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Recent Submissions</h5>
                <Link to="/instructor/submissions" className="text-decoration-none">
                  View All
                </Link>
              </Card.Header>
              <Card.Body className="pt-0">
                <Table responsive hover className="align-middle">
                  <thead>
                    <tr>
                      <th>Student</th>
                      <th>Assignment</th>
                      <th>Submitted</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentSubmissions.map((submission) => (
                      <tr key={submission.id}>
                        <td>{submission.studentName}</td>
                        <td>
                          <div>
                            <span className="fw-medium">{submission.assignment}</span>
                            <div className="small text-muted">{submission.course}</div>
                          </div>
                        </td>
                        <td>{submission.submittedOn}</td>
                        <td>
                          <Badge 
                            bg={submission.status === 'pending' ? 'warning' : 'success'}
                            text={submission.status === 'pending' ? 'dark' : 'white'}
                          >
                            {submission.status === 'pending' ? 'Pending' : 'Graded'}
                          </Badge>
                        </td>
                        <td>
                          <Button 
                            variant={submission.status === 'pending' ? 'outline-primary' : 'outline-secondary'} 
                            size="sm"
                          >
                            {submission.status === 'pending' ? 'Grade' : 'Review'}
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Quick Actions */}
        <Row className="mt-4">
          <Col>
            <Card className="border-0 shadow-sm">
              <Card.Header className="bg-white border-0 pt-4">
                <h5 className="card-title mb-0">Quick Actions</h5>
              </Card.Header>
              <Card.Body className="pt-3">
                <div className="quick-actions">
                  <Link to="/instructor/assignments/new" className="quick-action-btn">
                    <div className="action-icon bg-primary text-white">
                      <FaClipboardList />
                    </div>
                    <span>Create Assignment</span>
                  </Link>
                  
                  <Link to="/instructor/materials/upload" className="quick-action-btn">
                    <div className="action-icon bg-success text-white">
                      <FaFileUpload />
                    </div>
                    <span>Upload Course Material</span>
                  </Link>
                  
                  <Link to="/instructor/announcements/new" className="quick-action-btn">
                    <div className="action-icon bg-info text-white">
                      <FaBell />
                    </div>
                    <span>Create Announcement</span>
                  </Link>
                  
                  <Link to="/instructor/gradebook" className="quick-action-btn">
                    <div className="action-icon bg-warning text-white">
                      <FaBook />
                    </div>
                    <span>Manage Gradebook</span>
                  </Link>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default InstructorDashboard;