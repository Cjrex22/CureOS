# CureOS 🏥
# Hospital Management System

**CureOS** is a complete, desktop-based Hospital Management System developed entirely in **100% pure Java (JDK 8+)**. Built using Java Swing for the graphical user interface and SQLite for local, zero-configuration database persistence, CureOS is designed to be a clean, fast, and highly reliable tool for hospital receptionists, administrators, and doctors.

---

## 🚀 Features

CureOS includes a fully fleshed-out suite of modules to handle all aspects of hospital administration:

- **🔐 Secure Authentication**: Includes both Login and Account Creation (Sign Up) with role-based access (Admin, Receptionist, Doctor) and password confirmation validation.
- **📊 Live Dashboard**: A real-time overview of hospital operations, featuring live-updating summary cards (Total Patients, Available Doctors, Pending Appointments, Occupied Beds) and a live digital clock.
- **👤 Patient Management**: Full CRUD (Create, Read, Update, Delete) capabilities for patient records, including interactive patient search and dynamic ID generation (e.g., P001).
- **🩺 Doctor Directory**: Manage hospital staff, track their specializations, their contact information, shift timings, and real-time availability.
- **📅 Appointment Scheduling**: Book, confirm, cancel, or complete appointments. Features advanced, dynamic table filtering by Month, Doctor, and Appointment Status.
- **🛏️ Visual Ward & Bed Management**: A color-coded, interactive grid layout representing hospital beds across different wards (General, ICU, Maternity, etc.). Easily assign patients to beds, reserve beds, or discharge patients to free up space.
- **💊 Digital Prescriptions**: Doctors can generate and save prescriptions with a dynamic, multi-row medication table. Includes features to view patient prescription history and print prescriptions.
- **📉 Reporting Engine**: Generate instant system reports (e.g., Patients Admitted This Month, Doctors on Duty) and explicitly export these tables to cleanly formatted `.txt` files for archiving.
- **⚙️ System Settings**: Administrative tools to change the master admin password, view system info, and a secure "Danger Zone" to factory reset the database.

---

## 🛠️ Technology Stack

- **Language**: Java Only ☕
- **GUI Framework**: Java Swing (`JFrame`, `JPanel`, `JTable`, custom `Graphics2D` painting for aesthetics). NO HTML/CSS/JavaScript.
- **Database**: SQLite (via `sqlite-jdbc`). File-based persistence ensuring data remains entirely local to the machine (`cureos.db`).

---

## 🎨 UI Design & Aesthetics

CureOS prioritizes a clean, professional, and accessible user experience:
- **Color Scheme**: 
  - 🔵 Primary Blue (`#1A73E8`)
  - ⚪ Clean White (`#FFFFFF`)
  - 🟢 Success Green (`#34A853`)
  - 🔴 Danger Red (`#EA4335`)
  - 🟡 Warning Yellow (`#FBBC04`)
- All buttons feature custom rounded borders and smooth focus painting.
- The hospital logo (Red Cross) is synthetically drawn using pure `Graphics2D` rendering, requiring zero external image assets.

---

## ⚙️ Installation & Execution

Running CureOS is incredibly simple. It requires no complex server setups, no Maven/Gradle configurations, and no remote database holding.

### Prerequisites:
1. Java Development Kit (JDK 8 or higher) installed.
2. The `sqlite-jdbc` JAR file.

### Steps to Run:
1. Clone the repository.
2. Ensure you have a `lib/` directory inside the project folder containing the `sqlite-jdbc.jar` file. (You can download the latest version from Maven Central).
3. Open a terminal in the root directory of the project.
4. Run the included bash script:
   ```bash
   ./run.sh
   ```
   *(Alternatively, you can manually compile and run using `javac` and `java` commands, or simply run `Main.java` via your preferred Java IDE like IntelliJ, Eclipse, or VS Code, ensuring the `lib` folder is added to your project's classpath).*

### Default Credentials:
If you are running the app for the very first time, CureOS automatically seeds the database with three default accounts:
- **Admin**: `admin` / `admin123`
- **Receptionist**: `receptionist` / `rec123`
- **Doctor**: `doctor` / `doc123`

You can immediately use these or create your own via the **Create Account** button.

---

## 📂 File Structure Overview

All source code is cleanly separated by functional modules within the `src/` directory:

- `Main.java` — Application entry point.
- `DatabaseHelper.java` — JDBC SQLite connection management and schema initialization.
- `UIHelper.java` — Global design tokens, custom button painters, and table stylers.
- `LoginFrame.java` — CardLayout handling Login & Sign Up views.
- `DashboardFrame.java` — The primary container, sidebar navigation, and dashboard summary views.
- `PatientPanel.java` — Patient CRUD operations.
- `DoctorPanel.java` — Doctor CRUD operations.
- `AppointmentPanel.java` — Appointment scheduling and dynamic filtering.
- `WardPanel.java` — Visual bed assignment and ward management grid.
- `PrescriptionPanel.java` — Split-view prescription history and dynamic medicine entry form.
- `ReportPanel.java` — SQL reporting and `.txt` file exporting.
- `SettingsPanel.java` — App settings and database wipe utility.
