<div align="center">
  <img src="media/Logos_7.png" alt="Connectra Logo" width="200">
  <h1>Connectra - The University Online Meeting Platform</h1>
</div>

Connectra is a modern and secure University Online Meeting Platform designed to enhance online learning experiences for both lecturers and students. It provides a robust, interactive, and user-friendly platform for university-level online education.

## 🚀 Key Features

### 👨‍💼 Admin Dashboard
- **User Management**: Full control over creating and managing lecturer accounts.
- **System Monitoring**: Monitor platform activity and maintain system organization.
- **Pre-created Admin**: Includes a pre-created admin account for immediate system management.

### 👨‍🏫 Lecturer Dashboard
- **Class Scheduling**: Schedule meetings for specific student groups based on degree and batch.
- **Session Management**: Record sessions, view/download attendance reports, and share recordings.
- **Quick Quiz**: Launch real-time quizzes to check student attention. Results are instant and temporary.
- **Meeting Control**: Mute all participants, end meetings, and manage screen sharing permissions.
- **Smart Attendance**: Automatically calculates attendance based on an 80% participation threshold.

### 👨‍🎓 Student Dashboard
- **Secure Registration**: Register using university-provided email addresses for verification.
- **Meeting Access**: View scheduled meetings and join via Meeting ID.
- **Profile Management**: Update profile details while keeping the university email fixed.

### 🎥 Live Meeting Features
- **Real-time Communication**: Powered by **Agora SDK** for high-quality video and audio.
- **Screen Sharing**: Available for teachers and students (with permission).
- **Interactive Tools**: "Quick Quiz" for engagement, active speaker highlighting.

## 🏗️ Architecture

<div align="center">
  <img src="media/c4_level_2_container_diagram.png" alt="C4 Level 2 Container Diagram" width="800">
</div>

## 🛠️ Technology Stack

This repository contains the **Backend** source code for Connectra.

- **Backend Framework**: Spring Boot (Java)
- **Build Tool**: Maven
- **Real-time Communication**: Agora SDK

**Related Technologies in the Connectra Ecosystem:**
- **Frontend Landing Page**: Next.js
- **Desktop Application**: Electron.js (React)

## 📂 Project Structure

```
src/
├── main/
│   ├── java/uwu/connectra/connectra_backend/
│   │   ├── config/          # Configuration classes
│   │   ├── controllers/     # REST API Controllers
│   │   ├── dtos/            # Data Transfer Objects
│   │   ├── models/          # Entity models
│   │   ├── repositories/    # Database repositories
│   │   ├── security/        # Security configurations
│   │   └── services/        # Business logic services
│   └── resources/
│       └── application.yaml # Application configuration
└── test/                    # Unit and integration tests
```

## 🏁 Getting Started

### Prerequisites
- Java 21 or higher
- Maven

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/theekshana-nirmal/connectra-backend.git
   ```
2. Navigate to the project directory:
   ```bash
   cd connectra-backend
   ```
3. Build the project:
   ```bash
   ./mvnw clean install
   ```

### Running the Application

Run the Spring Boot application using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

The backend server will start on the configured port (default is usually 8080).

---
*Developed for University Online Learning Enhancement.*