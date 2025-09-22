# Student Course Enrollment Feature

## Overview
This feature allows students to enroll in courses with admin approval. The system implements a workflow where students can request enrollment in available courses, and administrators can approve or reject these requests.

## Implementation Details

### Backend Components

#### 1. Enrollment Model (`Enrollment.java`)
- **Status**: PENDING (default), ACTIVE, DROPPED, COMPLETED
- **Fields**: enrollmentId, course, student, enrollmentDate, status, grade, createdAt
- **Default Status**: All new enrollments start with PENDING status

#### 2. Enrollment Controller (`EnrollmentController.java`)
- `POST /api/enrollments` - Student requests enrollment
- `GET /api/enrollments/pending` - Admin gets pending enrollments
- `PUT /api/enrollments/{id}/approve` - Admin approves enrollment
- `PUT /api/enrollments/{id}/reject` - Admin rejects enrollment
- `GET /api/enrollments/student/{studentId}` - Get student's enrollments

#### 3. Enrollment Service (`EnrollmentService.java`)
- `requestEnrollment()` - Creates enrollment with PENDING status
- `updateEnrollmentStatus()` - Updates enrollment status
- Prevents duplicate enrollments for same student-course combination

### Frontend Components

#### 1. Student Course Registration (`CourseRegistration.js`)
- **Location**: `/student/courses/register`
- **Features**:
  - View available courses
  - View current enrollments with status badges
  - Enroll in courses (creates PENDING enrollment)
  - Course details modal
  - Real-time status updates with notifications

#### 2. Admin Enrollment Approval (`EnrollmentApproval.js`)
- **Location**: `/admin/enrollment-approval`
- **Features**:
  - View all pending enrollment requests
  - Approve/reject enrollments with one click
  - Student and course information display
  - Real-time updates with notifications

#### 3. Notification System
- **Toast Notifications**: Success, error, warning, info messages
- **Global Context**: `NotificationContext.js` for app-wide notifications
- **Component**: `NotificationToast.js` for reusable toast messages

### API Endpoints

#### Student Endpoints
```
POST /api/enrollments
{
  "studentId": 123,
  "courseId": 456
}
```

#### Admin Endpoints
```
GET /api/enrollments/pending
PUT /api/enrollments/{id}/approve
PUT /api/enrollments/{id}/reject
```

### User Flow

#### Student Enrollment Process
1. Student navigates to "Register for Courses"
2. Views available courses and current enrollments
3. Clicks "Enroll" on desired course
4. System creates enrollment with PENDING status
5. Student receives confirmation notification
6. Student can view enrollment status in dashboard

#### Admin Approval Process
1. Admin receives notification of pending enrollments
2. Admin navigates to "Enrollment Approvals"
3. Reviews student and course information
4. Clicks "Approve" or "Reject"
5. System updates enrollment status
6. Admin receives confirmation notification

### Status Flow
```
PENDING → ACTIVE (approved by admin)
PENDING → DROPPED (rejected by admin)
ACTIVE → DROPPED (student drops course)
ACTIVE → COMPLETED (course completion)
```

### Features Implemented
- ✅ Student course enrollment requests
- ✅ Admin approval/rejection system
- ✅ Real-time status updates
- ✅ Notification system
- ✅ Duplicate enrollment prevention
- ✅ Status badges and visual indicators
- ✅ Responsive UI components

### Navigation
- **Student**: Dashboard → "Register for Courses" button → Course Registration page
- **Admin**: Dashboard → "Review Enrollments" button → Enrollment Approval page

### Security
- Role-based access control (students can only enroll, admins can approve)
- JWT authentication required for all endpoints
- Input validation and error handling

This implementation provides a complete enrollment workflow with proper status management, notifications, and user-friendly interfaces for both students and administrators.