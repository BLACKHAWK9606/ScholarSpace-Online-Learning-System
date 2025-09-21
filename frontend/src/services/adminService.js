import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:9090/api';

// Helper function to get auth header
const authHeader = () => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (user && user.token) {
    return { Authorization: `Bearer ${user.token}` };
  } else {
    return {};
  }
};

// Admin Dashboard
const getDashboardStats = async () => {
  const response = await axios.get(`${API_URL}/admin/dashboard`, { headers: authHeader() });
  return response.data;
};

// User Management
const getAllUsers = async () => {
  const response = await axios.get(`${API_URL}/admin/users`, { headers: authHeader() });
  return response.data;
};

const getUserById = async (userId) => {
  const response = await axios.get(`${API_URL}/admin/users/${userId}`, { headers: authHeader() });
  return response.data;
};

const updateUserStatus = async (userId, active) => {
  const response = await axios.put(
    `${API_URL}/admin/users/${userId}/status`, 
    { active }, 
    { headers: authHeader() }
  );
  return response.data;
};

// Course Management
const getAllCourses = async () => {
  const response = await axios.get(`${API_URL}/admin/courses`, { headers: authHeader() });
  return response.data;
};

const getCourseById = async (courseId) => {
  const response = await axios.get(`${API_URL}/admin/courses/${courseId}`, { headers: authHeader() });
  return response.data;
};

const createCourse = async (courseData) => {
  const response = await axios.post(
    `${API_URL}/admin/courses`, 
    courseData, 
    { headers: authHeader() }
  );
  return response.data;
};

const updateCourse = async (courseId, courseData) => {
  const response = await axios.put(
    `${API_URL}/admin/courses/${courseId}`, 
    courseData, 
    { headers: authHeader() }
  );
  return response.data;
};

const deleteCourse = async (courseId) => {
  const response = await axios.delete(
    `${API_URL}/admin/courses/${courseId}`, 
    { headers: authHeader() }
  );
  return response.data;
};

const activateCourse = async (courseId) => {
  const response = await axios.put(
    `${API_URL}/admin/courses/${courseId}/activate`, 
    {}, 
    { headers: authHeader() }
  );
  return response.data;
};

const deactivateCourse = async (courseId) => {
  const response = await axios.put(
    `${API_URL}/admin/courses/${courseId}/deactivate`, 
    {}, 
    { headers: authHeader() }
  );
  return response.data;
};

// Instructor Management
const getAllInstructors = async () => {
  const response = await axios.get(`${API_URL}/admin/instructors`, { headers: authHeader() });
  return response.data;
};

const getCourseInstructors = async (courseId) => {
  const response = await axios.get(
    `${API_URL}/admin/courses/${courseId}/instructors`, 
    { headers: authHeader() }
  );
  return response.data;
};

const assignInstructorToCourse = async (courseId, instructorId) => {
  const response = await axios.post(
    `${API_URL}/admin/courses/${courseId}/instructors`, 
    { instructorId }, 
    { headers: authHeader() }
  );
  return response.data;
};

const removeInstructorFromCourse = async (courseId, instructorId) => {
  const response = await axios.delete(
    `${API_URL}/admin/courses/${courseId}/instructors/${instructorId}`, 
    { headers: authHeader() }
  );
  return response.data;
};

// Enrollment Management
const getPendingEnrollments = async () => {
  const response = await axios.get(
    `${API_URL}/admin/enrollments/pending`, 
    { headers: authHeader() }
  );
  return response.data;
};

const approveEnrollment = async (enrollmentId) => {
  const response = await axios.put(
    `${API_URL}/admin/enrollments/${enrollmentId}/approve`, 
    {}, 
    { headers: authHeader() }
  );
  return response.data;
};

const rejectEnrollment = async (enrollmentId) => {
  const response = await axios.put(
    `${API_URL}/admin/enrollments/${enrollmentId}/reject`, 
    {}, 
    { headers: authHeader() }
  );
  return response.data;
};

const getEnrollmentsByCourse = async (courseId) => {
  const response = await axios.get(
    `${API_URL}/admin/courses/${courseId}/enrollments`, 
    { headers: authHeader() }
  );
  return response.data;
};

const getEnrollmentsByStudent = async (studentId) => {
  const response = await axios.get(
    `${API_URL}/admin/students/${studentId}/enrollments`, 
    { headers: authHeader() }
  );
  return response.data;
};

export const adminService = {
  // Dashboard
  getDashboardStats,
  
  // User Management
  getAllUsers,
  getUserById,
  updateUserStatus,
  
  // Course Management
  getAllCourses,
  getCourseById,
  createCourse,
  updateCourse,
  deleteCourse,
  activateCourse,
  deactivateCourse,
  
  // Instructor Management
  getAllInstructors,
  getCourseInstructors,
  assignInstructorToCourse,
  removeInstructorFromCourse,
  
  // Enrollment Management
  getPendingEnrollments,
  approveEnrollment,
  rejectEnrollment,
  getEnrollmentsByCourse,
  getEnrollmentsByStudent
};