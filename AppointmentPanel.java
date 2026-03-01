
// AppointmentPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.util.ArrayList;

public class AppointmentPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbFilterMonth;
    private JComboBox<String> cbFilterDoctor;
    private JComboBox<String> cbFilterStatus;

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Appointments");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        String[] months = { "All", "01-Jan", "02-Feb", "03-Mar", "04-Apr", "05-May", "06-Jun",
                "07-Jul", "08-Aug", "09-Sep", "10-Oct", "11-Nov", "12-Dec" };
        cbFilterMonth = new JComboBox<>(months);

        cbFilterDoctor = new JComboBox<>();
        cbFilterDoctor.addItem("All");
        cbFilterStatus = new JComboBox<>(new String[] { "All", "Pending", "Confirmed", "Done", "Cancelled" });

        try (Connection c = DatabaseHelper.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT name FROM doctors")) {
            while (rs.next())
                cbFilterDoctor.addItem(rs.getString("name"));
        } catch (Exception e) {
        }

        cbFilterMonth.addActionListener(e -> loadData());
        cbFilterDoctor.addActionListener(e -> loadData());
        cbFilterStatus.addActionListener(e -> loadData());

        filterPanel.add(new JLabel("Month:"));
        filterPanel.add(cbFilterMonth);
        filterPanel.add(new JLabel("Doctor:"));
        filterPanel.add(cbFilterDoctor);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(cbFilterStatus);

        JButton btnNew = UIHelper.createRoundedButton("New Appointment", UIHelper.SUCCESS_COLOR);
        btnNew.addActionListener(e -> openDialog(null));
        filterPanel.add(btnNew);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = { "Appt ID", "Patient Name", "Doctor Name", "Date", "Time Slot", "Reason", "Status" };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);

        JButton btnConfirm = UIHelper.createRoundedButton("Confirm", UIHelper.PRIMARY_COLOR);
        JButton btnDone = UIHelper.createRoundedButton("Mark Done", UIHelper.SUCCESS_COLOR);
        JButton btnCancel = UIHelper.createRoundedButton("Cancel", UIHelper.DANGER_COLOR);
        JButton btnPrint = UIHelper.createRoundedButton("Print", Color.GRAY);

        btnConfirm.addActionListener(e -> updateStatus("Confirmed"));
        btnDone.addActionListener(e -> updateStatus("Done"));
        btnCancel.addActionListener(e -> updateStatus("Cancelled"));
        btnPrint.addActionListener(e -> {
            try {
                table.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        });

        bottomPanel.add(btnConfirm);
        bottomPanel.add(btnDone);
        bottomPanel.add(btnCancel);
        bottomPanel.add(btnPrint);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        if (model == null)
            return;
        model.setRowCount(0);

        String q = "SELECT a.*, d.name as doctor_name FROM appointments a LEFT JOIN doctors d ON a.doctor_id = d.doctor_id WHERE 1=1";

        String month = cbFilterMonth.getSelectedItem().toString();
        if (!month.equals("All")) {
            String mStr = month.substring(0, 2);
            q += " AND substr(a.appt_date, 6, 2) = '" + mStr + "'";
        }

        String doc = cbFilterDoctor.getSelectedItem().toString();
        if (!doc.equals("All")) {
            q += " AND d.name = '" + doc.replace("'", "''") + "'";
        }

        String st = cbFilterStatus.getSelectedItem().toString();
        if (!st.equals("All")) {
            q += " AND a.status = '" + st + "'";
        }

        q += " ORDER BY a.appt_date DESC, a.time_slot DESC";

        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(q)) {
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("appt_id"), rs.getString("patient_name"), rs.getString("doctor_name"),
                        rs.getString("appt_date"), rs.getString("time_slot"), rs.getString("reason"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = model.getValueAt(row, 0).toString();
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement ps = conn.prepareStatement("UPDATE appointments SET status=? WHERE appt_id=?")) {
                ps.setString(1, status);
                ps.setString(2, id);
                ps.executeUpdate();
                loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select an appointment first.");
        }
    }

    private void openDialog(String id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Appointment", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtPatient = new JTextField();
        JComboBox<String> cbDoctor = new JComboBox<>();
        try (Connection c = DatabaseHelper.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT doctor_id, name FROM doctors")) {
            while (rs.next())
                cbDoctor.addItem(rs.getString("doctor_id") + " - " + rs.getString("name"));
        } catch (Exception e) {
        }

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        String[] days = new String[31];
        for (int i = 1; i <= 31; i++)
            days[i - 1] = String.format("%02d", i);
        String[] months = { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" };
        String[] years = { "2024", "2025", "2026", "2027", "2028" };
        JComboBox<String> cbDay = new JComboBox<>(days);
        JComboBox<String> cbMon = new JComboBox<>(months);
        JComboBox<String> cbYear = new JComboBox<>(years);
        datePanel.add(cbYear);
        datePanel.add(new JLabel("-"));
        datePanel.add(cbMon);
        datePanel.add(new JLabel("-"));
        datePanel.add(cbDay);

        ArrayList<String> slots = new ArrayList<>();
        for (int h = 9; h <= 18; h++) {
            slots.add((h > 12 ? h - 12 : h) + ":00" + (h >= 12 ? "PM" : "AM"));
            if (h != 18)
                slots.add((h > 12 ? h - 12 : h) + ":30" + (h >= 12 ? "PM" : "AM"));
        }
        JComboBox<String> cbTime = new JComboBox<>(slots.toArray(new String[0]));
        JTextField txtReason = new JTextField();

        form.add(new JLabel("Patient Name:"));
        form.add(txtPatient);
        form.add(new JLabel("Doctor:"));
        form.add(cbDoctor);
        form.add(new JLabel("Date:"));
        form.add(datePanel);
        form.add(new JLabel("Time Slot:"));
        form.add(cbTime);
        form.add(new JLabel("Reason:"));
        form.add(txtReason);

        JPanel btns = new JPanel();
        JButton btnSave = UIHelper.createRoundedButton("Save", UIHelper.SUCCESS_COLOR);
        JButton btnCancel = UIHelper.createRoundedButton("Cancel", UIHelper.DANGER_COLOR);

        btnSave.addActionListener(e -> {
            try (Connection c = DatabaseHelper.getConnection()) {
                String docId = cbDoctor.getSelectedItem() != null ? cbDoctor.getSelectedItem().toString().split(" ")[0]
                        : "";
                String dDate = cbYear.getSelectedItem() + "-" + cbMon.getSelectedItem() + "-" + cbDay.getSelectedItem();
                String newId = DatabaseHelper.generateId("appointments", "appt_id", "A");

                PreparedStatement ps = c.prepareStatement("INSERT INTO appointments VALUES(?,?,?,?,?,?,?)");
                ps.setString(1, newId);
                ps.setString(2, txtPatient.getText());
                ps.setString(3, docId);
                ps.setString(4, dDate);
                ps.setString(5, cbTime.getSelectedItem().toString());
                ps.setString(6, txtReason.getText());
                ps.setString(7, "Pending");
                ps.executeUpdate();

                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        btnCancel.addActionListener(e -> dialog.dispose());

        btns.add(btnSave);
        btns.add(btnCancel);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
