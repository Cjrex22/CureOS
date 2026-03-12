// UIHelper.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIHelper {
    public static final Color PRIMARY_COLOR = Color.decode("#1A73E8");
    public static final Color SECONDARY_COLOR = Color.decode("#FFFFFF");
    public static final Color ACCENT_COLOR = Color.decode("#E8F0FE");
    public static final Color SUCCESS_COLOR = Color.decode("#34A853");
    public static final Color DANGER_COLOR = Color.decode("#EA4335");
    public static final Color WARNING_COLOR = Color.decode("#FBBC04");
    
    public static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 18);

    public static JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setFont(MAIN_FONT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(MAIN_FONT);
        table.setRowHeight(30);
        table.setSelectionBackground(ACCENT_COLOR);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Alternating row renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? SECONDARY_COLOR : Color.decode("#F8F9FA"));
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static JPanel createRoundedPanel(Color bg, int radius) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
            }
        };
    }

    public static void drawLogo(Graphics2D g2d, int x, int y, int size) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(x, y, size, size, Math.max(10, size / 5), Math.max(10, size / 5));
        g2d.setColor(DANGER_COLOR);
        
        int thickness = size / 4;
        int length = size - (size / 3);
        int offset = (size - length) / 2;
        int center = (size - thickness) / 2;
        
        g2d.fillRect(x + center, y + offset, thickness, length);
        g2d.fillRect(x + offset, y + center, length, thickness);
    }
}
