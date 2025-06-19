# ScholarSpace Learning Management System

ScholarSpace is a comprehensive Learning Management System (LMS) designed to facilitate educational interactions between students, instructors, and administrators. The platform provides a centralized environment for course management, enrollment, content delivery, assignment submission, and grading.

## Features

### Implemented Features
- User authentication (login, registration, password reset)
- Admin dashboard for managing departments, instructors, and courses
- Course assignment to instructors
- Basic student and instructor dashboards
- Role-based access control

### Upcoming Features
- Student course enrollment with admin approval
- Course content management
- Assignment submission and grading
- Notifications and announcements
- User profile settings

## Technology Stack

### Frontend
- React.js
- React Bootstrap
- Axios for API communication
- React Router for navigation

### Backend
- Spring Boot
- Spring Security
- MySQL Database
- JWT Authentication

## Prerequisites

Before you begin, ensure you have the following installed:
- Node.js (v14 or later)
- npm (v6 or later)
- Java Development Kit (JDK) 11 or later
- Maven
- MySQL Server

## Installation and Setup

### Database Setup

1. Create a MySQL database named `scholarspace`:
```sql
CREATE DATABASE scholarspace;
```

2. Create a user and grant privileges (or use an existing user):
```sql
CREATE USER 'scholarspace_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON scholarspace.* TO 'scholarspace_user'@'localhost';
FLUSH PRIVILEGES;
```

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/your-username/ScholarSpace.git
cd ScholarSpace
```

2. Navigate to the backend directory:
```bash
cd Scholarspace-admin/admin
```

3. Configure the database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/scholarspace?useSSL=false&serverTimezone=UTC
spring.datasource.username=scholarspace_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

4. Build and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```

The backend server will start on http://localhost:8080.

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd ../../frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create a `.env` file in the frontend directory with the following content:
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

4. Start the frontend development server:
```bash
npm start
```

The frontend application will start on http://localhost:1010.

## Project Structure

```
ScholarSpace/
├── Scholarspace-admin/          # Backend admin service
│   └── admin/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/scholarspace/admin/
│       │   │   │   ├── controllers/     # REST controllers
│       │   │   │   ├── models/          # Entity models
│       │   │   │   ├── repositories/    # Data repositories
│       │   │   │   ├── services/        # Business logic
│       │   │   │   └── AdminApplication.java
│       │   │   └── resources/
│       │   │       └── application.properties
│       │   └── test/
│       └── pom.xml
├── frontend/                    # React frontend
│   ├── public/
│   ├── src/
│   │   ├── components/          # React components
│   │   │   ├── Admin/           # Admin-specific components
│   │   │   ├── Instructor/      # Instructor-specific components
│   │   │   └── Student/         # Student-specific components
│   │   ├── services/            # API services
│   │   ├── utils/               # Utility functions
│   │   ├── App.js               # Main application component
│   │   └── index.js             # Entry point
│   ├── package.json
│   └── .env
└── README.md
```

## User Accounts for Testing

### Admin
- Email: admin@scholarspace.com
- Password: admin123

### Instructor
- Email: instructor@scholarspace.com
- Password: instructor123

### Student
- Email: student@scholarspace.com
- Password: student123

## API Documentation

The API documentation is available at http://localhost:8080/swagger-ui.html when the backend is running.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [React Bootstrap](https://react-bootstrap.github.io/)
- [MySQL](https://www.mysql.com/)
