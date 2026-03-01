
// LoginFrame.java
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // Login Form fields
    private JTextField txtUserLogin;
    private JPasswordField txtPassLogin;
    private JComboBox<String> cbRoleLogin;

    // Signup Form fields
    private JTextField txtUserSignup;
    private JPasswordField txtPassSignup;
    private JPasswordField txtPassConfirmSignup;
    private JComboBox<String> cbRoleSignup;

    public LoginFrame() {
        setTitle("CureOS — Hospital Management System");
        setSize(400, 600); // Increased height for signup form
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Draw logo in the center top
                UIHelper.drawLogo(g2, (getWidth() - 60) / 2, 20, 60);
            }
        };
        headerPanel.setPreferredSize(new Dimension(400, 100));
        headerPanel.setBackground(UIHelper.PRIMARY_COLOR);
        add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createSignupPanel(), "Signup");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "Login");
    }

    private JPanel createLoginPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        formPanel.setBackground(UIHelper.SECONDARY_COLOR);

        JLabel lblTitle = new JLabel("CureOS Login");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UIHelper.MAIN_FONT);
        txtUserLogin = new JTextField();
        txtUserLogin.setMaximumSize(new Dimension(300, 35));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UIHelper.MAIN_FONT);
        txtPassLogin = new JPasswordField();
        txtPassLogin.setMaximumSize(new Dimension(300, 35));

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(UIHelper.MAIN_FONT);
        cbRoleLogin = new JComboBox<>(new String[] { "Admin", "Receptionist", "Doctor" });
        cbRoleLogin.setMaximumSize(new Dimension(300, 35));
        cbRoleLogin.setBackground(UIHelper.SECONDARY_COLOR);

        formPanel.add(lblTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(leftAlign(lblUser));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtUserLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(leftAlign(lblPass));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtPassLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(leftAlign(lblRole));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(cbRoleLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton btnLogin = UIHelper.createRoundedButton("LOGIN", UIHelper.PRIMARY_COLOR);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(300, 40));
        btnLogin.addActionListener(e -> attemptLogin());

        JButton btnGoToSignup = UIHelper.createRoundedButton("Create Account", UIHelper.WARNING_COLOR);
        btnGoToSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoToSignup.setMaximumSize(new Dimension(300, 40));
        btnGoToSignup.addActionListener(e -> cardLayout.show(mainPanel, "Signup"));

        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(btnGoToSignup);

        return formPanel;
    }

    private JPanel createSignupPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        formPanel.setBackground(UIHelper.SECONDARY_COLOR);

        JLabel lblTitle = new JLabel("Create Account");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UIHelper.MAIN_FONT);
        txtUserSignup = new JTextField();
        txtUserSignup.setMaximumSize(new Dimension(300, 35));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UIHelper.MAIN_FONT);
        txtPassSignup = new JPasswordField();
        txtPassSignup.setMaximumSize(new Dimension(300, 35));

        JLabel lblPassConfirm = new JLabel("Confirm Password:");
        lblPassConfirm.setFont(UIHelper.MAIN_FONT);
        txtPassConfirmSignup = new JPasswordField();
        txtPassConfirmSignup.setMaximumSize(new Dimension(300, 35));

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(UIHelper.MAIN_FONT);
        cbRoleSignup = new JComboBox<>(new String[] { "Admin", "Receptionist", "Doctor" });
        cbRoleSignup.setMaximumSize(new Dimension(300, 35));
        cbRoleSignup.setBackground(UIHelper.SECONDARY_COLOR);

        formPanel.add(lblTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(leftAlign(lblUser));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtUserSignup);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(leftAlign(lblPass));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtPassSignup);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(leftAlign(lblPassConfirm));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtPassConfirmSignup);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(leftAlign(lblRole));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(cbRoleSignup);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnSignup = UIHelper.createRoundedButton("SIGN UP", UIHelper.SUCCESS_COLOR);
        btnSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignup.setMaximumSize(new Dimension(300, 40));
        btnSignup.addActionListener(e -> attemptSignup());

        JButton btnBackToLogin = UIHelper.createRoundedButton("Back to Login", Color.GRAY);
        btnBackToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackToLogin.setMaximumSize(new Dimension(300, 40));
        btnBackToLogin.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        formPanel.add(btnSignup);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(btnBackToLogin);

        return formPanel;
    }

    private Component leftAlign(Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(UIHelper.SECONDARY_COLOR);
        p.add(c);
        p.setMaximumSize(new Dimension(300, 25));
        return p;
    }

    private void attemptLogin() {
        String user = txtUserLogin.getText().trim();
        String pass = new String(txtPassLogin.getPassword());
        String role = cbRoleLogin.getSelectedItem().toString();

        try {
            Connection conn = DatabaseHelper.getConnection();
            PreparedStatement ps = conn
                    .prepareStatement("SELECT * FROM users WHERE username=? AND password=? AND role=?");
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                new DashboardFrame(user, role).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptSignup() {
        String user = txtUserSignup.getText().trim();
        String pass = new String(txtPassSignup.getPassword());
        String confirmPass = new String(txtPassConfirmSignup.getPassword());
        String role = cbRoleSignup.getSelectedItem().toString();

        if (user.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseHelper.getConnection();

            // Check if user already exists
            PreparedStatement checkPs = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            checkPs.setString(1, user);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                rs.close();
                checkPs.close();
                return;
            }
            rs.close();
            checkPs.close();

            // Insert new user
            PreparedStatement insertPs = conn
                    .prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
            insertPs.setString(1, user);
            insertPs.setString(2, pass);
            insertPs.setString(3, role);
            insertPs.executeUpdate();
            insertPs.close();

            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Switch back to login view and prefill username & role
            txtUserLogin.setText(user);
            txtPassLogin.setText("");
            cbRoleLogin.setSelectedItem(role);
            cardLayout.show(mainPanel, "Login");

            // Clear signup fields
            txtUserSignup.setText("");
            txtPassSignup.setText("");
            txtPassConfirmSignup.setText("");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
