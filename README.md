

# 🏥 Clinic Reservation System

 A **desktop-based clinic management application** built with **JavaFX** and **MySQL**, designed to streamline appointment booking, patient–doctor communication, and clinic administration.

---

## 🎯 Overview

**DocDesk** is a comprehensive clinic reservation system that serves both **Doctors** and **Patients**. It enables doctors to manage clinics, schedules, reservations, reports, and patient communication, while allowing patients to search for doctors, book appointments, join waiting lists, and chat directly with practitioners.

The system follows **MVC architecture** and the **DAO pattern**, ensuring clean separation of concerns, scalability, and maintainability.

---

## 💻 Technology Stack

* **Language:** Java 17+
* **UI:** JavaFX (FXML)
* **UI Design:** Scene Builder
* **Database:** MySQL (JDBC)
* **Architecture:** MVC + DAO Pattern
* **Maps Integration:** Google Maps
* **Email Service:** JavaMail API
* **Reporting:** Apache POI (Excel), iText (PDF)
* **Automation:** Timer-based waiting list notifications

---

## 👨‍⚕️ Doctor Features

### Clinic Management

* Create and manage **one clinic per doctor**
* Edit clinic details (name, address, price, consultation duration)
* Set clinic location and view it via **Google Maps**
* Enable or disable **online consultation**

### Schedule & Slot Generation

* Define working days and hours
* System **automatically generates time slots** based on:

  * Schedule start & end time
  * Consultation duration
* Schedule updates apply **starting from the next month** (existing bookings remain unchanged)

### Appointments

* View all reservations
* Cancel appointments
* After appointment time:

  * Mark patient as **Present** or **Absent**
  * If marked present → patient can submit a **rating**

### Waiting List

* View waiting list when a day is fully booked
* Accept or reject additional patients
* Automatic slot offering via email

### Reports & Reviews

* View clinic ratings and reviews
* Generate full statistical reports including:

  * Confirmed appointments
  * Cancellations
  * Absences
  * Ratings
  * Booking dates and patient details
* Export reports as **PDF** or **Excel**

### Account Settings

* Update personal information
* Change username and password

---

## 👩‍💼 Patient Features

### Dashboard

* Search doctors by name, specialty, or location
* View doctor profiles and clinic details
* View clinic location on map

### Booking

* View available time slots dynamically generated from doctor schedule
* Book or cancel appointments

### Waiting List

* Join waiting list when no slots are available
* Receive automatic email notification if a slot becomes available

### Rating & Reviews

* Submit rating **only after appointment is completed**

### Communication

* Direct chat with doctor

### Account Management

* Edit personal profile and contact information

---

## 💬 Real-Time Chat System

* Professional **one-to-one chat** between doctor and patient
* Accessible from user dashboards
* Available before and after booking
* Messages stored securely in the database
* Separate conversations per appointment/user

---

## 🔁 Waiting List Automation

1. When a booking is canceled, the system selects the **first patient** in the waiting list
2. Sends an **email notification** with the available slot
3. If no booking occurs within **10 minutes**:

   * The request is marked as **Expired**
   * The system automatically notifies the next patient

---

## 🧩 UML & Database Design

### UML Diagrams

* Use Case Diagram
* Class Diagram
* Sequence Diagrams (Booking, Waiting List, Chat)
<p align="center">
  <img src="ClinicUML.png" "/>
</p>

### Database Design

* Relational and normalized schema
* Main tables:
<p align="center">
  <img src="DatabaseDiagram.png" "/>
</p>

---

## ⚙️ Requirements

* JDK 17 or higher
* MySQL Server
* Scene Builder (optional)
* JavaMail API
* Apache POI & iText libraries

---

## 🚀 Getting Started

```bash
git clone https://github.com/Nour-Sameh/ClinicReservationSystem.git
```

1. Configure database connection
2. Import SQL schema
3. Run the application
4. Make sure you downloaded all libraries 

---

## 👥 Contributors

* **Nour Sameh**
* **Nermen Ramadan**
* **Mariem Tarek**
* **Mariem Ali**

---
