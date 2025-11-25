# 🏥 Clinic Management System

## 🌟 Overview
**ClinicDB** is a comprehensive system designed to efficiently manage clinics, practitioners, patients, appointments, ratings, real-time chat, and waiting list operations.  
The project is fully implemented in **Java**, applying the **DAO (Data Access Object) Pattern** for database interaction.

---

## 🔑 Key Features

### 👤 User Management
- Secure storage of Patients and Practitioners.
- Console-based login and role-specific menu options.

### 📅 Appointment Management (CRUD)
- Book, update, cancel, and view appointments.
- Prevents double-booking for the same time slot.
- Practitioners can view all upcoming appointments.

### ⏳ Waiting List System (Queue)
- FIFO logic that automatically assigns cancelled slots to the next waiting patient.
- Based on the `request_time` of each waiting entry.

### ⭐ Rating System
- Patients can rate clinics after their appointments.
- Ratings include **score (1–5)** and **comment**.
- Clinics display their average rating.

### 💬 1:1 Chat System
- Real-time and persistent messaging between patient and practitioner.
- Each conversation is stored in a dedicated chat session.

### 🕒 Scheduling
- Clinics can define detailed working hours using:
  - `Schedule`
  - `WorkingHoursRule`
- Time slots generated automatically based on the working hours.

---

## 🏗️ Technical Stack

| Component       | Description |
|----------------|-------------|
| **Language**   | Java |
| **Database**   | MySQL / MariaDB |
| **Architecture** | DAO Pattern |
| **Tools** | NetBeans, MySQL Workbench |

---

## 👥 Contributors

- **Nour Sameh**  
- **Mariem Tarek**
- **Nermen Ramadan**
- **Mariem Ali**

---

## 📜 License
This project is currently provided without a specific license.  
You may add MIT, Apache, or GPL license depending on your needs.
