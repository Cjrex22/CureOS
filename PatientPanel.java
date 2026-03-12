
// PatientPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PatientPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public PatientPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        // Top components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Patient Management");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actPanel.setOpaque(false);

        txtSearch = new JTextField(15);
        JButton btnSearch = UIHelper.createRoundedButton("Search", UIHelper.PRIMARY_COLOR);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));

        JButton btnAdd = UIHelper.createRoundedButton("Add New Patient", UIHelper.SUCCESS_COLOR);
        btnAdd.addActionListener(e -> openPatientDialog(null));

        actPanel.add(new JLabel("Search: "));
        actPanel.add(txtSearch);
        actPanel.add(btnSearch);
        actPanel.add(btnAdd);

        topPanel.add(actPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = { "Patient ID", "Full Name", "Age", "Gender", "Blood Group", "Contact", "Disease",
                "Doctor Assigned", "Admission Date", "Ward No", "Status" };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnEdit = UIHelper.createRoundedButton("Edit", UIHelper.WARNING_COLOR);
        JButton btnDelete = UIHelper.createRoundedButton("Delete", UIHelper.DANGER_COLOR);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = model.getValueAt(row, 0).toString();
                openPatientDialog(id);
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to edit.");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = model.getValueAt(row, 0).toString();
                int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete patient " + id + "?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    deletePatient(id);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Select a patient to delete.");
            }
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData("");
    }

    private void loadData(String search) {
        model.setRowCount(0);
        String q = "SELECT * FROM patients WHERE name LIKE ? OR patient_id LIKE ? OR disease LIKE ?";
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ps.setString(3, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("patient_id"), rs.getString("name"), rs.getInt("age"),
                        rs.getString("gender"), rs.getString("blood_group"), rs.getString("contact"),
                        rs.getString("disease"), rs.getString("doctor_id"), rs.getString("admission_date"),
                        rs.getString("ward"), rs.getString("status") // combined ward/status
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePatient(String id) {
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM patients WHERE patient_id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            loadData("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPatientDialog(String id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                id == null ? "Add Patient" : "Edit Patient", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(10, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[] { "Male", "Female", "Other" });
        JComboBox<String> cbBlood = new JComboBox<>(new String[] { "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-" });
        JTextField txtContact = new JTextField();
        JTextField txtDisease = new JTextField();
        JComboBox<String> cbDoctor = new JComboBox<>();

        // Year Month Day Date Picker components as requested
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

        // load doctors
        cbDoctor.addItem("None");
        try (Connection c = DatabaseHelper.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT doctor_id, name FROM doctors")) {
            while (rs.next())
                cbDoctor.addItem(rs.getString("doctor_id") + " - " + rs.getString("name"));
        } catch (Exception e) {
        }

        JComboBox<String> cbWard = new JComboBox<>(
                new String[] { "None", "General", "ICU", "Maternity", "Pediatric", "Emergency" });
        JComboBox<String> cbStatus = new JComboBox<>(new String[] { "OPD", "IPD", "Discharged" });

        form.add(new JLabel("Name:"));
        form.add(txtName);
        form.add(new JLabel("Age:"));
        form.add(txtAge);
        form.add(new JLabel("Gender:"));
        form.add(cbGender);
        form.add(new JLabel("Blood Group:"));
        form.add(cbBlood);
        form.add(new JLabel("Contact:"));
        form.add(txtContact);
        form.add(new JLabel("Disease/Diagnosis:"));
        form.add(txtDisease);
        form.add(new JLabel("Doctor Assigned:"));
        form.add(cbDoctor);
        form.add(new JLabel("Admission Date:"));
        form.add(datePanel);
        form.add(new JLabel("Ward:"));
        form.add(cbWard);
        form.add(new JLabel("Status:"));
        form.add(cbStatus);

        if (id != null) {
            try (Connection c = DatabaseHelper.getConnection();
                    PreparedStatement ps = c.prepareStatement("SELECT * FROM patients WHERE patient_id=?")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtName.setText(rs.getString("name"));
                    txtAge.setText(String.valueOf(rs.getInt("age")));
                    cbGender.setSelectedItem(rs.getString("gender"));
                    cbBlood.setSelectedItem(rs.getString("blood_group"));
                    txtContact.setText(rs.getString("contact"));
                    txtDisease.setText(rs.getString("disease"));

                    String dDate = rs.getString("admission_date");
                    if (dDate != null && dDate.length() == 10) {
                        cbYear.setSelectedItem(dDate.substring(0, 4));
                        cbMon.setSelectedItem(dDate.substring(5, 7));
                        cbDay.setSelectedItem(dDate.substring(8, 10));
                    }

                    cbWard.setSelectedItem(rs.getString("ward"));
                    cbStatus.setSelectedItem(rs.getString("status"));
                }
            } catch (Exception e) {
            }
        }

        JPanel btns = new JPanel();
        JButton btnSave = UIHelper.createRoundedButton("Save", UIHelper.SUCCESS_COLOR);
        JButton btnCancel = UIHelper.createRoundedButton("Cancel", UIHelper.DANGER_COLOR);

        btnSave.addActionListener(e -> {
            try (Connection c = DatabaseHelper.getConnection()) {
                String docId = (cbDoctor.getSelectedItem() != null && !cbDoctor.getSelectedItem().equals("None"))
                        ? cbDoctor.getSelectedItem().toString().split(" ")[0]
                        : "";
                String dDate = cbYear.getSelectedItem() + "-" + cbMon.getSelectedItem() + "-" + cbDay.getSelectedItem();

                if (id == null) {
                    String newId = DatabaseHelper.generateId("patients", "patient_id", "P");
                    PreparedStatement ps = c.prepareStatement("INSERT INTO patients VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                    ps.setString(1, newId);
                    ps.setString(2, txtName.getText());
                    ps.setInt(3, Integer.parseInt(txtAge.getText().isEmpty() ? "0" : txtAge.getText()));
                    ps.setString(4, cbGender.getSelectedItem().toString());
                    ps.setString(5, cbBlood.getSelectedItem().toString());
                    ps.setString(6, txtContact.getText());
                    ps.setString(7, txtDisease.getText());
                    ps.setString(8, docId);
                    ps.setString(9, dDate);
                    ps.setString(10, cbWard.getSelectedItem().toString());
                    ps.setString(11, ""); // bed no
                    ps.setString(12, cbStatus.getSelectedItem().toString());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE patients SET name=?, age=?, gender=?, blood_group=?, contact=?, disease=?, doctor_id=?, admission_date=?, ward=?, status=? WHERE patient_id=?");
                    ps.setString(1, txtName.getText());
                    ps.setInt(2, Integer.parseInt(txtAge.getText().isEmpty() ? "0" : txtAge.getText()));
                    ps.setString(3, cbGender.getSelectedItem().toString());
                    ps.setString(4, cbBlood.getSelectedItem().toString());
                    ps.setString(5, txtContact.getText());
                    ps.setString(6, txtDisease.getText());
                    ps.setString(7, docId);
                    ps.setString(8, dDate);
                    ps.setString(9, cbWard.getSelectedItem().toString());
                    ps.setString(10, cbStatus.getSelectedItem().toString());
                    ps.setString(11, id);
                    ps.executeUpdate();
                }
                dialog.dispose();
                loadData("");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving: " + ex.getMessage());
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
