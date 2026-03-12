
// DashboardFrame.java
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DashboardFrame extends JFrame {
    private String username;
    private String role;
    private JPanel centerPanel;
    private CardLayout cardLayout;
    private JLabel lblClock;

    // Components
    private PatientPanel patientPanel;
    private DoctorPanel doctorPanel;
    private AppointmentPanel appointmentPanel;
    private WardPanel wardPanel;
    private PrescriptionPanel prescriptionPanel;
    private ReportPanel reportPanel;
    private SettingsPanel settingsPanel;

    public DashboardFrame(String username, String role) {
        this.username = username;
        this.role = role;

        setTitle("CureOS — Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initTopBar();
        initCenterPanel(); // Must init center before sidebar to ensure views exist
        initSidebar();

        // Start Clock
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();
    }

    private void initTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIHelper.PRIMARY_COLOR);
        topBar.setPreferredSize(new Dimension(getWidth(), 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                UIHelper.drawLogo((Graphics2D) g, 0, 0, 40);
            }
        };
        logoPanel.setPreferredSize(new Dimension(40, 40));
        logoPanel.setOpaque(false);

        // Fetch hospital name from DB settings (if table existed), else hardcoded
        JLabel lblTitle = new JLabel("CureOS Hospital");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        leftPanel.add(logoPanel);
        leftPanel.add(lblTitle);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        rightPanel.setOpaque(false);

        lblClock = new JLabel();
        lblClock.setFont(UIHelper.MAIN_FONT);
        lblClock.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel("Welcome, " + username + " (" + role + ")");
        lblUser.setFont(UIHelper.MAIN_FONT);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UIHelper.MAIN_FONT);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(UIHelper.DANGER_COLOR);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        rightPanel.add(lblClock);
        rightPanel.add(lblUser);
        rightPanel.add(btnLogout);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
    }

    private void initSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIHelper.SECONDARY_COLOR);
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] menus = { "Dashboard", "Patients", "Doctors", "Appointments", "Wards", "Prescriptions", "Reports",
                "Settings" };

        for (String menu : menus) {
            JButton btn = new JButton(menu);
            btn.setFont(UIHelper.BOLD_FONT);
            btn.setMaximumSize(new Dimension(180, 45));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setBackground(UIHelper.SECONDARY_COLOR);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E0E0E0")),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.LEFT);

            btn.addActionListener(e -> {
                if (menu.equals("Dashboard")) {
                    centerPanel.add(createHomePanel(), "DashboardRe");
                    cardLayout.show(centerPanel, "DashboardRe");
                } else {
                    cardLayout.show(centerPanel, menu);
                }
            });
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(sidebar, BorderLayout.WEST);
    }

    private void initCenterPanel() {
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBackground(Color.decode("#F0F2F5"));

        // initialize panels
        patientPanel = new PatientPanel();
        doctorPanel = new DoctorPanel();
        appointmentPanel = new AppointmentPanel();
        wardPanel = new WardPanel();
        prescriptionPanel = new PrescriptionPanel();
        reportPanel = new ReportPanel();
        settingsPanel = new SettingsPanel();

        centerPanel.add(createHomePanel(), "Dashboard");
        centerPanel.add(patientPanel, "Patients");
        centerPanel.add(doctorPanel, "Doctors");
        centerPanel.add(appointmentPanel, "Appointments");
        centerPanel.add(wardPanel, "Wards");
        centerPanel.add(prescriptionPanel, "Prescriptions");
        centerPanel.add(reportPanel, "Reports");
        centerPanel.add(settingsPanel, "Settings");

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setBackground(Color.decode("#F0F2F5"));

        String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date());

        panel.add(createSummaryCard("Total Patients Today",
                getCount("SELECT count(*) FROM patients WHERE admission_date='" + today + "'"),
                UIHelper.PRIMARY_COLOR));
        panel.add(createSummaryCard("Total Doctors Available",
                getCount("SELECT count(*) FROM doctors WHERE availability='Available'"), UIHelper.SUCCESS_COLOR));
        panel.add(createSummaryCard("Pending Appointments",
                getCount("SELECT count(*) FROM appointments WHERE status='Pending'"), UIHelper.WARNING_COLOR));
        panel.add(createSummaryCard("Occupied Beds", getCount("SELECT count(*) FROM wards WHERE status='Occupied'"),
                UIHelper.DANGER_COLOR));

        return panel;
    }

    private JPanel createSummaryCard(String title, int count, Color color) {
        JPanel card = UIHelper.createRoundedPanel(color, 20);
        card.setPreferredSize(new Dimension(230, 130));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblCount = new JLabel(String.valueOf(count));
        lblCount.setFont(new Font("Arial", Font.BOLD, 42));
        lblCount.setForeground(Color.WHITE);
        lblCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(lblCount);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblTitle);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private int getCount(String query) {
        try (Connection conn = DatabaseHelper.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
        if (lblClock != null)
            lblClock.setText(sdf.format(new Date()));
    }
}
