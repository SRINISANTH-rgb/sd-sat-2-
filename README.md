# Java Servlet User Management System (CRUD Prototype)

A fully working **Java Servlet-based User Management System** prototype with complete **CRUD operations** (Create, Read, Update, Delete), designed for college practical records, lab assignments, and faculty demonstrations.

---

## 🌟 Key Features
- **Java Servlet Core:** Built with `HttpServlet` using standard `doGet`, `doPost`, `doPut`, `doDelete` routing.
- **Dual DAO Architecture:** 
  - **In-Memory DAO (Default):** Zero setup required. Runs out of the box using Java 17.
  - **MySQL JDBC DAO:** Ready for persistent database integration if required.
- **Modern Responsive Web GUI:** Built with HTML5, CSS3 (glassmorphism/dark mode), and JavaScript `fetch()` API.
- **Live Faculty API Console:** Interactive REST API tester built right into the UI for viva demonstrations.
- **Dual Runner:** Instant single-click Embedded Server runner (`run.bat` / `run.ps1`) AND standard Tomcat WAR packaging (`pom.xml`).

---

## 🚀 Quick Start Guide

### Option 1: Run Instantly (No Tomcat/Maven Installation Needed!)
Simply execute the launcher script in PowerShell or CMD:

```powershell
.\run.ps1
```
*or in Command Prompt:*
```cmd
.\run.bat
```

Then open your browser at **`http://localhost:8080`**.

---

### Option 2: Deploy on Apache Tomcat / Eclipse
Build the WAR archive:
```bash
mvn clean package
```
Deploy the output file `target/user-management.war` into your Tomcat `webapps/` folder.

---

## 📂 Project Structure
- `src/main/java/com/usermanagement/model/User.java` - User Entity Model
- `src/main/java/com/usermanagement/dao/` - Data Access Layer (InMemory & MySQL DAOs)
- `src/main/java/com/usermanagement/servlet/UserServlet.java` - HttpServlet Controller
- `src/main/java/com/usermanagement/MainApp.java` - Embedded HTTP Server Launcher
- `src/main/webapp/` - HTML, CSS, JavaScript Frontend
- `LAB_EXPERIMENT_REPORT.md` - Complete Practical Record Report & Faculty Viva Q&A

---

## 📝 Lab Practical Record
See [`LAB_EXPERIMENT_REPORT.md`](file:///c:/Users/SRINISANTH/OneDrive/Documents/Desktop/SD%20SAT%202/LAB_EXPERIMENT_REPORT.md) for the complete experiment report, system architecture diagrams, and faculty viva questions.
