package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastSender;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.InetAddress;
import java.util.List;

/**
 * Giao diện Trạm phát Cảnh báo (SenderFrame).
 * Được cấu hình chuẩn NetBeans GUI Builder (có tệp SenderFrame.form để kéo thả kéo chỉnh bằng chuột).
 */
public class SenderFrame extends javax.swing.JFrame {

    private final User currentUser;
    private final AlertDAO alertDAO;
    private DefaultTableModel tableModel;

    public SenderFrame(User user) {
        this.currentUser = user;
        this.alertDAO = new AlertDAO();
        initComponents();
        setupCustomStyles();
        setupEvents();
        loadAlertHistory();

        setSize(1000, 750);
        setLocationRelativeTo(null);
    }

    private void setupCustomStyles() {
        btnSendAlert.setBackground(UITheme.ACCENT_BLUE);
        btnSendAlert.setForeground(Color.WHITE);
        btnClearForm.setBackground(UITheme.BG_DARK_CARD);
        btnBack.setBackground(UITheme.BG_DARK_CARD);
        btnBack.setForeground(Color.WHITE);

        UITheme.styleTable(tblSentAlerts);
        UITheme.styleTextField(txtAlertTitle);

        tableModel = (DefaultTableModel) tblSentAlerts.getModel();
    }

    /**
     * Khởi tạo giao diện chuẩn NetBeans GUI Builder.
     * Mở file này trong NetBeans để sử dụng tab Design kéo thả chuột!
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        formPanel = new javax.swing.JPanel();
        lblSeverity = new javax.swing.JLabel();
        cmbSeverity = new javax.swing.JComboBox<>();
        lblAlertTitle = new javax.swing.JLabel();
        txtAlertTitle = new javax.swing.JTextField();
        lblContent = new javax.swing.JLabel();
        spContent = new javax.swing.JScrollPane();
        txtContent = new javax.swing.JTextArea();
        formButtonPanel = new javax.swing.JPanel();
        btnSendAlert = new javax.swing.JButton();
        btnClearForm = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        spTable = new javax.swing.JScrollPane();
        tblSentAlerts = new javax.swing.JTable();
        tableButtonPanel = new javax.swing.JPanel();
        btnRefreshTable = new javax.swing.JButton();
        btnDeleteAlert = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Phát Cảnh Báo Khẩn Cấp - Sender Station");

        headerPanel.setBackground(new java.awt.Color(26, 26, 51));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblTitle.setText("TRẠM PHÁT CẢNH BÁO KHẨN CẤP (SENDER)");
        headerPanel.add(lblTitle, java.awt.BorderLayout.WEST);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Menu Chính");
        headerPanel.add(btnBack, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);

        splitPane.setDividerLocation(450);
        splitPane.setResizeWeight(0.45);

        formPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tạo Bản Tin Cảnh Báo Mới", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 77, 77))); // NOI18N
        formPanel.setLayout(new java.awt.GridBagLayout());

        lblSeverity.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSeverity.setText("Mức độ khẩn cấp:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        formPanel.add(lblSeverity, gridBagConstraints);

        cmbSeverity.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CRITICAL - Cực kỳ khẩn cấp", "HIGH - Khẩn cấp cao", "MEDIUM - Cảnh báo trung bình", "LOW - Thông tin bình thường" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        formPanel.add(cmbSeverity, gridBagConstraints);

        lblAlertTitle.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblAlertTitle.setText("Tiêu đề bản tin:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        formPanel.add(lblAlertTitle, gridBagConstraints);

        txtAlertTitle.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        formPanel.add(txtAlertTitle, gridBagConstraints);

        lblContent.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblContent.setText("Nội dung cảnh báo chi tiết:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        formPanel.add(lblContent, gridBagConstraints);

        txtContent.setColumns(20);
        txtContent.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtContent.setLineWrap(true);
        txtContent.setRows(5);
        txtContent.setWrapStyleWord(true);
        spContent.setViewportView(txtContent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
        formPanel.add(spContent, gridBagConstraints);

        formButtonPanel.setLayout(new java.awt.GridLayout(1, 2, 10, 0));

        btnSendAlert.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSendAlert.setText("🚀 PHÁT CẢNH BÁO");
        formButtonPanel.add(btnSendAlert);

        btnClearForm.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearForm.setText("🧹 Làm Mới");
        formButtonPanel.add(btnClearForm);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        formPanel.add(formButtonPanel, gridBagConstraints);

        splitPane.setLeftComponent(formPanel);

        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nhật Ký Bản Tin Đã Phát", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(51, 153, 255))); // NOI18N
        tablePanel.setLayout(new java.awt.BorderLayout());

        tblSentAlerts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Mã tin", "Mức độ", "Địa điểm", "Thời gian"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spTable.setViewportView(tblSentAlerts);

        tablePanel.add(spTable, java.awt.BorderLayout.CENTER);

        tableButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tableButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnRefreshTable.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnRefreshTable.setText("🔄 Tải Lại");
        tableButtonPanel.add(btnRefreshTable);

        btnDeleteAlert.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnDeleteAlert.setText("🗑️ Xóa Bản Tin");
        tableButtonPanel.add(btnDeleteAlert);

        tablePanel.add(tableButtonPanel, java.awt.BorderLayout.SOUTH);

        splitPane.setRightComponent(tablePanel);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupEvents() {
        btnBack.addActionListener(e -> {
            new MainMenuFrame(currentUser).setVisible(true);
            this.dispose();
        });

        btnClearForm.addActionListener(e -> clearForm());
        btnSendAlert.addActionListener(e -> performSendAlert());
        btnRefreshTable.addActionListener(e -> loadAlertHistory());
    }

    private void clearForm() {
        txtAlertTitle.setText("");
        txtContent.setText("");
        cmbSeverity.setSelectedIndex(0);
    }

    private void performSendAlert() {
        String title = txtAlertTitle.getText().trim();
        String content = txtContent.getText().trim();
        String severitySel = (String) cmbSeverity.getSelectedItem();
        String severity = "CRITICAL";

        if (severitySel != null) {
            if (severitySel.contains("HIGH")) severity = "HIGH";
            else if (severitySel.contains("MEDIUM")) severity = "MEDIUM";
            else if (severitySel.contains("LOW")) severity = "LOW";
        }

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung cảnh báo!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String alertCode = "ALT-" + System.currentTimeMillis() % 100000;
        String senderUser = currentUser != null ? currentUser.getUsername() : "admin";
        String senderIp = "127.0.0.1";
        try {
            senderIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {}

        Alert alert = new Alert();
        alert.setAlertCode(alertCode);
        alert.setAlertType("SECURITY");
        alert.setSeverity(severity);
        alert.setLocation(title);
        alert.setMessage(content);
        alert.setSenderUsername(senderUser);
        alert.setSenderIp(senderIp);

        int alertId = alertDAO.insertAlert(alert);
        if (alertId > 0) {
            alert.setId(alertId);
            boolean sent = MulticastSender.sendAlert(alert);
            if (sent) {
                JOptionPane.showMessageDialog(this, "Đã phát cảnh báo thành công qua UDP Multicast!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadAlertHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Cảnh báo đã lưu vào DB nhưng lỗi phát UDP Multicast!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu cảnh báo vào cơ sở dữ liệu!", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAlertHistory() {
        tableModel.setRowCount(0);
        List<Alert> alerts = alertDAO.getAllAlerts();
        for (Alert a : alerts) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getAlertCode(),
                    a.getSeverity(),
                    a.getLocation(),
                    a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClearForm;
    private javax.swing.JButton btnDeleteAlert;
    private javax.swing.JButton btnRefreshTable;
    private javax.swing.JButton btnSendAlert;
    private javax.swing.JComboBox<String> cmbSeverity;
    private javax.swing.JPanel formButtonPanel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblAlertTitle;
    private javax.swing.JLabel lblContent;
    private javax.swing.JLabel lblSeverity;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane spContent;
    private javax.swing.JScrollPane spTable;
    private javax.swing.JPanel tableButtonPanel;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblSentAlerts;
    private javax.swing.JTextField txtAlertTitle;
    private javax.swing.JTextArea txtContent;
    // End of variables declaration//GEN-END:variables
}
