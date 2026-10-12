# EXPERIMENT REPORT: USER MANAGEMENT SYSTEM WITH CRUD OPERATIONS USING JAVA SERVLETS

**Course / Subject:** Web Technologies / Advanced Java Lab Practical  
**Experiment Name:** Development of User Management System with CRUD Operations using Java Servlets, HTML/CSS/JS, and Data Access Objects (DAO).  

---

## 1. OBJECTIVE & AIM
To design, implement, and test a working web application prototype for a **User Management System** using **Java Servlets (HttpServlet)** for server-side processing, **HTML5/CSS3/JavaScript** for client-side representation, and the **Data Access Object (DAO) Pattern** supporting both **In-Memory Storage** (zero database dependency) and **MySQL Database Persistence**.

---

## 2. FUNCTIONAL REQUIREMENTS (CRUD)

| Operation | Action | Http Method | Endpoint | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Create** | Add User | `POST` | `/users` | Accepts `name`, `email`, `role`, `status` and saves new user record. |
| **Read** | View Users | `GET` | `/users` | Retrieves all users or a specific user by ID in JSON/Table format. |
| **Update** | Edit User | `POST` / `PUT` | `/users?action=update` | Modifies existing user details (`name`, `email`, `role`, `status`). |
| **Delete** | Delete User | `POST` / `DELETE` | `/users?action=delete` | Removes a user record by ID from memory/database. |

---

## 3. ARCHITECTURE & SERVLET LIFECYCLE

### 3.1 Architecture Overview
The application follows the classic 3-Tier MVC (Model-View-Controller) architecture:
1. **View (Frontend):** `index.html`, `style.css`, `app.js` providing dynamic UI and asynchronous `fetch()` HTTP calls.
2. **Controller (Java Servlet):** `UserServlet.java` handling HTTP requests (`doGet`, `doPost`, `doPut`, `doDelete`), parameter validation, and routing.
3. **Model & DAO (Data Layer):** `User.java` (Entity), `IUserDAO.java` (Interface), `InMemoryUserDAO.java` (In-Memory ConcurrentHashMap storage), and `MySQLUserDAO.java` (JDBC persistence).

```
 +------------------------+     HTTP Requests      +------------------------+
 |   Client Web Browser   | ---------------------> |      UserServlet       |
 | (index.html + app.js)  | <--------------------- |  (javax.servlet.http)  |
 +------------------------+      JSON Response     +------------------------+
                                                               |
                                                   Uses DAO Interface
                                                               v
                                                   +------------------------+
                                                   |   InMemoryUserDAO /    |
                                                   |     MySQLUserDAO       |
                                                   +------------------------+
```

### 3.2 Servlet Lifecycle Methods
- **`init()`**: Initializes the DAO instance (`InMemoryUserDAO` or `MySQLUserDAO`).
- **`service()`**: Receives incoming requests and dispatches to appropriate HTTP handler methods.
- **`doGet()`**: Fetches user list or single user details.
- **`doPost()`**: Handles user creation, updates, and deletion.
- **`doPut()`**: Updates user attributes.
- **`doDelete()`**: Deletes specified user.
- **`destroy()`**: Releases resources when the server shuts down.

---

## 4. CODE STRUCTURE & IMPLEMENTATION

### Project Directory Tree:
```
SD SAT 2/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── usermanagement/
│       │           ├── model/
│       │           │   └── User.java
│       │           ├── dao/
│       │           │   ├── IUserDAO.java
│       │           │   ├── InMemoryUserDAO.java
│       │           │   └── MySQLUserDAO.java
│       │           ├── servlet/
│       │           │   └── UserServlet.java
│       │           └── MainApp.java
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml
│           ├── index.html
│           ├── css/
│           │   └── style.css
│           └── js/
│               └── app.js
├── lib/
│   └── javax.servlet-api-4.0.1.jar
├── pom.xml
├── run.bat
└── run.ps1
```

---

## 5. STEP-BY-STEP EXECUTION GUIDE

### Method A: One-Click Execution (Embedded Server - Recommended)
1. Open PowerShell or Command Prompt in the project folder.
2. Run the executable script:
   ```cmd
   .\run.bat
   ```
   *or using PowerShell:*
   ```powershell
   .\run.ps1
   ```
3. Open your browser and navigate to:
   **`http://localhost:8080`**

### Method B: Apache Tomcat / Eclipse IDE Deployment
1. Build `.war` file using Maven:
   ```cmd
   mvn clean package
   ```
2. Copy `target/user-management.war` into Tomcat's `webapps/` folder.
3. Access: **`http://localhost:8080/user-management/`**

---

## 6. OPTIONAL MYSQL DATABASE CONFIGURATION

If database persistence is required by your faculty:
1. Create a MySQL database and table using this SQL script:
   ```sql
   CREATE DATABASE usermanagement_db;
   USE usermanagement_db;

   CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       email VARCHAR(100) NOT NULL,
       role VARCHAR(50) DEFAULT 'Student',
       status VARCHAR(20) DEFAULT 'Active'
   );
   ```
2. In `src/main/java/com/usermanagement/servlet/UserServlet.java`, change line 25 in `init()`:
   ```java
   // Switch from InMemoryUserDAO to MySQLUserDAO
   this.userDAO = new com.usermanagement.dao.MySQLUserDAO();
   ```

---

## 7. FACULTY VIVA VOCE QUESTIONS & ANSWERS

**Q1: What is a Java Servlet?**  
*Answer:* A Servlet is a Java class used to extend the capabilities of servers that host applications accessed by means of a request-response programming model (commonly HTTP).

**Q2: What is the difference between `GenericServlet` and `HttpServlet`?**  
*Answer:* `GenericServlet` is protocol-independent, whereas `HttpServlet` is specifically designed for the HTTP protocol and provides methods like `doGet()`, `doPost()`, `doPut()`, and `doDelete()`.

**Q3: Why use the Data Access Object (DAO) Pattern?**  
*Answer:* The DAO pattern decouples business logic from data persistence code. It enables seamless switching between storage implementations (e.g., In-Memory List vs. MySQL Database) without modifying servlet controller code.

**Q4: How does `UserServlet` process HTTP GET and POST requests?**  
*Answer:* The container routes GET requests to `doGet()` to return user data in JSON, while POST/PUT/DELETE requests route to `doPost()`, `doPut()`, or `doDelete()` to handle data mutations.

---

**Conclusion:** The User Management System prototype successfully implements all CRUD operations using Java Servlets, HTML, CSS, JavaScript, and DAO design patterns.
