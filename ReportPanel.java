
// ReportPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportPanel extends JPanel {
    private JTable listTable;
    private DefaultTableModel listModel;
    private JRadioButton rbPatients;
    private JRadioButton rbDoctors;
    private JRadioButton rbAppts;
    private JRadioButton rbDischarged;

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.decode("#F0F2F5"));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("System Reports");
        lblTitle.setFont(UIHelper.TITLE_FONT);
        topPanel.add(lblTitle, BorderLayout.WEST);

        // Radio Buttons for Selection
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        filterPanel.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        rbPatients = new JRadioButton("Admitted This Month", true);
        rbDoctors = new JRadioButton("Doctors Available");
        rbAppts = new JRadioButton("Appointments");
        rbDischarged = new JRadioButton("Discharged Patients");

        Font rbFont = new Font("Arial", Font.BOLD, 12);
        rbPatients.setFont(rbFont);
        rbDoctors.setFont(rbFont);
        rbAppts.setFont(rbFont);
        rbDischarged.setFont(rbFont);
        rbPatients.setOpaque(false);
        rbDoctors.setOpaque(false);
        rbAppts.setOpaque(false);
        rbDischarged.setOpaque(false);

        bg.add(rbPatients);
        bg.add(rbDoctors);
        bg.add(rbAppts);
        bg.add(rbDischarged);

        filterPanel.add(rbPatients);
        filterPanel.add(rbDoctors);
        filterPanel.add(rbAppts);
        filterPanel.add(rbDischarged);

        JButton btnGenerate = UIHelper.createRoundedButton("Load Report", UIHelper.PRIMARY_COLOR);
        btnGenerate.addActionListener(e -> generateReport());
        filterPanel.add(btnGenerate);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table
        listModel = new DefaultTableModel();
        listTable = new JTable(listModel);
        UIHelper.styleTable(listTable);
        add(new JScrollPane(listTable), BorderLayout.CENTER);

        // Export Actions
        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botPanel.setOpaque(false);
        JButton btnExport = UIHelper.createRoundedButton("Export to TXT", UIHelper.SUCCESS_COLOR);
        btnExport.addActionListener(e -> exportToTxt());
        botPanel.add(btnExport);
        add(botPanel, BorderLayout.SOUTH);

        generateReport(); // load default
    }

    private void generateReport() {
        listModel.setRowCount(0);
        listModel.setColumnCount(0);

        try (Connection conn = DatabaseHelper.getConnection(); Statement stmt = conn.createStatement()) {
            if (rbPatients.isSelected()) {
                listModel.setColumnIdentifiers(new String[] { "ID", "Name", "Date", "Ward", "Disease" });
                String monthStr = new java.text.SimpleDateFormat("yyyy-MM").format(new java.util.Date());
                ResultSet rs = stmt.executeQuery(
                        "SELECT patient_id, name, admission_date, ward, disease FROM patients WHERE status='IPD' AND substr(admission_date, 1, 7) = '"
                                + monthStr + "'");
                while (rs.next())
                    listModel.addRow(new Object[] { rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5) });
            } else if (rbDoctors.isSelected()) {
                listModel.setColumnIdentifiers(new String[] { "ID", "Name", "Specialization", "Shift" });
                ResultSet rs = stmt.executeQuery(
                        "SELECT doctor_id, name, specialization, shift FROM doctors WHERE availability='Available'");
                while (rs.next())
                    listModel.addRow(
                            new Object[] { rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) });
            } else if (rbAppts.isSelected()) {
                listModel.setColumnIdentifiers(new String[] { "Pending/Confirmed", "Done", "Cancelled" });
                // Simple summary mapping
                Object[] row = new Object[] { 0, 0, 0 };
                ResultSet rs = stmt.executeQuery("SELECT status, count(*) FROM appointments GROUP BY status");
                while (rs.next()) {
                    String st = rs.getString(1);
                    int count = rs.getInt(2);
                    if (st.equals("Pending") || st.equals("Confirmed"))
                        row[0] = (int) row[0] + count;
                    else if (st.equals("Done"))
                        row[1] = count;
                    else if (st.equals("Cancelled"))
                        row[2] = count;
                }
                listModel.addRow(row);
            } else if (rbDischarged.isSelected()) {
                listModel.setColumnIdentifiers(new String[] { "ID", "Name", "Disease", "Date admitted" });
                ResultSet rs = stmt.executeQuery(
                        "SELECT patient_id, name, disease, admission_date FROM patients WHERE status='Discharged'");
                while (rs.next())
                    listModel.addRow(
                            new Object[] { rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportToTxt() {
        if (listModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export!");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Report");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".txt"))
                f = new File(f.getAbsolutePath() + ".txt");

            try (FileWriter fw = new FileWriter(f)) {
                fw.write("=== CUREOS REPORT ===\n\n");

                // Headers
                for (int i = 0; i < listModel.getColumnCount(); i++) {
                    fw.write(listModel.getColumnName(i) + "\t|\t");
                }
                fw.write("\n--------------------------------------------------------------\n");

                // Rows
                for (int r = 0; r < listModel.getRowCount(); r++) {
                    for (int c = 0; c < listModel.getColumnCount(); c++) {
                        fw.write(listModel.getValueAt(r, c) + "\t|\t");
                    }
                    fw.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Report exported successfully.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export Error: " + e.getMessage());
            }
        }
    }
}
