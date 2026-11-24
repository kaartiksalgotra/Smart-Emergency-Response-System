# Smart Emergency Response and Resource Allocation System (ERAS)

Java and JavaFX-based desktop application for real-time emergency dispatch, multi-threaded resource allocation, priority queuing, and incident tracking.

---

## 🚀 Quick Setup & Run Instructions for Teammates

### 1. Prerequisites
- **Java Development Kit (JDK 17 or higher)** (Java 17, 21, etc.)
- **IntelliJ IDEA** (Community or Ultimate)
- **MySQL Server** (running locally on port 3306)

---

### 2. Database Setup
1. Open **MySQL Workbench** or your MySQL command line.
2. Execute the database script in order:
   - First run: [`database/schema.sql`](file:///database/schema.sql)
   - Then run: [`database/sample_data.sql`](file:///database/sample_data.sql)
3. Open [`db.properties`](file:///db.properties) in the project root:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_emergency_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=YOUR_MYSQL_USERNAME
   db.password=YOUR_MYSQL_PASSWORD
   ```
   *(Update username and password to match your local MySQL configuration if needed).*

---

### 3. Open in IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Click **File -> Open...** and select the cloned `Smart-Emergency-Response-System` root directory.
3. IntelliJ will automatically detect the project structure, source folders (`back_end/src`, `frontend/src`), and bundled libraries (`lib/javafx-sdk`, `lib/mysql-connector-j-26.10.0.jar`).
4. If prompted to **Set Project SDK**, select **JDK 17** (or your installed JDK).

---

### 4. Running the Application (1-Click Run)

The repository includes pre-configured **Shared Run Configurations**:

1. Look at the top-right toolbar in IntelliJ IDEA at the **Run Configurations** dropdown:
   - **Option 1 (Recommended): `App (Zero-Config Launcher)`**  
     Runs `com.emergency.App`. Bypasses JavaFX runtime checks and runs instantly on Windows, macOS, and Linux without any manual VM arguments!
   - **Option 2: `FrontendMain`**  
     Runs `com.emergency.FrontendMain` with automated relative JavaFX module path (`$PROJECT_DIR$/lib/javafx-sdk`).
2. Simply click the green **Run (▶)** button!

---

### 5. Default Login Credentials

| Role | Email | Password |
|---|---|---|
| **System Admin** | `admin@emergency.com` | `admin123` |
| **Citizen User** | `user@emergency.com` | `user123` |

---

## 📁 Project Structure

- `frontend/src/com/emergency/` - Modern JavaFX UI views, custom responsive components, and controllers.
- `back_end/src/com/emergency/` - Core domain models, JDBC database repositories (`storage/`), services, and multithreaded `EmergencyProcessor` priority queue.
- `database/` - MySQL schema, initial tables, and sample seed data.
- `lib/` - Bundled JavaFX SDK JARs and MySQL Connector.
- `.idea/` - IntelliJ project configuration and shared 1-click run configurations.
