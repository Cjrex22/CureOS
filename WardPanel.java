
// WardPanel.java
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class WardPanel extends JPanel {
    private JPanel gridPanel;
    private JComboBox<String> cbWards;

    public WardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Ward & Bed Management");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        cbWards = new JComboBox<>(new String[] { "General", "ICU", "Maternity", "Pediatric", "Emergency" });
        cbWards.addActionListener(e -> loadBeds());

        JButton btnSummary = UIHelper.createRoundedButton("View Ward Summary", UIHelper.PRIMARY_COLOR);
        btnSummary.addActionListener(e -> showSummary());

        filterPanel.add(new JLabel("Select Ward: "));
        filterPanel.add(cbWards);
        filterPanel.add(btnSummary);
        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 5, 15, 15)); // 5 columns
        gridPanel.setBackground(Color.decode("#F0F2F5"));
        add(new JScrollPane(gridPanel), BorderLayout.CENTER);

        loadBeds();
    }

    private void loadBeds() {
        gridPanel.removeAll();
        String currentWard = cbWards.getSelectedItem().toString();

        try (Connection conn = DatabaseHelper.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM wards WHERE ward_name=? ORDER BY bed_id")) {
            ps.setString(1, currentWard);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String bedId = rs.getString("bed_id");
                String bedNo = rs.getString("bed_number");
                String status = rs.getString("status");
                String patientId = rs.getString("patient_id");

                JPanel bedBox = UIHelper.createRoundedPanel(
                        status.equals("Available") ? UIHelper.SUCCESS_COLOR
                                : (status.equals("Occupied") ? UIHelper.DANGER_COLOR : UIHelper.WARNING_COLOR),
                        15);
                bedBox.setPreferredSize(new Dimension(150, 100));
                bedBox.setLayout(new BoxLayout(bedBox, BoxLayout.Y_AXIS));
                bedBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                bedBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

                JLabel lblBed = new JLabel("Bed: " + bedNo);
                lblBed.setFont(UIHelper.BOLD_FONT);
                lblBed.setForeground(Color.WHITE);
                lblBed.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblStat = new JLabel(status);
                lblStat.setForeground(Color.WHITE);
                lblStat.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblPat = new JLabel(patientId.isEmpty() ? "Empty" : "Patient: " + patientId);
                lblPat.setForeground(Color.WHITE);
                lblPat.setAlignmentX(Component.CENTER_ALIGNMENT);

                bedBox.add(Box.createVerticalGlue());
                bedBox.add(lblBed);
                bedBox.add(Box.createRigidArea(new Dimension(0, 5)));
                bedBox.add(lblStat);
                bedBox.add(Box.createRigidArea(new Dimension(0, 5)));
                bedBox.add(lblPat);
                bedBox.add(Box.createVerticalGlue());

                bedBox.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        manageBed(bedId, bedNo, status, patientId);
                    }
                });

                gridPanel.add(bedBox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void manageBed(String bedId, String bedNo, String status, String patientId) {
        String[] options;
        if (status.equals("Available")) {
            options = new String[] { "Assign Bed", "Reserve", "Cancel" };
        } else if (status.equals("Reserved")) {
            options = new String[] { "Assign Bed", "Make Available", "Cancel" };
        } else {
            options = new String[] { "Release Bed", "Cancel" };
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Manage Bed " + bedNo + "\nStatus: " + status + "\nPatient: "
                        + (patientId.isEmpty() ? "None" : patientId),
                "Bed Options", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice < 0 || options[choice].equals("Cancel"))
            return;

        String action = options[choice];
        try (Connection conn = DatabaseHelper.getConnection()) {
            if (action.equals("Assign Bed")) {
                String pId = JOptionPane.showInputDialog(this, "Enter Patient ID:");
                if (pId != null && !pId.trim().isEmpty()) {
                    PreparedStatement ps = conn
                            .prepareStatement("UPDATE wards SET status='Occupied', patient_id=? WHERE bed_id=?");
                    ps.setString(1, pId);
                    ps.setString(2, bedId);
                    ps.executeUpdate();

                    // Also update patient record
                    PreparedStatement ps2 = conn
                            .prepareStatement("UPDATE patients SET ward=?, status='IPD' WHERE patient_id=?");
                    ps2.setString(1, cbWards.getSelectedItem().toString() + "-" + bedNo);
                    ps2.setString(2, pId);
                    ps2.executeUpdate();
                }
            } else if (action.equals("Release Bed") || action.equals("Make Available")) {
                PreparedStatement ps = conn
                        .prepareStatement("UPDATE wards SET status='Available', patient_id='' WHERE bed_id=?");
                ps.setString(1, bedId);
                ps.executeUpdate();
                if (!patientId.isEmpty()) {
                    PreparedStatement ps2 = conn
                            .prepareStatement("UPDATE patients SET status='Discharged' WHERE patient_id=?");
                    ps2.setString(1, patientId);
                    ps2.executeUpdate();
                }
            } else if (action.equals("Reserve")) {
                PreparedStatement ps = conn.prepareStatement("UPDATE wards SET status='Reserved' WHERE bed_id=?");
                ps.setString(1, bedId);
                ps.executeUpdate();
            }
            loadBeds();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showSummary() {
        StringBuilder summary = new StringBuilder("Ward Summary:\n\n");
        try (Connection c = DatabaseHelper.getConnection();
                Statement s = c.createStatement();
                ResultSet rs = s
                        .executeQuery("SELECT ward_name, status, count(*) FROM wards GROUP BY ward_name, status")) {
            while (rs.next()) {
                summary.append(rs.getString(1)).append(" - ").append(rs.getString(2)).append(": ").append(rs.getInt(3))
                        .append("\n");
            }
        } catch (Exception e) {
        }
        JOptionPane.showMessageDialog(this, summary.toString(), "Ward Summary", JOptionPane.INFORMATION_MESSAGE);
    }
}
