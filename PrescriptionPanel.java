
// PrescriptionPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;

public class PrescriptionPanel extends JPanel {
    private JTable listTable; // History of prescriptions
    private DefaultTableModel listModel;

    // Form fields
    private JTextField txtSearchPatient;
    private JLabel lblPatientName;
    private String selectedPatientId = "";
    private JComboBox<String> cbDoctor;
    private JTextField txtDate;
    private JTable medTable;
    private DefaultTableModel medModel;
    private JTextArea txtNotes;

    public PrescriptionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(400);
        split.setOpaque(false);

        // Left System: History
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(false);
        historyPanel.add(new JLabel("Prescription History"), BorderLayout.NORTH);

        String[] cols = { "Pres ID", "Patient Name", "Date" };
        listModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        listTable = new JTable(listModel);
        UIHelper.styleTable(listTable);
        listTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && listTable.getSelectedRow() != -1)
                loadPrescriptionDetails();
        });
        historyPanel.add(new JScrollPane(listTable), BorderLayout.CENTER);

        // Right System: New Prescription Form
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topForm = new JPanel(new GridLayout(4, 2, 5, 5));
        topForm.setBackground(Color.WHITE);

        txtSearchPatient = new JTextField();
        JButton btnFind = new JButton("Find Patient");
        btnFind.addActionListener(e -> findPatient());
        JPanel patPanel = new JPanel(new BorderLayout());
        patPanel.setOpaque(false);
        patPanel.add(txtSearchPatient, BorderLayout.CENTER);
        patPanel.add(btnFind, BorderLayout.EAST);

        lblPatientName = new JLabel("Patient: None");
        lblPatientName.setForeground(UIHelper.PRIMARY_COLOR);

        cbDoctor = new JComboBox<>();
        try (Connection c = DatabaseHelper.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT doctor_id, name FROM doctors")) {
            while (rs.next())
                cbDoctor.addItem(rs.getString("doctor_id") + " - " + rs.getString("name"));
        } catch (Exception e) {
        }

        txtDate = new JTextField(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

        topForm.add(new JLabel("Search Patient (ID/Name):"));
        topForm.add(patPanel);
        topForm.add(new JLabel(""));
        topForm.add(lblPatientName);
        topForm.add(new JLabel("Doctor:"));
        topForm.add(cbDoctor);
        topForm.add(new JLabel("Date:"));
        topForm.add(txtDate);

        // Medicines table
        JPanel midForm = new JPanel(new BorderLayout());
        midForm.setOpaque(false);
        midForm.add(new JLabel("Medicines:"), BorderLayout.NORTH);

        String[] mCols = { "Medicine Name", "Dosage", "Frequency", "Days" };
        medModel = new DefaultTableModel(mCols, 0); // Editable for input
        medTable = new JTable(medModel);
        UIHelper.styleTable(medTable);
        medTable.setRowHeight(25);
        midForm.add(new JScrollPane(medTable), BorderLayout.CENTER);

        JPanel medBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        medBtns.setOpaque(false);
        JButton btnAddMed = new JButton("+ Add Row");
        btnAddMed.addActionListener(e -> medModel.addRow(new Object[] { "", "", "", "" }));
        JButton btnRemMed = new JButton("- Remove Row");
        btnRemMed.addActionListener(e -> {
            if (medTable.getSelectedRow() != -1)
                medModel.removeRow(medTable.getSelectedRow());
        });
        medBtns.add(btnAddMed);
        medBtns.add(btnRemMed);
        midForm.add(medBtns, BorderLayout.SOUTH);

        // Notes
        JPanel botForm = new JPanel(new BorderLayout());
        botForm.setOpaque(false);
        botForm.add(new JLabel("Additional Notes:"), BorderLayout.NORTH);
        txtNotes = new JTextArea(4, 20);
        botForm.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton btnSave = UIHelper.createRoundedButton("Save Prescription", UIHelper.SUCCESS_COLOR);
        JButton btnPrint = UIHelper.createRoundedButton("Print Prescription", Color.GRAY);
        JButton btnClear = UIHelper.createRoundedButton("Clear", UIHelper.DANGER_COLOR);

        btnSave.addActionListener(e -> savePrescription());
        btnPrint.addActionListener(e -> printPrescription());
        btnClear.addActionListener(e -> clearForm());
        actions.add(btnSave);
        actions.add(btnPrint);
        actions.add(btnClear);

        formPanel.add(topForm, BorderLayout.NORTH);
        formPanel.add(midForm, BorderLayout.CENTER);

        JPanel botActions = new JPanel(new BorderLayout());
        botActions.setOpaque(false);
        botActions.add(botForm, BorderLayout.CENTER);
        botActions.add(actions, BorderLayout.SOUTH);
        formPanel.add(botActions, BorderLayout.SOUTH);

        split.setLeftComponent(historyPanel);
        split.setRightComponent(formPanel);

        add(split, BorderLayout.CENTER);

        loadHistory();
        medModel.addRow(new Object[] { "", "", "", "" }); // One empty row initially
    }

    private void findPatient() {
        String q = txtSearchPatient.getText().trim();
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT patient_id, name FROM patients WHERE patient_id=? OR name LIKE ?")) {
            ps.setString(1, q);
            ps.setString(2, "%" + q + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                selectedPatientId = rs.getString("patient_id");
                lblPatientName.setText("Patient: " + rs.getString("name") + " (" + selectedPatientId + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Patient not found.");
            }
        } catch (Exception e) {
        }
    }

    private void savePrescription() {
        if (selectedPatientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient.");
            return;
        }

        StringBuilder medicines = new StringBuilder();
        for (int i = 0; i < medModel.getRowCount(); i++) {
            String mName = (String) medModel.getValueAt(i, 0);
            if (mName != null && !mName.trim().isEmpty()) {
                medicines.append(mName).append("|")
                        .append(medModel.getValueAt(i, 1)).append("|")
                        .append(medModel.getValueAt(i, 2)).append("|")
                        .append(medModel.getValueAt(i, 3)).append(";;"); // separator
            }
        }

        try (Connection conn = DatabaseHelper.getConnection()) {
            String newId = DatabaseHelper.generateId("prescriptions", "pres_id", "RX");
            String docId = cbDoctor.getSelectedItem() != null ? cbDoctor.getSelectedItem().toString().split(" ")[0]
                    : "";

            PreparedStatement ps = conn.prepareStatement("INSERT INTO prescriptions VALUES(?,?,?,?,?,?)");
            ps.setString(1, newId);
            ps.setString(2, selectedPatientId);
            ps.setString(3, docId);
            ps.setString(4, txtDate.getText());
            ps.setString(5, medicines.toString());
            ps.setString(6, txtNotes.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Saved as " + newId);
            loadHistory();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void loadHistory() {
        listModel.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT p.pres_id, pat.name, p.date FROM prescriptions p LEFT JOIN patients pat ON p.patient_id = pat.patient_id ORDER BY p.date DESC")) {
            while (rs.next())
                listModel.addRow(new Object[] { rs.getString(1), rs.getString(2), rs.getString(3) });
        } catch (Exception e) {
        }
    }

    private void loadPrescriptionDetails() {
        int row = listTable.getSelectedRow();
        if (row < 0)
            return;
        String pId = listModel.getValueAt(row, 0).toString();

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM prescriptions WHERE pres_id=?")) {
            ps.setString(1, pId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                selectedPatientId = rs.getString("patient_id");

                // Fetch patient name
                try (PreparedStatement pps = conn.prepareStatement("SELECT name FROM patients WHERE patient_id=?")) {
                    pps.setString(1, selectedPatientId);
                    ResultSet prs = pps.executeQuery();
                    if (prs.next())
                        lblPatientName.setText("Patient: " + prs.getString("name") + " (" + selectedPatientId + ")");
                }

                txtDate.setText(rs.getString("date"));
                txtNotes.setText(rs.getString("notes"));

                // Parse medicines
                medModel.setRowCount(0);
                String[] meds = rs.getString("medicines").split(";;");
                for (String m : meds) {
                    if (m.isEmpty())
                        continue;
                    String[] parts = m.split("\\|");
                    medModel.addRow(new Object[] {
                            parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : "",
                            parts.length > 2 ? parts[2] : "", parts.length > 3 ? parts[3] : ""
                    });
                }
            }
        } catch (Exception e) {
        }
    }

    private void clearForm() {
        selectedPatientId = "";
        lblPatientName.setText("Patient: None");
        txtSearchPatient.setText("");
        medModel.setRowCount(0);
        medModel.addRow(new Object[] { "", "", "", "" });
        txtNotes.setText("");
        listTable.clearSelection();
    }

    private void printPrescription() {
        try {
            // Simplified print representation using a JTextArea
            JTextArea printArea = new JTextArea();
            printArea.append("=== CUREOS HOSPITAL PRESCRIPTION ===\n\n");
            printArea.append(lblPatientName.getText() + "\n");
            printArea.append("Date: " + txtDate.getText() + "\n");
            printArea.append("Doctor: "
                    + (cbDoctor.getSelectedItem() != null ? cbDoctor.getSelectedItem().toString() : "") + "\n\n");
            printArea.append("MEDICINES:\n");
            for (int i = 0; i < medModel.getRowCount(); i++) {
                String m = (String) medModel.getValueAt(i, 0);
                if (m != null && !m.trim().isEmpty()) {
                    printArea.append("- " + m + " | Dose: " + medModel.getValueAt(i, 1) +
                            " | Freq: " + medModel.getValueAt(i, 2) + " | Days: " + medModel.getValueAt(i, 3) + "\n");
                }
            }
            printArea.append("\nNOTES:\n" + txtNotes.getText());
            printArea.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
