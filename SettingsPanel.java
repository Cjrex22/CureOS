
// SettingsPanel.java
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("System Settings");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Section: Change Admin Password
        JPanel pwdPanel = UIHelper.createRoundedPanel(Color.WHITE, 15);
        pwdPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pwdPanel.setMaximumSize(new Dimension(800, 70));

        pwdPanel.add(new JLabel("Change Admin Password: "));
        JPasswordField txtPwd = new JPasswordField(15);
        pwdPanel.add(txtPwd);
        JButton btnPwd = UIHelper.createRoundedButton("Update", UIHelper.PRIMARY_COLOR);
        btnPwd.addActionListener(e -> {
            String newPwd = new String(txtPwd.getPassword());
            if (newPwd.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }
            try (Connection conn = DatabaseHelper.getConnection();
                    PreparedStatement ps = conn
                            .prepareStatement("UPDATE users SET password=? WHERE username='admin'")) {
                ps.setString(1, newPwd);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Admin password updated successfully.");
                txtPwd.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        pwdPanel.add(btnPwd);

        // Section: Danger Zone (Clear Data)
        JPanel dangerPanel = UIHelper.createRoundedPanel(Color.WHITE, 15);
        dangerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        dangerPanel.setMaximumSize(new Dimension(800, 70));

        JLabel lblDng = new JLabel("Danger Zone: ");
        lblDng.setForeground(UIHelper.DANGER_COLOR);
        lblDng.setFont(new Font("Arial", Font.BOLD, 14));
        dangerPanel.add(lblDng);

        JButton btnClear = UIHelper.createRoundedButton("Clear All Data", UIHelper.DANGER_COLOR);
        btnClear.addActionListener(e -> clearAllData());
        dangerPanel.add(btnClear);

        // Section: About
        JPanel aboutPanel = UIHelper.createRoundedPanel(Color.WHITE, 15);
        aboutPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        aboutPanel.setMaximumSize(new Dimension(800, 100));

        JLabel lblAbout = new JLabel(
                "<html><b>CureOS Hospital Management System</b><br>Version 1.0.0<br>100% Pure Java Swing Application</html>");
        aboutPanel.add(lblAbout);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(pwdPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(dangerPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(aboutPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void clearAllData() {
        int conf = JOptionPane.showConfirmDialog(this,
                "WARNING: This will permanently delete ALL patients, doctors, appointments, and prescriptions. Continue?",
                "Factory Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            String validation = JOptionPane.showInputDialog(this, "Type 'DELETE' to confirm:");
            if (validation != null && validation.equals("DELETE")) {
                try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM patients");
                    stmt.execute("DELETE FROM doctors");
                    stmt.execute("DELETE FROM appointments");
                    stmt.execute("DELETE FROM prescriptions");
                    stmt.execute("UPDATE wards SET status='Available', patient_id=''");
                    JOptionPane.showMessageDialog(this, "All data has been cleared.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error clearing data: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Operation cancelled.");
            }
        }
    }
}
