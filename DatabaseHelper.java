
// DatabaseHelper.java
import javax.swing.JOptionPane;
import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:cureos.db";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public static void initDB() {
        String[] tables = {
                "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, role TEXT)",
                "CREATE TABLE IF NOT EXISTS patients (patient_id TEXT PRIMARY KEY, name TEXT, age INTEGER, gender TEXT, blood_group TEXT, contact TEXT, disease TEXT, doctor_id TEXT, admission_date TEXT, ward TEXT, bed_no TEXT, status TEXT)",
                "CREATE TABLE IF NOT EXISTS doctors (doctor_id TEXT PRIMARY KEY, name TEXT, specialization TEXT, phone TEXT, email TEXT, availability TEXT, shift TEXT)",
                "CREATE TABLE IF NOT EXISTS appointments (appt_id TEXT PRIMARY KEY, patient_name TEXT, doctor_id TEXT, appt_date TEXT, time_slot TEXT, reason TEXT, status TEXT)",
                "CREATE TABLE IF NOT EXISTS prescriptions (pres_id TEXT PRIMARY KEY, patient_id TEXT, doctor_id TEXT, date TEXT, medicines TEXT, notes TEXT)",
                "CREATE TABLE IF NOT EXISTS wards (bed_id TEXT PRIMARY KEY, ward_name TEXT, bed_number TEXT, status TEXT, patient_id TEXT)"
        };

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            for (String sql : tables) {
                stmt.execute(sql);
            }

            // Default users
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')");
                stmt.execute(
                        "INSERT INTO users (username, password, role) VALUES ('receptionist', 'rec123', 'Receptionist')");
                stmt.execute("INSERT INTO users (username, password, role) VALUES ('doctor', 'doc123', 'Doctor')");
            }

            // Initialize Ward beds if empty
            ResultSet rw = stmt.executeQuery("SELECT count(*) FROM wards");
            if (rw.next() && rw.getInt(1) == 0) {
                String[] wardTypes = { "General", "ICU", "Maternity", "Pediatric", "Emergency" };
                int bedIdCounter = 1;
                for (String w : wardTypes) {
                    for (int i = 1; i <= 10; i++) { // 10 beds per ward
                        stmt.execute("INSERT INTO wards (bed_id, ward_name, bed_number, status, patient_id) " +
                                "VALUES ('B" + String.format("%03d", bedIdCounter++) + "', '" + w + "', '"
                                + w.substring(0, 1) + i + "', 'Available', '')");
                    }
                }
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static String generateId(String tableName, String idCol, String prefix) {
        String query = "SELECT " + idCol + " FROM " + tableName + " ORDER BY " + idCol + " DESC LIMIT 1";
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith(prefix)) {
                    int num = Integer.parseInt(lastId.substring(prefix.length()));
                    return prefix + String.format("%03d", num + 1);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefix + "001";
    }
}
