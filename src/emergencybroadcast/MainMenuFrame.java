package emergencybroadcast;

import emergencybroadcast.model.User;
import emergencybroadcast.ui.UITheme;

import java.awt.*;

/**
 * Giao diện Menu Chính (MainMenuFrame).
 * Được cấu hình chuẩn NetBeans GUI Builder (có tệp MainMenuFrame.form để kéo thả kéo chỉnh bằng chuột).
 */
public class MainMenuFrame extends javax.swing.JFrame {

    private final User currentUser;

    public MainMenuFrame(User user) {
        this.currentUser = user;
        initComponents();
        setupUserInfo();
        setupEvents();
        styleMenuButtons();

        setSize(780, 580);
        setLocationRelativeTo(null);
    }

    private void setupUserInfo() {
        if (currentUser != null) {
            lblUserInfo.setText("Xin chào, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        }
    }

    private void styleMenuButtons() {
        btnSender.setBackground(UITheme.BG_DARK_CARD);
        btnReceiver.setBackground(UITheme.BG_DARK_CARD);
        btnHistory.setBackground(UITheme.BG_DARK_CARD);
        btnLogout.setBackground(UITheme.BG_DARK_CARD);

        btnSender.setForeground(Color.WHITE);
        btnReceiver.setForeground(Color.WHITE);
        btnHistory.setForeground(Color.WHITE);
        btnLogout.setForeground(UITheme.ACCENT_ROSE);
    }

    /**
     * Khởi tạo giao diện chuẩn NetBeans GUI Builder.
     * Mở file này trong NetBeans để sử dụng tab Design kéo thả chuột!
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblUserInfo = new javax.swing.JLabel();
        menuContainer = new javax.swing.JPanel();
        btnSender = new javax.swing.JButton();
        btnReceiver = new javax.swing.JButton();
        btnHistory = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Menu Chính - Hệ Thống Cảnh Báo Khẩn Cấp");

        headerPanel.setBackground(new java.awt.Color(26, 26, 51));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));
        headerPanel.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblTitle.setText("BẢNG ĐIỀU KHIỂN HỆ THỐNG");
        headerPanel.add(lblTitle, java.awt.BorderLayout.WEST);

        lblUserInfo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUserInfo.setForeground(new java.awt.Color(230, 230, 230));
        lblUserInfo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUserInfo.setText("Xin chào, User (ADMIN)");
        headerPanel.add(lblUserInfo, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);

        menuContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 30, 30, 30));
        menuContainer.setLayout(new java.awt.GridLayout(2, 2, 20, 20));

        btnSender.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnSender.setText("<html><center><font size=5>📡 GỬI CẢNH BÁO</font><br><font color=#aaaaaa size=3>Tạo &amp; phát bản tin khẩn cấp</font></center></html>");
        menuContainer.add(btnSender);

        btnReceiver.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnReceiver.setText("<html><center><font size=5>🔔 NHẬN CẢNH BÁO</font><br><font color=#aaaaaa size=3>Theo dõi &amp; tiếp nhận cảnh báo</font></center></html>");
        menuContainer.add(btnReceiver);

        btnHistory.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnHistory.setText("<html><center><font size=5>📜 LỊCH SỬ &amp; TÌM KIẾM</font><br><font color=#aaaaaa size=3>Tra cứu nhật ký phát tin &amp; báo nhận</font></center></html>");
        menuContainer.add(btnHistory);

        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btnLogout.setText("<html><center><font size=5>🚪 ĐĂNG XUẤT</font><br><font color=#aaaaaa size=3>Thoát phiên làm việc hiện tại</font></center></html>");
        menuContainer.add(btnLogout);

        getContentPane().add(menuContainer, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupEvents() {
        btnSender.addActionListener(e -> {
            new SenderFrame(currentUser).setVisible(true);
            this.dispose();
        });

        btnReceiver.addActionListener(e -> {
            new ReceiverFrame(currentUser).setVisible(true);
            this.dispose();
        });

        btnHistory.addActionListener(e -> {
            new HistoryFrame(currentUser).setVisible(true);
            this.dispose();
        });

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHistory;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnReceiver;
    private javax.swing.JButton btnSender;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUserInfo;
    private javax.swing.JPanel menuContainer;
    // End of variables declaration//GEN-END:variables
}
