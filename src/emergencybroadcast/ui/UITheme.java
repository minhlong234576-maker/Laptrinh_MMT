package emergencybroadcast.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme - Lớp tiện ích quản lý tập trung giao diện cho toàn bộ ứng dụng.
 * Cung cấp: Bảng màu gradient, rounded panel, nút hiện đại, bảng đẹp mắt, v.v.
 */
public class UITheme {

    // ============================================
    //  BẢNG MÀU CHÍNH (Dark Premium Palette)
    // ============================================
    public static final Color BG_DARK_PRIMARY = new Color(15, 23, 42);        // Slate-900
    public static final Color BG_DARK_SECONDARY = new Color(30, 41, 59);      // Slate-800
    public static final Color BG_DARK_CARD = new Color(51, 65, 85);           // Slate-700
    public static final Color BG_DARK_CARD_HOVER = new Color(71, 85, 105);    // Slate-600

    public static final Color ACCENT_BLUE = new Color(59, 130, 246);          // Blue-500
    public static final Color ACCENT_BLUE_LIGHT = new Color(96, 165, 250);    // Blue-400
    public static final Color ACCENT_CYAN = new Color(34, 211, 238);          // Cyan-400
    public static final Color ACCENT_EMERALD = new Color(52, 211, 153);       // Emerald-400
    public static final Color ACCENT_AMBER = new Color(251, 191, 36);         // Amber-400
    public static final Color ACCENT_ROSE = new Color(251, 113, 133);         // Rose-400
    public static final Color ACCENT_VIOLET = new Color(167, 139, 250);       // Violet-400
    public static final Color ACCENT_ORANGE = new Color(251, 146, 60);        // Orange-400

    public static final Color TEXT_PRIMARY = new Color(241, 245, 249);         // Slate-100
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);       // Slate-400
    public static final Color TEXT_MUTED = new Color(100, 116, 139);           // Slate-500

    public static final Color BORDER_SUBTLE = new Color(51, 65, 85);          // Slate-700
    public static final Color BORDER_FOCUS = new Color(59, 130, 246, 120);    // Blue with alpha

    // Severity Colors
    public static final Color SEVERITY_LOW = new Color(34, 197, 94);          // Green-500
    public static final Color SEVERITY_MEDIUM = new Color(234, 179, 8);       // Yellow-500
    public static final Color SEVERITY_HIGH = new Color(249, 115, 22);        // Orange-500
    public static final Color SEVERITY_CRITICAL = new Color(239, 68, 68);     // Red-500

    // Gradient pairs
    public static final Color GRADIENT_BLUE_START = new Color(37, 99, 235);
    public static final Color GRADIENT_BLUE_END = new Color(124, 58, 237);
    public static final Color GRADIENT_RED_START = new Color(220, 38, 38);
    public static final Color GRADIENT_RED_END = new Color(190, 24, 93);
    public static final Color GRADIENT_GREEN_START = new Color(5, 150, 105);
    public static final Color GRADIENT_GREEN_END = new Color(20, 184, 166);

    // ============================================
    //  FONTS
    // ============================================
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    // ============================================
    //  ROUNDED PANEL (Bo tròn góc + gradient)
    // ============================================

    public static class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color gradientStart;
        private Color gradientEnd;
        private boolean useGradient;

        public RoundedPanel(int radius) {
            this(radius, null, null);
        }

        public RoundedPanel(int radius, Color gradientStart, Color gradientEnd) {
            super();
            this.cornerRadius = radius;
            this.gradientStart = gradientStart;
            this.gradientEnd = gradientEnd;
            this.useGradient = (gradientStart != null && gradientEnd != null);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (useGradient) {
                GradientPaint gp = new GradientPaint(0, 0, gradientStart, getWidth(), getHeight(), gradientEnd);
                g2.setPaint(gp);
            } else {
                g2.setColor(getBackground());
            }
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
            g2.dispose();
            super.paintComponent(g);
        }

        public void setGradient(Color start, Color end) {
            this.gradientStart = start;
            this.gradientEnd = end;
            this.useGradient = (start != null && end != null);
            repaint();
        }
    }

    public static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Double(x + thickness / 2.0, y + thickness / 2.0,
                    w - thickness, h - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    // ============================================
    //  COMPONENT STYLES & FACTORIES
    // ============================================

    public static JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, bg.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(brighter(bg, 0.15f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public static void styleTextField(JTextField txt) {
        txt.setFont(FONT_BODY);
        txt.setBackground(BG_DARK_SECONDARY);
        txt.setForeground(TEXT_PRIMARY);
        txt.setCaretColor(ACCENT_BLUE_LIGHT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(BORDER_SUBTLE);
        table.setBackground(BG_DARK_SECONDARY);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(BG_DARK_PRIMARY);
        header.setForeground(ACCENT_BLUE_LIGHT);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_DARK_SECONDARY : BG_DARK_PRIMARY);
                    c.setForeground(TEXT_PRIMARY);
                }
                return c;
            }
        });
    }

    public static Color brighter(Color c, float factor) {
        int r = Math.min((int) (c.getRed() + 255 * factor), 255);
        int g = Math.min((int) (c.getGreen() + 255 * factor), 255);
        int b = Math.min((int) (c.getBlue() + 255 * factor), 255);
        return new Color(r, g, b);
    }

    public static Color getSeverityColor(String severity) {
        if (severity == null) return TEXT_MUTED;
        switch (severity.toUpperCase()) {
            case "LOW":
                return SEVERITY_LOW;
            case "MEDIUM":
                return SEVERITY_MEDIUM;
            case "HIGH":
                return SEVERITY_HIGH;
            case "CRITICAL":
                return SEVERITY_CRITICAL;
            default:
                return TEXT_MUTED;
        }
    }

    /**
     * Thiết lập global UI defaults cho dark theme.
     */
    public static void applyGlobalDefaults() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        UIManager.put("control", BG_DARK_SECONDARY);
        UIManager.put("info", BG_DARK_SECONDARY);
        UIManager.put("nimbusBase", BG_DARK_PRIMARY);
        UIManager.put("nimbusAlertYellow", SEVERITY_MEDIUM);
        UIManager.put("nimbusDisabledText", TEXT_MUTED);
        UIManager.put("nimbusFocus", ACCENT_BLUE);
        UIManager.put("nimbusGreen", ACCENT_EMERALD);
        UIManager.put("nimbusInfoBlue", ACCENT_BLUE);
        UIManager.put("nimbusLightBackground", BG_DARK_SECONDARY);
        UIManager.put("nimbusOrange", SEVERITY_HIGH);
        UIManager.put("nimbusRed", SEVERITY_CRITICAL);
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("nimbusSelectionBackground", ACCENT_BLUE);
        UIManager.put("text", TEXT_PRIMARY);

        UIManager.put("Panel.background", BG_DARK_PRIMARY);
        UIManager.put("OptionPane.background", BG_DARK_SECONDARY);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("OptionPane.buttonFont", FONT_BUTTON);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BODY_BOLD);
    }
}
