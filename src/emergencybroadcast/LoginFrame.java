package emergencybroadcast;

import emergencybroadcast.dao.UserDAO;
import emergencybroadcast.model.User;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Giao diện Đăng nhập hệ thống (LoginFrame).
 * Được cấu hình chuẩn NetBeans GUI Builder (có tệp LoginFrame.form để kéo thả kéo chỉnh bằng chuột).
 */
public class LoginFrame extends javax.swing.JFrame {

    private final UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initComponents();
        setupEvents();
        
        // Tùy chỉnh màu sắc bằng UITheme
        UITheme.styleTextField(txtUsername);
        UITheme.styleTextField(txtPassword);
        btnLogin.setBackground(UITheme.ACCENT_BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnExit.setBackground(UITheme.BG_DARK_CARD);
        btnExit.setForeground(UITheme.TEXT_PRIMARY);
        
        setSize(480, 520);
        setLocationRelativeTo(null);
    }

    /**
     * Khởi tạo giao diện chuẩn NetBeans GUI Builder.
     * Mở file này trong NetBeans để sử dụng tab Design kéo thả chuột!
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        titlePanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        formPanel = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblError = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        btnLogin = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Đăng Nhập Hệ Thống Cảnh Báo Khẩn Cấp");
        setResizable(false);

        titlePanel.setBackground(new java.awt.Color(26, 26, 51));
        titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        titlePanel.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("ỨNG DỤNG CẢNH BÁO KHẨN CẤP");
        titlePanel.add(lblTitle);

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSubtitle.setForeground(new java.awt.Color(204, 204, 204));
        lblSubtitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitle.setText("Vui lòng đăng nhập tài khoản hệ thống");
        titlePanel.add(lblSubtitle);

        getContentPane().add(titlePanel, java.awt.BorderLayout.PAGE_START);

        formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 35, 15, 35));
        formPanel.setLayout(new java.awt.GridBagLayout());

        lblUsername.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblUsername.setText("Tên đăng nhập:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 5, 0);
        formPanel.add(lblUsername, gridBagConstraints);

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        formPanel.add(txtUsername, gridBagConstraints);

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblPassword.setText("Mật khẩu:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        formPanel.add(lblPassword, gridBagConstraints);

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 15, 0);
        formPanel.add(txtPassword, gridBagConstraints);

        lblError.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblError.setForeground(new java.awt.Color(255, 51, 51));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        formPanel.add(lblError, gridBagConstraints);

        getContentPane().add(formPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 35, 25, 35));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 2, 15, 0));

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogin.setText("Đăng Nhập");
        buttonPanel.add(btnLogin);

        btnExit.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnExit.setText("Thoát");
        buttonPanel.add(btnExit);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupEvents() {
        btnLogin.addActionListener(e -> performLogin());
        btnExit.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return;
        }

        lblError.setText("");
        User user = userDAO.login(username, password);

        if (user != null) {
            this.dispose();
            String role = user.getRole();
            if ("ADMIN".equalsIgnoreCase(role) || "OPERATOR".equalsIgnoreCase(role)) {
                new MainMenuFrame(user).setVisible(true);
            } else {
                new ReceiverFrame(user).setVisible(true);
            }
        } else {
            lblError.setText("Tài khoản hoặc mật khẩu không chính xác!");
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalDefaults();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogin;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
