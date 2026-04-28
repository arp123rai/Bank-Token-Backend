#  Bank Token Management System - Backend

##  Project Overview
This is the backend service of the Bank Token Management System built using Spring Boot. It handles token generation, queue management, counter operations, and real-time updates for efficient bank service flow.

---

##  Core Features

### 🎫 Token Management
- Generate unique token numbers for customers
- Manage token queue in real-time
- Track current running token
- Estimate waiting time for each user

---

###  Counter System
- Supports multiple service counters:
  - Cash Counter
  - Loan Counter
  - General Service Counter
- Each counter processes tokens sequentially
- Real-time token movement between counters

---

###  Notification System
- Alerts when only 3 tokens are ahead
- Notification message: "Your turn is coming"
- Notification when token is active: "This is your turn"
- Sound trigger integration support

---

###  Admin / Manager Dashboard
- Track total tokens generated daily
- Identify peak hours (high traffic time)
- Weekly analytics data (7-day report)
- Monitor all counters activity

---

##  Tech Stack

- Java
- Spring Boot
- REST APIs
- MySQL
- Maven

---

##  Project Structure
##  Project Structure

src/
 └── main/
      ├── java/
      │    └── com/yourpackage/
      │         ├── controller/
      │         ├── service/
      │         ├── repository/
      │         ├── model/
      │         └── config/
      │
      └── resources/
           ├── application.properties (excluded)
           └── static/

pom.xml
.gitignore

### 2️⃣ Configure Database
- Create MySQL database
- Update `application.properties` with your credentials

### 3️⃣ Run Application
- Open project in Eclipse
- Run as Spring Boot application

---

##  Security Note
Sensitive data like database credentials are not included in this repository and are managed locally.

---

## 🔗 API Features
- Token generation API
- Queue status API
- Counter update API
- Dashboard analytics API

---

## Author
- Full Stack Java Developer
