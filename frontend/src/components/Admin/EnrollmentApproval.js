import React, { useState, useEffect } from 'react';
import { 
  Container, Card, Button, Table, 
  Spinner, Alert, Dropdown
} from 'react-bootstrap';
import { FaCheck, FaTimes, FaEllipsisV, FaEye } from 'react-icons/fa';
import { adminService } from '../../services/api';

function EnrollmentApproval() {
  // State for enrollments and UI
  const [pendingEnrollments, setPendingEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [processingId, setProcessingId] = useState(null);

  // Fetch pending enrollments when component mounts
  useEffect(() => {
    fetchPendingEnrollments();
  }, []);

  // Function to fetch pending enrollments
  const fetchPendingEnrollments = async () => {
    setLoading(true);
    try {
      const data = await adminService.getPendingEnrollments();
      setPendingEnrollments(data);
      setError(null);
    } catch (err) {
      console.error("Error fetching pending enrollments:", err);
      setError("Failed to load enrollment requests. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  // Handle enrollment approval
  const handleApproveEnrollment = async (enrollmentId) => {
    setProcessingId(enrollmentId);
    try {
      await adminService.approveEnrollment(enrollmentId);
      // Remove the approved enrollment from the list
      setPendingEnrollments(pendingEnrollments.filter(e => e.enrollmentId !== enrollmentId));
      setError(null);
    } catch (err) {
      console.error("Error approving enrollment:", err);
      setError("Failed to approve enrollment. Please try again.");
    } finally {
      setProcessingId(null);
    }
  };

  // Handle enrollment rejection
  const handleRejectEnrollment = async (enrollmentId) => {
    if (window.confirm("Are you sure you want to reject this enrollment request?")) {
      setProcessingId(enrollmentId);
      try {
        await adminService.rejectEnrollment(enrollmentId);
        // Remove the rejected enrollment from the list
        setPendingEnrollments(pendingEnrollments.filter(e => e.enrollmentId !== enrollmentId));
        setError(null);
      } catch (err) {
        console.error("Error rejecting enrollment:", err);
        setError("Failed to reject enrollment. Please try again.");
      } finally {
        setProcessingId(null);
      }
    }
  };

  // View student details
  const handleViewStudent = (studentId) => {
    // This would navigate to student details or open a modal
    alert(`View student details for ID: ${studentId}`);
  };

  // View course details
  const handleViewCourse = (courseId) => {
    // This would navigate to course details or open a modal
    alert(`View course details for ID: ${courseId}`);
  };

  // Format date for display
  const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  // Render loading spinner
  if (loading && pendingEnrollments.length === 0) {
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
            <h4 className="mb-0">Enrollment Requests</h4>
            <Button variant="outline-primary" onClick={fetchPendingEnrollments}>
              Refresh
            </Button>
          </div>
        </Card.Header>
        <Card.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          
          <Table responsive hover className="align-middle">
            <thead className="bg-light">
              <tr>
                <th>Student</th>
                <th>Course</th>
                <th>Requested On</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {pendingEnrollments.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-center py-4">
                    No pending enrollment requests found.
                  </td>
                </tr>
              ) : (
                pendingEnrollments.map(enrollment => (
                  <tr key={enrollment.enrollmentId}>
                    <td>
                      <div className="d-flex align-items-center">
                        <div className="student-avatar me-2">
                          {enrollment.student.firstName.charAt(0)}{enrollment.student.lastName.charAt(0)}
                        </div>
                        <div>
                          <div className="fw-bold">{enrollment.student.firstName} {enrollment.student.lastName}</div>
                          <small className="text-muted">{enrollment.student.email}</small>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="fw-bold">{enrollment.course.courseCode}</div>
                      <div>{enrollment.course.courseName}</div>
                    </td>
                    <td>
                      {formatDate(enrollment.enrollmentDate)}
                    </td>
                    <td>
                      {processingId === enrollment.enrollmentId ? (
                        <Spinner animation="border" size="sm" />
                      ) : (
                        <div className="d-flex">
                          <Button 
                            variant="success" 
                            size="sm" 
                            className="me-2"
                            onClick={() => handleApproveEnrollment(enrollment.enrollmentId)}
                          >
                            <FaCheck className="me-1" /> Approve
                          </Button>
                          <Button 
                            variant="danger" 
                            size="sm"
                            onClick={() => handleRejectEnrollment(enrollment.enrollmentId)}
                          >
                            <FaTimes className="me-1" /> Reject
                          </Button>
                          <Dropdown align="end" className="ms-2">
                            <Dropdown.Toggle variant="light" size="sm" id={`dropdown-${enrollment.enrollmentId}`} className="btn-icon">
                              <FaEllipsisV />
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                              <Dropdown.Item onClick={() => handleViewStudent(enrollment.student.userId)}>
                                <FaEye className="me-2" /> View Student
                              </Dropdown.Item>
                              <Dropdown.Item onClick={() => handleViewCourse(enrollment.course.courseId)}>
                                <FaEye className="me-2" /> View Course
                              </Dropdown.Item>
                            </Dropdown.Menu>
                          </Dropdown>
                        </div>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      <style jsx>{`
        .student-avatar {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background-color: #e9ecef;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
        }
      `}</style>
    </Container>
  );
}

export default EnrollmentApproval;
