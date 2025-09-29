import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Table, Form, InputGroup, Badge, Button, Modal, Alert } from 'react-bootstrap';
import { FaSearch, FaUserGraduate, FaEnvelope, FaPhone, FaMapMarkerAlt, FaEye, FaCheck, FaTimes } from 'react-icons/fa';
import api from '../../services/api';
import Breadcrumb from '../common/Breadcrumb';

function StudentManagement() {
  const [students, setStudents] = useState([]);
  const [filteredStudents, setFilteredStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [enrollmentRequests, setEnrollmentRequests] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const breadcrumbItems = [
    { label: 'Dashboard', path: '/admin/dashboard', type: 'dashboard' },
    { label: 'Student Management', type: 'student' }
  ];

  useEffect(() => {
    fetchStudents();
    fetchEnrollmentRequests();
  }, []);

  useEffect(() => {
    filterStudents();
  }, [students, searchTerm, statusFilter]);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const response = await api.get('/dashboard/students');
      setStudents(response.data);
    } catch (error) {
      setError('Failed to fetch students');
      console.error('Error fetching students:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchEnrollmentRequests = async () => {
    try {
      const response = await api.get('/enrollments/pending');
      setEnrollmentRequests(response.data);
    } catch (error) {
      console.error('Error fetching enrollment requests:', error);
    }
  };

  const filterStudents = () => {
    let filtered = students;

    if (searchTerm) {
      filtered = filtered.filter(student =>
        student.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        student.email.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(student => 
        statusFilter === 'active' ? student.isActive : !student.isActive
      );
    }

    setFilteredStudents(filtered);
  };

  const handleViewStudent = (student) => {
    setSelectedStudent(student);
    setShowModal(true);
  };

  const handleApproveEnrollment = async (enrollmentId) => {
    try {
      await api.put(`/enrollments/${enrollmentId}/approve`);
      setSuccess('Enrollment approved successfully');
      await fetchEnrollmentRequests();
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setError('Failed to approve enrollment');
      setTimeout(() => setError(''), 3000);
    }
  };

  const handleRejectEnrollment = async (enrollmentId) => {
    try {
      await api.put(`/enrollments/${enrollmentId}/reject`);
      setSuccess('Enrollment rejected successfully');
      await fetchEnrollmentRequests();
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setError('Failed to reject enrollment');
      setTimeout(() => setError(''), 3000);
    }
  };

  const getStatusBadge = (isActive) => {
    return isActive ? 
      <Badge bg="success">Active</Badge> : 
      <Badge bg="secondary">Inactive</Badge>;
  };

  if (loading) {
    return (
      <Container className="py-4">
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      </Container>
    );
  }

  return (
    <Container fluid className="py-4">
      <Breadcrumb items={breadcrumbItems} />
      
      {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert variant="success" dismissible onClose={() => setSuccess('')}>{success}</Alert>}

      <Row className="mb-4">
        <Col>
          <div className="d-flex justify-content-between align-items-center">
            <div>
              <h2 className="mb-1">
                <FaUserGraduate className="me-2 text-primary" />
                Student Management
              </h2>
              <p className="text-muted">Manage student accounts and enrollment requests</p>
            </div>
            <div className="d-flex align-items-center">
              <Badge bg="info" className="me-2">
                Total Students: {students.length}
              </Badge>
              <Badge bg="warning">
                Pending Requests: {enrollmentRequests.length}
              </Badge>
            </div>
          </div>
        </Col>
      </Row>

      {/* Enrollment Requests Section */}
      {enrollmentRequests.length > 0 && (
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Pending Enrollment Requests ({enrollmentRequests.length})</h5>
          </Card.Header>
          <Card.Body>
            <Table responsive>
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Course</th>
                  <th>Requested Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {enrollmentRequests.map((request) => (
                  <tr key={request.enrollmentId}>
                    <td>
                      <div>
                        <strong>{request.student?.name || 'Unknown Student'}</strong>
                        <div className="small text-muted">{request.student?.email}</div>
                      </div>
                    </td>
                    <td>
                      <div>
                        <strong>{request.course?.title || 'Unknown Course'}</strong>
                        <div className="small text-muted">{request.course?.code}</div>
                      </div>
                    </td>
                    <td>{new Date(request.enrollmentDate).toLocaleDateString()}</td>
                    <td>
                      <div className="btn-group">
                        <Button
                          variant="outline-success"
                          size="sm"
                          onClick={() => handleApproveEnrollment(request.enrollmentId)}
                        >
                          <FaCheck /> Approve
                        </Button>
                        <Button
                          variant="outline-danger"
                          size="sm"
                          onClick={() => handleRejectEnrollment(request.enrollmentId)}
                        >
                          <FaTimes /> Reject
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}

      {/* Students List Section */}
      <Card>
        <Card.Header>
          <Row className="align-items-center">
            <Col md={6}>
              <h5 className="mb-0">All Students</h5>
            </Col>
            <Col md={6}>
              <Row>
                <Col md={8}>
                  <InputGroup>
                    <InputGroup.Text>
                      <FaSearch />
                    </InputGroup.Text>
                    <Form.Control
                      type="text"
                      placeholder="Search students..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                    />
                  </InputGroup>
                </Col>
                <Col md={4}>
                  <Form.Select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                  >
                    <option value="all">All Status</option>
                    <option value="active">Active</option>
                    <option value="inactive">Inactive</option>
                  </Form.Select>
                </Col>
              </Row>
            </Col>
          </Row>
        </Card.Header>
        <Card.Body>
          {filteredStudents.length === 0 ? (
            <div className="text-center py-5">
              <FaUserGraduate size={48} className="text-muted mb-3" />
              <h5>No students found</h5>
              <p className="text-muted">
                {searchTerm || statusFilter !== 'all' 
                  ? 'Try adjusting your search or filter criteria'
                  : 'No students have registered yet'
                }
              </p>
            </div>
          ) : (
            <Table responsive hover>
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Contact</th>
                  <th>Status</th>
                  <th>Joined</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredStudents.map((student) => (
                  <tr key={student.userId}>
                    <td>
                      <div>
                        <strong>{student.name}</strong>
                        <div className="small text-muted">ID: {student.userId}</div>
                      </div>
                    </td>
                    <td>
                      <div>
                        <div className="small">
                          <FaEnvelope className="me-1" />
                          {student.email}
                        </div>
                        {student.phone && (
                          <div className="small">
                            <FaPhone className="me-1" />
                            {student.phone}
                          </div>
                        )}
                      </div>
                    </td>
                    <td>{getStatusBadge(student.isActive)}</td>
                    <td>{new Date(student.createdAt).toLocaleDateString()}</td>
                    <td>
                      <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => handleViewStudent(student)}
                      >
                        <FaEye /> View
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>

      {/* Student Details Modal */}
      <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Student Details</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedStudent && (
            <Row>
              <Col md={6}>
                <h6>Personal Information</h6>
                <p><strong>Name:</strong> {selectedStudent.name}</p>
                <p><strong>Email:</strong> {selectedStudent.email}</p>
                {selectedStudent.phone && <p><strong>Phone:</strong> {selectedStudent.phone}</p>}
                {selectedStudent.dateOfBirth && (
                  <p><strong>Date of Birth:</strong> {new Date(selectedStudent.dateOfBirth).toLocaleDateString()}</p>
                )}
                {selectedStudent.address && (
                  <p><strong>Address:</strong> {selectedStudent.address}</p>
                )}
              </Col>
              <Col md={6}>
                <h6>Account Information</h6>
                <p><strong>User ID:</strong> {selectedStudent.userId}</p>
                <p><strong>Status:</strong> {getStatusBadge(selectedStudent.isActive)}</p>
                <p><strong>Joined:</strong> {new Date(selectedStudent.createdAt).toLocaleDateString()}</p>
                {selectedStudent.emergencyContact && (
                  <p><strong>Emergency Contact:</strong> {selectedStudent.emergencyContact}</p>
                )}
              </Col>
            </Row>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default StudentManagement;