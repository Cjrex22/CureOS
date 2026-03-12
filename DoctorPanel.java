
// DoctorPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DoctorPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    public DoctorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Doctor Management");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actPanel.setOpaque(false);

        txtSearch = new JTextField(15);
        JButton btnSearch = UIHelper.createRoundedButton("Search", UIHelper.PRIMARY_COLOR);
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));

        JButton btnAdd = UIHelper.createRoundedButton("Add Doctor", UIHelper.SUCCESS_COLOR);
        btnAdd.addActionListener(e -> openDialog(null));

        actPanel.add(new JLabel("Search: "));
        actPanel.add(txtSearch);
        actPanel.add(btnSearch);
        actPanel.add(btnAdd);

        topPanel.add(actPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = { "Doctor ID", "Name", "Specialization", "Phone", "Email", "Availability", "Shift" };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        JButton btnEdit = UIHelper.createRoundedButton("Edit", UIHelper.WARNING_COLOR);
        JButton btnDelete = UIHelper.createRoundedButton("Delete", UIHelper.DANGER_COLOR);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0)
                openDialog(model.getValueAt(row, 0).toString());
            else
                JOptionPane.showMessageDialog(this, "Select a doctor to edit.");
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String id = model.getValueAt(row, 0).toString();
                if (JOptionPane.showConfirmDialog(this, "Delete " + id + "?", "Confirm",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try (Connection conn = DatabaseHelper.getConnection();
                            PreparedStatement ps = conn.prepareStatement("DELETE FROM doctors WHERE doctor_id=?")) {
                        ps.setString(1, id);
                        ps.executeUpdate();
                        loadData("");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else
                JOptionPane.showMessageDialog(this, "Select a doctor to delete.");
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);

        loadData("");
    }

    private void loadData(String query) {
        model.setRowCount(0);
        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM doctors WHERE name LIKE ? OR doctor_id LIKE ? OR specialization LIKE ?")) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getString("doctor_id"), rs.getString("name"), rs.getString("specialization"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("availability"),
                        rs.getString("shift")
                });
            }
        } catch (Exception e) {
        }
    }

    private void openDialog(String id) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                id == null ? "Add Doctor" : "Edit Doctor", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtName = new JTextField();
        JTextField txtSpec = new JTextField();
        JTextField txtPhone = new JTextField();
        JTextField txtEmail = new JTextField();
        JComboBox<String> cbAvail = new JComboBox<>(new String[] { "Available", "On Leave", "In Surgery" });
        JComboBox<String> cbShift = new JComboBox<>(new String[] { "Morning", "Evening", "Night" });

        form.add(new JLabel("Name:"));
        form.add(txtName);
        form.add(new JLabel("Specialization:"));
        form.add(txtSpec);
        form.add(new JLabel("Phone:"));
        form.add(txtPhone);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Availability:"));
        form.add(cbAvail);
        form.add(new JLabel("Shift:"));
        form.add(cbShift);

        if (id != null) {
            try (Connection c = DatabaseHelper.getConnection();
                    PreparedStatement ps = c.prepareStatement("SELECT * FROM doctors WHERE doctor_id=?")) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtName.setText(rs.getString("name"));
                    txtSpec.setText(rs.getString("specialization"));
                    txtPhone.setText(rs.getString("phone"));
                    txtEmail.setText(rs.getString("email"));
                    cbAvail.setSelectedItem(rs.getString("availability"));
                    cbShift.setSelectedItem(rs.getString("shift"));
                }
            } catch (Exception e) {
            }
        }

        JPanel btns = new JPanel();
        JButton btnSave = UIHelper.createRoundedButton("Save", UIHelper.SUCCESS_COLOR);
        JButton btnCancel = UIHelper.createRoundedButton("Cancel", UIHelper.DANGER_COLOR);

        btnSave.addActionListener(e -> {
            try (Connection c = DatabaseHelper.getConnection()) {
                if (id == null) {
                    String newId = DatabaseHelper.generateId("doctors", "doctor_id", "D");
                    PreparedStatement ps = c.prepareStatement("INSERT INTO doctors VALUES(?,?,?,?,?,?,?)");
                    ps.setString(1, newId);
                    ps.setString(2, txtName.getText());
                    ps.setString(3, txtSpec.getText());
                    ps.setString(4, txtPhone.getText());
                    ps.setString(5, txtEmail.getText());
                    ps.setString(6, cbAvail.getSelectedItem().toString());
                    ps.setString(7, cbShift.getSelectedItem().toString());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = c.prepareStatement(
                            "UPDATE doctors SET name=?, specialization=?, phone=?, email=?, availability=?, shift=? WHERE doctor_id=?");
                    ps.setString(1, txtName.getText());
                    ps.setString(2, txtSpec.getText());
                    ps.setString(3, txtPhone.getText());
                    ps.setString(4, txtEmail.getText());
                    ps.setString(5, cbAvail.getSelectedItem().toString());
                    ps.setString(6, cbShift.getSelectedItem().toString());
                    ps.setString(7, id);
                    ps.executeUpdate();
                }
                dialog.dispose();
                loadData("");
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
