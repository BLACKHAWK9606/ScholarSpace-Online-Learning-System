import React, { useState, useEffect } from 'react';
import { 
  Container, Row, Col, Card, Button, Spinner, Alert, 
  Badge, Nav, Dropdown, ProgressBar, Table
} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { 
  FaBook, FaClipboardList, FaUserGraduate, FaCalendarAlt, 
  FaBell, FaClock, FaCheckCircle, FaTrophy, FaEllipsisV
} from 'react-icons/fa';
import { studentService } from '../../services/api';
import './StudentDashboard.css'; // We'll create this file for custom styling

function StudentDashboard() {
  // State to store student's registered and available courses
  const [registeredCourses, setRegisteredCourses] = useState([]);
  const [availableCourses, setAvailableCourses] = useState([]);
  const [upcomingAssignments, setUpcomingAssignments] = useState([]);
  const [recentGrades, setRecentGrades] = useState([]);
  const [courseProgress, setCourseProgress] = useState([]);
  const [notifications, setNotifications] = useState([]);

  // Loading and error states
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  // Fetch student data when component mounts
  useEffect(() => {
    const fetchStudentData = async () => {
      try {
        // Fetch registered courses
        const registeredResponse = await studentService.getRegisteredCourses();
        
        // Fetch available courses
        const availableResponse = await studentService.getAvailableCourses();
        
        // Update state with fetched data
        setRegisteredCourses(registeredResponse.data || []);
        setAvailableCourses(availableResponse.data || []);
        
        // Mock data for new UI elements
        setUpcomingAssignments([
          { id: 1, title: 'Programming Assignment 3', course: 'Introduction to Python', dueDate: '2023-04-15', daysLeft: 5 },
          { id: 2, title: 'Data Analysis Project', course: 'Database Systems', dueDate: '2023-04-20', daysLeft: 10 },
          { id: 3, title: 'Quiz 2', course: 'Web Development', dueDate: '2023-04-18', daysLeft: 8 }
        ]);
        
        setRecentGrades([
          { id: 1, assignment: 'Programming Assignment 2', course: 'Introduction to Python', grade: 92, maxGrade: 100, feedback: 'Great work!' },
          { id: 2, title: 'Quiz 1', course: 'Web Development', grade: 85, maxGrade: 100, feedback: 'Good effort.' }
        ]);
        
        setCourseProgress([
          { id: 1, course: 'Introduction to Python', progress: 70, modules: 10, completed: 7 },
          { id: 2, course: 'Database Systems', progress: 45, modules: 8, completed: 3.5 },
          { id: 3, course: 'Web Development', progress: 60, modules: 12, completed: 7.2 }
        ]);
        
        setNotifications([
          { id: 1, type: 'assignment', message: 'New assignment posted: Programming Assignment 3', time: '2 hours ago' },
          { id: 2, type: 'grade', message: 'You received a grade: 92/100 on Programming Assignment 2', time: '1 day ago' },
          { id: 3, type: 'course', message: 'New material available in Web Development', time: '2 days ago' }
        ]);
        
        setIsLoading(false);
      } catch (err) {
        console.error("Error fetching student data:", err);
        setError('Failed to load student dashboard');
        setIsLoading(false);
      }
    };

    fetchStudentData();
  }, []); // Empty dependency array means this runs once on component mount

  // Show loading spinner while fetching data
  if (isLoading) {
    return (
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
        <div className="text-center">
          <Spinner animation="border" variant="primary" style={{ width: '3rem', height: '3rem' }} />
          <p className="mt-3">Loading your courses...</p>
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
    <div className="student-dashboard">
      {/* Top navbar with notifications */}
      <div className="dashboard-header bg-white py-3 px-4 shadow-sm mb-4">
        <Row className="align-items-center">
          <Col>
            <h1 className="mb-0">Student Dashboard</h1>
            <p className="text-muted mb-0">Welcome back, Student</p>
          </Col>
          <Col xs="auto">
            <Dropdown align="end">
              <Dropdown.Toggle variant="light" id="notification-dropdown" className="position-relative">
                <FaBell />
                <Badge pill bg="danger" className="position-absolute top-0 start-100 translate-middle">
                  {notifications.length}
                </Badge>
              </Dropdown.Toggle>
              <Dropdown.Menu className="shadow-sm notification-menu">
                <Dropdown.Header>Notifications</Dropdown.Header>
                {notifications.map((notification, index) => (
                  <Dropdown.Item key={index} className="notification-item">
                    <div className="d-flex">
                      <div className={`notification-icon ${
                        notification.type === 'assignment' ? 'bg-primary' : 
                        notification.type === 'grade' ? 'bg-success' : 
                        'bg-info'
                      }`}>
                        {notification.type === 'assignment' ? <FaClipboardList /> : 
                         notification.type === 'grade' ? <FaTrophy /> : 
                         <FaBook />}
                      </div>
                      <div className="ms-3">
                        <p className="mb-0 small">{notification.message}</p>
                        <small className="text-muted">{notification.time}</small>
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
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link 
              active={activeTab === 'grades'} 
              onClick={() => setActiveTab('grades')}
            >
              Grades
            </Nav.Link>
          </Nav.Item>
        </Nav>

        {/* Summary Cards */}
        <Row className="g-4 mb-4">
          <Col xl={3} md={6}>
            <Card className="border-0 shadow-sm">
              <Card.Body className="d-flex align-items-center">
                <div className="stats-icon bg-primary bg-opacity-10 text-primary">
                  <FaBook />
                </div>
                <div className="ms-3">
                  <h6 className="card-subtitle text-muted mb-1">Enrolled Courses</h6>
                  <h3 className="card-title mb-0">{registeredCourses.length}</h3>
                </div>
              </Card.Body>
            </Card>
          </Col>
          <Col xl={3} md={6}>
            <Card className="border-0 shadow-sm">
              <Card.Body className="d-flex align-items-center">
                <div className="stats-icon bg-warning bg-opacity-10 text-warning">
                  <FaClipboardList />
                </div>
                <div className="ms-3">
                  <h6 className="card-subtitle text-muted mb-1">Pending Tasks</h6>
                  <h3 className="card-title mb-0">{upcomingAssignments.length}</h3>
                </div>
              </Card.Body>
            </Card>
          </Col>
          <Col xl={3} md={6}>
            <Card className="border-0 shadow-sm">
              <Card.Body className="d-flex align-items-center">
                <div className="stats-icon bg-success bg-opacity-10 text-success">
                  <FaCheckCircle />
                </div>
                <div className="ms-3">
                  <h6 className="card-subtitle text-muted mb-1">Completed Tasks</h6>
                  <h3 className="card-title mb-0">{recentGrades.length}</h3>
                </div>
              </Card.Body>
            </Card>
          </Col>
          <Col xl={3} md={6}>
            <Card className="border-0 shadow-sm">
              <Card.Body className="d-flex align-items-center">
                <div className="stats-icon bg-info bg-opacity-10 text-info">
                  <FaUserGraduate />
                </div>
                <div className="ms-3">
                  <h6 className="card-subtitle text-muted mb-1">Available Courses</h6>
                  <h3 className="card-title mb-0">{availableCourses.length}</h3>
                  <Link 
                    to="/student/courses/register" 
                    className="btn btn-sm btn-outline-primary mt-2"
                  >
                    Register Now
                  </Link>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>

        {/* Main Content */}
        <Row className="g-4">
          {/* Course Cards */}
          <Col lg={8}>
            <Card className="border-0 shadow-sm mb-4">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">My Courses</h5>
                <Link to="/student/courses" className="text-decoration-none">
                  View All
                </Link>
              </Card.Header>
              <Card.Body className="pt-0">
                {registeredCourses.length === 0 ? (
                  <Alert variant="info">No courses registered. Browse available courses to get started.</Alert>
                ) : (
                  <div className="course-grid">
                    {registeredCourses.map((course, index) => (
                      <Card key={course.courseId || index} className="course-card border-0 shadow-sm">
                        <Card.Body>
                          <div className="course-progress-indicator">
                            <div 
                              className="progress-ring" 
                              style={{
                                background: `conic-gradient(#0d6efd ${(courseProgress[index]?.progress || 0) * 3.6}deg, #e9ecef 0deg)`
                              }}
                            >
                              <div className="progress-center">
                                {courseProgress[index]?.progress || 0}%
                              </div>
                            </div>
                          </div>
                          <h5 className="course-title">{course.title}</h5>
                          <p className="course-instructor text-muted">
                            {course.instructor || 'Dr. Robert Smith'}
                          </p>
                          <div className="d-flex justify-content-between align-items-center mb-3">
                            <Badge bg="light" text="dark" className="px-2 py-1">
                              <FaBook className="me-1" /> 
                              {courseProgress[index]?.completed || 0}/{courseProgress[index]?.modules || 0} modules
                            </Badge>
                            <Badge bg="light" text="dark" className="px-2 py-1">
                              <FaCalendarAlt className="me-1" /> 
                              {course.schedule || 'Mon/Wed'}
                            </Badge>
                          </div>
                          <Link 
                            to={`/student/course/${course.courseId}`} 
                            className="btn btn-primary w-100"
                          >
                            Go to Course
                          </Link>
                        </Card.Body>
                      </Card>
                    ))}
                  </div>
                )}
              </Card.Body>
            </Card>
            
            {/* Recent Grades */}
            <Card className="border-0 shadow-sm">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Recent Grades</h5>
                <Link to="/student/grades" className="text-decoration-none">
                  View All
                </Link>
              </Card.Header>
              <Card.Body className="pt-0">
                {recentGrades.length === 0 ? (
                  <Alert variant="info">No grades yet. Complete and submit assignments to receive feedback.</Alert>
                ) : (
                  <Table responsive hover className="align-middle">
                    <thead>
                      <tr>
                        <th>Assignment</th>
                        <th>Course</th>
                        <th>Grade</th>
                        <th>Feedback</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentGrades.map((grade) => (
                        <tr key={grade.id}>
                          <td>{grade.assignment}</td>
                          <td>{grade.course}</td>
                          <td>
                            <div className="d-flex align-items-center">
                              <div className="me-3">
                                <h6 className="mb-0">{grade.grade}/{grade.maxGrade}</h6>
                                <div className="text-muted small">
                                  {Math.round((grade.grade / grade.maxGrade) * 100)}%
                                </div>
                              </div>
                              <ProgressBar 
                                now={(grade.grade / grade.maxGrade) * 100} 
                                variant={
                                  (grade.grade / grade.maxGrade) * 100 >= 90 ? 'success' :
                                  (grade.grade / grade.maxGrade) * 100 >= 70 ? 'info' :
                                  (grade.grade / grade.maxGrade) * 100 >= 60 ? 'warning' :
                                  'danger'
                                }
                                style={{ width: '100px', height: '8px' }}
                              />
                            </div>
                          </td>
                          <td>{grade.feedback}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                )}
              </Card.Body>
            </Card>
          </Col>
          
          {/* Sidebar Content */}
          <Col lg={4}>
            {/* Upcoming Assignments */}
            <Card className="border-0 shadow-sm mb-4">
              <Card.Header className="bg-white border-0 pt-4 d-flex justify-content-between align-items-center">
                <h5 className="card-title mb-0">Upcoming Assignments</h5>
                <Dropdown align="end">
                  <Dropdown.Toggle variant="light" size="sm" className="btn-icon">
                    <FaEllipsisV />
                  </Dropdown.Toggle>
                  <Dropdown.Menu>
                    <Dropdown.Item>View All Assignments</Dropdown.Item>
                    <Dropdown.Item>Add to Calendar</Dropdown.Item>
                    <Dropdown.Item>Download Schedule</Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              </Card.Header>
              <Card.Body className="pt-0">
                {upcomingAssignments.length === 0 ? (
                  <Alert variant="info">No upcoming assignments due.</Alert>
                ) : (
                  <div className="assignment-list">
                    {upcomingAssignments.map((assignment) => (
                      <div key={assignment.id} className="assignment-item">
                        <div className="d-flex justify-content-between">
                          <div>
                            <h6 className="mb-1">{assignment.title}</h6>
                            <div className="text-muted small">{assignment.course}</div>
                          </div>
                          <Badge 
                            bg={
                              assignment.daysLeft <= 3 ? 'danger' :
                              assignment.daysLeft <= 7 ? 'warning' :
                              'info'
                            }
                            className="align-self-start"
                          >
                            {assignment.daysLeft} days left
                          </Badge>
                        </div>
                        <div className="d-flex align-items-center mt-2">
                          <FaClock className="text-muted me-2" size={14} />
                          <span className="text-muted small">Due: {assignment.dueDate}</span>
                        </div>
                        <Link 
                          to={`/student/assignments/${assignment.id}`}
                          className="btn btn-sm btn-outline-primary mt-2"
                        >
                          View Details
                        </Link>
                      </div>
                    ))}
                  </div>
                )}
              </Card.Body>
            </Card>
            
            {/* Course Progress */}
            <Card className="border-0 shadow-sm">
              <Card.Header className="bg-white border-0 pt-4">
                <h5 className="card-title mb-0">Course Progress</h5>
              </Card.Header>
              <Card.Body className="pt-0">
                {courseProgress.length === 0 ? (
                  <Alert variant="info">No course progress to display.</Alert>
                ) : (
                  <div className="progress-list">
                    {courseProgress.map((course) => (
                      <div key={course.id} className="progress-item">
                        <div className="d-flex justify-content-between mb-1">
                          <h6 className="mb-0">{course.course}</h6>
                          <span className="text-muted small">{course.progress}%</span>
                        </div>
                        <ProgressBar 
                          now={course.progress} 
                          variant={
                            course.progress >= 75 ? 'success' :
                            course.progress >= 50 ? 'info' :
                            course.progress >= 25 ? 'warning' :
                            'danger'
                          }
                          className="mb-1"
                          style={{ height: '8px' }}
                        />
                        <div className="d-flex justify-content-between">
                          <span className="text-muted small">
                            {course.completed} of {course.modules} modules completed
                          </span>
                          <Link 
                            to={`/student/course/${course.id}`}
                            className="text-decoration-none small"
                          >
                            Continue
                          </Link>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
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
                  <Button 
                    variant="outline-primary" 
                    className="action-button"
                    as={Link}
                    to="/student/assignments"
                  >
                    <FaClipboardList className="action-icon" />
                    View Assignments
                  </Button>
                  
                  <Button 
                    variant="outline-success"
                    className="action-button"
                    as={Link}
                    to="/student/grades"
                  >
                    <FaTrophy className="action-icon" />
                    View Grades
                  </Button>
                  
                  <Button 
                    variant="outline-info"
                    className="action-button"
                    as={Link}
                    to="/student/courses/register"
                  >
                    <FaBook className="action-icon" />
                    Register for Courses
                  </Button>
                  
                  <Button 
                    variant="outline-warning"
                    className="action-button"
                    as={Link}
                    to="/student/calendar"
                  >
                    <FaCalendarAlt className="action-icon" />
                    View Calendar
                  </Button>
                </div>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default StudentDashboard;