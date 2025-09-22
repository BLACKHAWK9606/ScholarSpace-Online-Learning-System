import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container } from 'react-bootstrap';

// Import authentication components
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import ForgotPassword from './components/Auth/ForgotPassword';
import ResetPassword from './components/Auth/ResetPassword';
import PrivateRoute from './components/Auth/PrivateRoute';

// Import dashboard components
import AdminDashboard from './components/Admin/AdminDashboard';
import InstructorDashboard from './components/Instructor/InstructorDashboard';
import StudentDashboard from './components/Student/StudentDashboard';

// Import admin components
import ManageCourses from './components/Admin/ManageCourses';
import ManageInstructors from './components/Admin/ManageInstructors';
import ManageDepartments from './components/Admin/ManageDepartments';
import CourseAssignment from './components/Admin/CourseAssignment';
import EnrollmentApproval from './components/Admin/EnrollmentApproval';

// Import student components
import CourseRegistration from './components/Student/CourseRegistration';

// Import context providers
import { AuthProvider } from './contexts/AuthContext';

// Import common components
import Navbar from './components/common/Navbar';
import CourseDetails from './components/common/CourseDetails';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <Container className="mt-4">
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />

              {/* Protected Routes */}
              <Route 
                path="/admin/dashboard" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <AdminDashboard />
                  </PrivateRoute>
                } 
              />

              {/* Admin Course Management Routes */}
              <Route 
                path="/admin/courses" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <ManageCourses />
                  </PrivateRoute>
                } 
              />

              {/* Admin Instructor Management Routes */}
              <Route 
                path="/admin/instructors" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <ManageInstructors />
                  </PrivateRoute>
                } 
              />

              {/* Admin Department Management Routes */}
              <Route 
                path="/admin/departments" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <ManageDepartments />
                  </PrivateRoute>
                } 
              />

              {/* Admin Instructor Assignment Routes */}
              <Route 
                path="/admin/course-assignment" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <CourseAssignment />
                  </PrivateRoute>
                } 
              />

              {/* Admin Enrollment Approval Routes */}
              <Route 
                path="/admin/enrollment-approval" 
                element={
                  <PrivateRoute allowedRoles={['admin']}>
                    <EnrollmentApproval />
                  </PrivateRoute>
                } 
              />

              <Route 
                path="/instructor/dashboard" 
                element={
                  <PrivateRoute allowedRoles={['instructor']}>
                    <InstructorDashboard />
                  </PrivateRoute>
                } 
              />

              <Route 
                path="/student/dashboard" 
                element={
                  <PrivateRoute allowedRoles={['student']}>
                    <StudentDashboard />
                  </PrivateRoute>
                } 
              />

              {/* Student Course Registration Route */}
              <Route 
                path="/student/courses/register" 
                element={
                  <PrivateRoute allowedRoles={['student']}>
                    <CourseRegistration />
                  </PrivateRoute>
                } 
              />

              {/* Default Route - Redirect to login */}
              {/* Course Routes */}
              <Route 
                path="/courses/:courseId" 
                element={
                  <PrivateRoute allowedRoles={['admin', 'instructor', 'student']}>
                    <CourseDetails />
                  </PrivateRoute>
                } 
              />
              
              {/* Default Route - Redirect to login */}
              <Route path="/" element={<Login />} />
            </Routes>
          </Container>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;