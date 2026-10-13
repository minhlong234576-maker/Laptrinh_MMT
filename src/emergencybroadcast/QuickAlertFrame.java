package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastSender;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;

/**
 * QuickAlertFrame – Màn hình phát cảnh báo KHẨN CẤP 1 chạm.
 *
 * Thiết kế tối giản:
 *  - Chỉ cần chọn ĐỊA ĐIỂM (danh sách gợi ý + tự gõ)
 *  - Nhấn 1 nút to = PHÁT NGAY – không cần điền form
 *  - Phím tắt F1-F5 phát từng loại sự cố
 *  - Phím ESC để đóng
 *  - Kết quả hiện trên status bar (không popup chặn)
 */
public class QuickAlertFrame extends javax.swing.JFrame {

    private final User currentUser;
    private final AlertDAO alertDAO;

    // Địa điểm thường dùng (gợi ý nhanh trong combobox)
    private static final String[] QUICK_LOCATIONS = {
        "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5",
        "Phòng máy chủ", "Kho hàng", "Tầng hầm B1", "Khu vực bếp",
        "Sảnh chính", "Phòng bảo vệ", "Bãi đỗ xe"
    };

    // Cấu hình các loại cảnh báo nhanh
    // { tên hiển thị, emoji, alertType, severity, color, nội dung mặc định }
    private static final Object[][] QUICK_TYPES = {
        {"CHÁY / LỬA",  "🔥", "FIRE",     "CRITICAL", new Color(200, 30,  30),  "Phát hiện cháy! Sơ tán khẩn cấp theo lối thoát hiểm!"},
        {"KHÍ ĐỘC",     "☠", "GAS",      "CRITICAL", new Color(150, 50, 180),  "Rò rỉ khí độc! Di chuyển ra ngoài ngay lập tức!"},
        {"AN NINH",     "!", "SECURITY", "HIGH",     new Color(220, 100,  0),  "Phát hiện xâm nhập! Liên hệ bảo vệ ngay!"},
        {"MẤT ĐIỆN",    "*", "POWER",    "HIGH",     new Color(200, 160,  0),  "Mất điện toàn bộ! Kích hoạt nguồn dự phòng!"},
        {"LŨ / NGẬP",   "~", "FLOOD",    "HIGH",     new Color( 30, 100, 200),  "Cảnh báo ngập lụt! Di chuyển lên tầng cao!"},
    };

    private JComboBox<String> cmbLocation;
    private JLabel lblStatusMsg;
    private javax.swing.Timer statusClearTimer;

    public QuickAlertFrame(User user) {
        this.currentUser = user;
        this.alertDAO = new AlertDAO();
        buildUI();

        setTitle("PHAT NHANH CANH BAO KHAN CAP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // ESC đóng cửa sổ
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // F1-F5 phát từng loại sự cố
        for (int i = 0; i < QUICK_TYPES.length; i++) {
            final int idx = i;
            getRootPane().registerKeyboardAction(
                e -> fireQuickAlert(idx),
                KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
            );
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (statusClearTimer != null) statusClearTimer.stop();
            }
        });
    }

    private void buildUI() {
        getContentPane().setBackground(UITheme.BG_DARK_PRIMARY);
        getContentPane().setLayout(new BorderLayout(0, 0));

        // ===== HEADER đỏ =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(120, 10, 10));
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("  PHAT CANH BAO KHAN CAP NGAY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        JLabel lblHint = new JLabel("F1=Chay  F2=Khi Doc  F3=An Ninh  F4=Mat Dien  F5=Lu  |  ESC=Dong");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(new Color(255, 200, 200));
        header.add(lblHint, BorderLayout.EAST);

        getContentPane().add(header, BorderLayout.NORTH);

        // ===== CENTRE WRAPPER =====
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(UITheme.BG_DARK_PRIMARY);

        // ===== LOCATION BAR =====
        JPanel locPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        locPanel.setBackground(UITheme.BG_DARK_SECONDARY);

        JLabel lblLoc = new JLabel("  Dia diem su co:");
        lblLoc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblLoc.setForeground(Color.WHITE);
        locPanel.add(lblLoc);

        cmbLocation = new JComboBox<>(QUICK_LOCATIONS);
        cmbLocation.setEditable(true);
        cmbLocation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbLocation.setBackground(UITheme.BG_DARK_CARD);
        cmbLocation.setForeground(Color.WHITE);
        cmbLocation.setPreferredSize(new Dimension(240, 36));
        cmbLocation.setSelectedIndex(0);
        cmbLocation.setToolTipText("Chon hoac go ten dia diem");
        locPanel.add(cmbLocation);

        JLabel lblTip = new JLabel("  <-- Chon/go dia diem, sau do nhan nut bên duoi de PHAT NGAY");
        lblTip.setFont(UITheme.FONT_SMALL);
        lblTip.setForeground(new Color(180, 220, 255));
        locPanel.add(lblTip);

        centerWrapper.add(locPanel, BorderLayout.NORTH);

        // ===== QUICK BUTTONS =====
        JPanel btnPanel = new JPanel(new GridLayout(1, QUICK_TYPES.length, 12, 0));
        btnPanel.setBackground(UITheme.BG_DARK_PRIMARY);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        for (int i = 0; i < QUICK_TYPES.length; i++) {
            btnPanel.add(createQuickButton(i));
        }
        centerWrapper.add(btnPanel, BorderLayout.CENTER);

        // ===== STATUS BAR + BACK BUTTON =====
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(UITheme.BG_DARK_PRIMARY);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 20));

        lblStatusMsg = new JLabel("  He thong san sang – Chon dia diem va nhan loai su co de phat ngay.");
        lblStatusMsg.setFont(UITheme.FONT_BODY);
        lblStatusMsg.setForeground(UITheme.TEXT_SECONDARY);
        lblStatusMsg.setOpaque(true);
        lblStatusMsg.setBackground(UITheme.BG_DARK_SECONDARY);
        lblStatusMsg.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        JButton btnBack = new JButton("<- Quay lai");
        btnBack.setFont(UITheme.FONT_BODY_BOLD);
        btnBack.setBackground(UITheme.BG_DARK_CARD);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setPreferredSize(new Dimension(130, 42));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> dispose());

        bottomPanel.add(lblStatusMsg, BorderLayout.CENTER);
        bottomPanel.add(btnBack, BorderLayout.EAST);

        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(centerWrapper, BorderLayout.CENTER);
    }

    /**
     * Tạo nút phát nhanh cho từng loại sự cố.
     */
    private JButton createQuickButton(int idx) {
        Object[] cfg = QUICK_TYPES[idx];
        String label    = (String) cfg[0];
        String emoji    = (String) cfg[1];
        String severity = (String) cfg[3];
        Color  color    = (Color)  cfg[4];
        int keyNum = idx + 1;

        String html = "<html><center>"
            + "<font size=6>" + emoji + "</font><br><br>"
            + "<b><font size=4>" + label + "</font></b><br>"
            + "<font color=#ffdddd>" + severity + "</font><br><br>"
            + "<font color=#cccccc size=2>Nhan hoac nhan [F" + keyNum + "]</font>"
            + "</center></html>";

        JButton btn = new JButton(html);
        btn.setFont(UITheme.FONT_BODY);
        btn.setBackground(color.darker());
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 8, 10, 8)
        ));
        btn.setOpaque(true);

        // Hover effect sáng lên
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(color);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 3),
                    BorderFactory.createEmptyBorder(10, 8, 10, 8)
                ));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(color.darker());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color, 2),
                    BorderFactory.createEmptyBorder(10, 8, 10, 8)
                ));
            }
        });

        btn.addActionListener(e -> fireQuickAlert(idx));
        return btn;
    }

    /**
     * PHÁT NHANH – luồng tối giản 3 bước:
     *  1. Lấy địa điểm từ combobox
     *  2. Gửi DB + UDP trên background thread (không block UI)
     *  3. Hiển thị kết quả trên status bar
     */
    public void fireQuickAlert(int idx) {
        Object[] cfg = QUICK_TYPES[idx];
        String label     = (String) cfg[0];
        String alertType = (String) cfg[2];
        String severity  = (String) cfg[3];
        Color  color     = (Color)  cfg[4];
        String defaultMsg = (String) cfg[5];

        // Lấy địa điểm
        String location = "";
        Object selectedLoc = cmbLocation.getSelectedItem();
        if (selectedLoc != null) location = selectedLoc.toString().trim();
        if (location.isEmpty()) location = "Khong xac dinh";

        // Hiển thị trạng thái gửi
        setStatusSending(label, location);

        final String finalLocation = location;
        final String finalAlertType = alertType;
        final String finalSeverity = severity;
        final String finalMsg = defaultMsg;
        final Color finalColor = color;

        // Chạy hoàn toàn trên background thread
        new Thread(() -> {
            try {
                String alertCode = "QUICK-" + System.currentTimeMillis() % 100000;
                String senderUser = (currentUser != null) ? currentUser.getUsername() : "admin";
                String senderIp = "127.0.0.1";
                try { senderIp = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignored) {}

                Alert alert = new Alert();
                alert.setAlertCode(alertCode);
                alert.setAlertType(finalAlertType);
                alert.setSeverity(finalSeverity);
                alert.setLocation(finalLocation);
                alert.setMessage("[KHAN CAP] " + finalMsg + " | Dia diem: " + finalLocation);
                alert.setSenderUsername(senderUser);
                alert.setSenderIp(senderIp);

                // 1. Lưu DB
                int alertId = alertDAO.insertAlert(alert);
                if (alertId > 0) alert.setId(alertId);

                // 2. Phát UDP Multicast
                boolean sent = MulticastSender.sendAlert(alert);

                final boolean success = sent;
                final int id = alertId;

                // 3. Cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    if (success && id > 0) {
                        setStatusSuccess(label, finalLocation, finalColor);
                        Toolkit.getDefaultToolkit().beep();
                    } else if (id <= 0) {
                        setStatusError("Loi luu DB! Kiem tra MySQL/XAMPP da bat chua.");
                    } else {
                        setStatusError("Da luu DB nhung loi phat UDP Multicast!");
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> setStatusError("Loi: " + ex.getMessage()));
            }
        }, "QuickAlert-" + alertType).start();
    }

    private void setStatusSending(String label, String location) {
        lblStatusMsg.setText("  Dang phat " + label + " tai [" + location + "]...");
        lblStatusMsg.setForeground(new Color(200, 200, 0));
        lblStatusMsg.setBackground(new Color(50, 50, 10));
        if (statusClearTimer != null) statusClearTimer.stop();
    }

    private void setStatusSuccess(String label, String location, Color color) {
        lblStatusMsg.setText("  DA PHAT THANH CONG: " + label
            + " tai [" + location + "] – Tat ca may tram da nhan tin hieu!");
        lblStatusMsg.setForeground(new Color(100, 255, 120));
        lblStatusMsg.setBackground(new Color(15, 50, 20));
        // Tự reset về sẵn sàng sau 8 giây
        statusClearTimer = new javax.swing.Timer(8000, e -> {
            lblStatusMsg.setText("  He thong san sang – Chon dia diem va nhan loai su co de phat ngay.");
            lblStatusMsg.setForeground(UITheme.TEXT_SECONDARY);
            lblStatusMsg.setBackground(UITheme.BG_DARK_SECONDARY);
        });
        statusClearTimer.setRepeats(false);
        statusClearTimer.start();
    }

    private void setStatusError(String msg) {
        lblStatusMsg.setText("  LOI: " + msg);
        lblStatusMsg.setForeground(new Color(255, 100, 100));
        lblStatusMsg.setBackground(new Color(60, 15, 15));
    }
}
