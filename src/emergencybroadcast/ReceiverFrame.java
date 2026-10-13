package emergencybroadcast;

import emergencybroadcast.dao.AlertReceiptDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.AlertReceipt;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastReceiver;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.InetAddress;
import java.util.List;

/**
 * Giao diện Trạm tiếp nhận Cảnh báo (ReceiverFrame).
 * Được cấu hình chuẩn NetBeans GUI Builder (có tệp ReceiverFrame.form để kéo thả kéo chỉnh bằng chuột).
 */
public class ReceiverFrame extends javax.swing.JFrame {

    private final User currentUser;
    private final AlertReceiptDAO receiptDAO;
    private MulticastReceiver multicastReceiver;
    private Alert currentAlert;
    private DefaultTableModel tableModel;

    public ReceiverFrame(User user) {
        this.currentUser = user;
        this.receiptDAO = new AlertReceiptDAO();
        initComponents();
        setupCustomStyles();
        setupEvents();
        startListening();
        loadReceiptHistory();

        setSize(850, 650);
        setLocationRelativeTo(null);
    }

    private void setupCustomStyles() {
        btnConfirmReceipt.setBackground(UITheme.ACCENT_EMERALD);
        btnConfirmReceipt.setForeground(Color.WHITE);
        btnBack.setBackground(UITheme.BG_DARK_CARD);
        btnBack.setForeground(Color.WHITE);

        UITheme.styleTable(tblReceivedAlerts);
        tableModel = (DefaultTableModel) tblReceivedAlerts.getModel();
    }

    /**
     * Khởi tạo giao diện chuẩn NetBeans GUI Builder.
     * Mở file này trong NetBeans để sử dụng tab Design kéo thả chuột!
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        headerLeft = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        alertDisplayPanel = new javax.swing.JPanel();
        alertHeaderInfo = new javax.swing.JPanel();
        lblAlertTitle = new javax.swing.JLabel();
        lblSeverity = new javax.swing.JLabel();
        spContent = new javax.swing.JScrollPane();
        txtAlertContent = new javax.swing.JTextArea();
        actionPanel = new javax.swing.JPanel();
        btnConfirmReceipt = new javax.swing.JButton();
        historyPanel = new javax.swing.JPanel();
        spHistory = new javax.swing.JScrollPane();
        tblReceivedAlerts = new javax.swing.JTable();
        historyButtonPanel = new javax.swing.JPanel();
        btnRefreshHistory = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Trạm Tiếp Nhận Cảnh Báo Khẩn Cấp - Receiver Station");

        headerPanel.setBackground(new java.awt.Color(26, 26, 51));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setLayout(new java.awt.BorderLayout());

        headerLeft.setOpaque(false);
        headerLeft.setLayout(new java.awt.GridLayout(2, 1, 0, 3));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblTitle.setText("TRẠM TIẾP NHẬN CẢNH BÁO (RECEIVER)");
        headerLeft.add(lblTitle);

        lblStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStatus.setForeground(new java.awt.Color(102, 255, 102));
        lblStatus.setText("Đang lắng nghe tín hiệu Multicast (239.10.10.10:5000)...");
        headerLeft.add(lblStatus);

        headerPanel.add(headerLeft, java.awt.BorderLayout.WEST);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Menu Chính");
        headerPanel.add(btnBack, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);

        splitPane.setDividerLocation(300);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        alertDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bản Tin Cảnh Báo Khẩn Cấp Mới Nhất", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(255, 51, 51))); // NOI18N
        alertDisplayPanel.setLayout(new java.awt.BorderLayout());

        alertHeaderInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 5, 15));
        alertHeaderInfo.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        lblAlertTitle.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblAlertTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblAlertTitle.setText("[Chưa có tin cảnh báo mới]");
        alertHeaderInfo.add(lblAlertTitle);

        lblSeverity.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSeverity.setForeground(new java.awt.Color(204, 204, 204));
        lblSeverity.setText("Trạng thái: Chờ phát sóng...");
        alertHeaderInfo.add(lblSeverity);

        alertDisplayPanel.add(alertHeaderInfo, java.awt.BorderLayout.NORTH);

        spContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 10, 15));

        txtAlertContent.setEditable(false);
        txtAlertContent.setColumns(20);
        txtAlertContent.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtAlertContent.setLineWrap(true);
        txtAlertContent.setRows(4);
        txtAlertContent.setText("Khi có cảnh báo được phát ra từ trạm Sender, nội dung bản tin sẽ hiển thị lập tức tại đây và hệ thống sẽ phát âm thanh báo động khẩn cấp.");
        txtAlertContent.setWrapStyleWord(true);
        spContent.setViewportView(txtAlertContent);

        alertDisplayPanel.add(spContent, java.awt.BorderLayout.CENTER);

        actionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 15, 15));
        actionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnConfirmReceipt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnConfirmReceipt.setText("XÁC NHẬN ĐÃ NHẬN TIN (ACK)");
        btnConfirmReceipt.setEnabled(false);
        actionPanel.add(btnConfirmReceipt);

        alertDisplayPanel.add(actionPanel, java.awt.BorderLayout.SOUTH);

        splitPane.setTopComponent(alertDisplayPanel);

        historyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nhật Ký Cảnh Báo Đã Tiếp Nhận", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(51, 153, 255))); // NOI18N
        historyPanel.setLayout(new java.awt.BorderLayout());

        tblReceivedAlerts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID Nhật Ký", "ID Cảnh Báo", "Máy Nhận", "Địa Chỉ IP", "Thời Gian Nhận", "Trạng Thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spHistory.setViewportView(tblReceivedAlerts);

        historyPanel.add(spHistory, java.awt.BorderLayout.CENTER);

        historyButtonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        historyButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnRefreshHistory.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnRefreshHistory.setText("Tải Lại Lịch Sử");
        historyButtonPanel.add(btnRefreshHistory);

        historyPanel.add(historyButtonPanel, java.awt.BorderLayout.SOUTH);

        splitPane.setBottomComponent(historyPanel);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupEvents() {
        btnBack.addActionListener(e -> {
            stopListening();
            if (currentUser != null && "RECEIVER".equalsIgnoreCase(currentUser.getRole())) {
                new LoginFrame().setVisible(true);
            } else {
                new MainMenuFrame(currentUser).setVisible(true);
            }
            this.dispose();
        });

        btnConfirmReceipt.addActionListener(e -> confirmReceipt());
        btnRefreshHistory.addActionListener(e -> loadReceiptHistory());
    }

    private void startListening() {
        multicastReceiver = new MulticastReceiver(new MulticastReceiver.OnAlertReceivedListener() {
            @Override
            public void onAlertReceived(Alert alert) {
                ReceiverFrame.this.onAlertReceived(alert);
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("-> Lỗi Multicast Receiver: " + errorMessage);
            }
        });
        multicastReceiver.startListening();
    }

    private void stopListening() {
        if (multicastReceiver != null) {
            multicastReceiver.stopListening();
        }
    }

    private void onAlertReceived(Alert alert) {
        SwingUtilities.invokeLater(() -> {
            this.currentAlert = alert;
            lblAlertTitle.setText(alert.getLocation());
            lblSeverity.setText("Mức độ: " + alert.getSeverity() + " | Mã tin: " + alert.getAlertCode() + " | Người phát: " + alert.getSenderUsername());
            txtAlertContent.setText(alert.getMessage());
            btnConfirmReceipt.setEnabled(true);
            Toolkit.getDefaultToolkit().beep();
        });
    }

    private void confirmReceipt() {
        if (currentAlert == null) return;

        try {
            String receiverName = currentUser != null ? currentUser.getFullName() : ("Máy Trạm (" + InetAddress.getLocalHost().getHostName() + ")");
            String receiverIp = InetAddress.getLocalHost().getHostAddress();

            AlertReceipt receipt = new AlertReceipt(currentAlert.getId(), receiverName, receiverIp, "ACKNOWLEDGED");
            boolean success = receiptDAO.insertReceipt(receipt);

            if (success) {
                JOptionPane.showMessageDialog(this, "Đã gửi phản hồi xác nhận (ACK) thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                btnConfirmReceipt.setEnabled(false);
                lblSeverity.setText("Mức độ: " + currentAlert.getSeverity() + " (ĐÃ XÁC NHẬN ACK)");
                loadReceiptHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể lưu xác nhận vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo bản tin xác nhận ACK: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReceiptHistory() {
        tableModel.setRowCount(0);
        List<AlertReceipt> receipts = receiptDAO.getAllReceipts();

        for (AlertReceipt r : receipts) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getAlertId(),
                    r.getReceiverName(),
                    r.getReceiverIp(),
                    r.getReceivedAt() != null ? r.getReceivedAt().toString() : "",
                    r.getStatus()
            });
        }
    }

    @Override
    public void dispose() {
        stopListening();
        super.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JPanel alertDisplayPanel;
    private javax.swing.JPanel alertHeaderInfo;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnConfirmReceipt;
    private javax.swing.JButton btnRefreshHistory;
    private javax.swing.JPanel headerLeft;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel historyButtonPanel;
    private javax.swing.JPanel historyPanel;
    private javax.swing.JLabel lblAlertTitle;
    private javax.swing.JLabel lblSeverity;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JScrollPane spContent;
    private javax.swing.JScrollPane spHistory;
    private javax.swing.JTable tblReceivedAlerts;
    private javax.swing.JTextArea txtAlertContent;
    // End of variables declaration//GEN-END:variables
}
