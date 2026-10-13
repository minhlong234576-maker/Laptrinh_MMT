package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.dao.AlertReceiptDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.AlertReceipt;
import emergencybroadcast.model.User;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Giao diện Lịch sử & Tra cứu Cảnh báo (HistoryFrame).
 * Được cấu hình chuẩn NetBeans GUI Builder (có tệp HistoryFrame.form để kéo thả kéo chỉnh bằng chuột).
 */
public class HistoryFrame extends javax.swing.JFrame {

    private final User currentUser;
    private final AlertDAO alertDAO;
    private final AlertReceiptDAO receiptDAO;
    private DefaultTableModel tableModel;

    public HistoryFrame(User user) {
        this.currentUser = user;
        this.alertDAO = new AlertDAO();
        this.receiptDAO = new AlertReceiptDAO();

        initComponents();
        setupCustomStyles();
        setupEvents();
        loadHistoryAlerts(null, null);

        setSize(1050, 700);
        setLocationRelativeTo(null);
    }

    private void setupCustomStyles() {
        btnFilter.setBackground(UITheme.ACCENT_BLUE);
        btnFilter.setForeground(Color.WHITE);
        btnViewDetails.setBackground(UITheme.ACCENT_CYAN);
        btnViewDetails.setForeground(Color.BLACK);
        btnBack.setBackground(UITheme.BG_DARK_CARD);
        btnBack.setForeground(Color.WHITE);

        UITheme.styleTable(tblHistory);
        UITheme.styleTextField(txtSearchKey);
        tableModel = (DefaultTableModel) tblHistory.getModel();
    }

    /**
     * Khởi tạo giao diện chuẩn NetBeans GUI Builder.
     * Mở file này trong NetBeans để sử dụng tab Design kéo thả chuột!
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        contentContainer = new javax.swing.JPanel();
        filterPanel = new javax.swing.JPanel();
        lblSeverity = new javax.swing.JLabel();
        cmbSeverityFilter = new javax.swing.JComboBox<>();
        lblKeyword = new javax.swing.JLabel();
        txtSearchKey = new javax.swing.JTextField();
        btnFilter = new javax.swing.JButton();
        btnResetFilter = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        spHistory = new javax.swing.JScrollPane();
        tblHistory = new javax.swing.JTable();
        actionPanel = new javax.swing.JPanel();
        btnViewDetails = new javax.swing.JButton();
        btnExportCSV = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nhật Ký & Lịch Sử Cảnh Báo Khẩn Cấp");

        headerPanel.setBackground(new java.awt.Color(26, 26, 51));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));
        headerPanel.setLayout(new java.awt.BorderLayout());

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setForeground(new java.awt.Color(255, 77, 77));
        lblTitle.setText("NHẬT KÝ & LỊCH SỬ CẢNH BÁO KHẨN CẤP");
        headerPanel.add(lblTitle, java.awt.BorderLayout.WEST);

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBack.setText("Menu Chính");
        headerPanel.add(btnBack, java.awt.BorderLayout.EAST);

        getContentPane().add(headerPanel, java.awt.BorderLayout.PAGE_START);

        contentContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentContainer.setLayout(new java.awt.BorderLayout());

        filterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bộ Lọc & Tìm Kiếm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 13))); // NOI18N
        filterPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        lblSeverity.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSeverity.setText("Mức độ:");
        filterPanel.add(lblSeverity);

        cmbSeverityFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-- Tất cả --", "CRITICAL", "HIGH", "MEDIUM", "LOW" }));
        filterPanel.add(cmbSeverityFilter);

        lblKeyword.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblKeyword.setText("Từ khóa:");
        filterPanel.add(lblKeyword);

        txtSearchKey.setColumns(15);
        txtSearchKey.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        filterPanel.add(txtSearchKey);

        btnFilter.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnFilter.setText("Lọc Dữ Liệu");
        filterPanel.add(btnFilter);

        btnResetFilter.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnResetFilter.setText("Đặt Lại");
        filterPanel.add(btnResetFilter);

        contentContainer.add(filterPanel, java.awt.BorderLayout.NORTH);

        tablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Danh Sách Cảnh Báo Đã Lưu Hệ Thống", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 13))); // NOI18N
        tablePanel.setLayout(new java.awt.BorderLayout());

        tblHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "ID", "Mã tin", "Mức độ", "Địa điểm", "Người phát", "Thời gian tạo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spHistory.setViewportView(tblHistory);

        tablePanel.add(spHistory, java.awt.BorderLayout.CENTER);

        actionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnViewDetails.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnViewDetails.setText("Xem Chi Tiết & Phản Hồi");
        actionPanel.add(btnViewDetails);

        btnExportCSV.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnExportCSV.setText("Xuất File CSV");
        actionPanel.add(btnExportCSV);

        tablePanel.add(actionPanel, java.awt.BorderLayout.SOUTH);

        contentContainer.add(tablePanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(contentContainer, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupEvents() {
        btnBack.addActionListener(e -> {
            new MainMenuFrame(currentUser).setVisible(true);
            this.dispose();
        });

        btnFilter.addActionListener(e -> performFilter());
        btnResetFilter.addActionListener(e -> {
            cmbSeverityFilter.setSelectedIndex(0);
            txtSearchKey.setText("");
            loadHistoryAlerts(null, null);
        });

        btnViewDetails.addActionListener(e -> viewAlertDetails());
        btnExportCSV.addActionListener(e -> exportToCSV());
    }

    private void performFilter() {
        String severity = (String) cmbSeverityFilter.getSelectedItem();
        if (severity != null && severity.startsWith("--")) {
            severity = null;
        }
        String keyword = txtSearchKey.getText().trim();
        if (keyword.isEmpty()) {
            keyword = null;
        }

        loadHistoryAlerts(severity, keyword);
    }

    private void loadHistoryAlerts(String severity, String keyword) {
        tableModel.setRowCount(0);
        List<Alert> alerts = alertDAO.searchAlertsFiltered(keyword, null, severity);
        for (Alert a : alerts) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getAlertCode(),
                    a.getSeverity(),
                    a.getLocation(),
                    a.getSenderUsername() != null ? a.getSenderUsername() : "system",
                    a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""
            });
        }
    }

    private void viewAlertDetails() {
        int selectedRow = tblHistory.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cảnh báo từ bảng để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int alertId = (int) tableModel.getValueAt(selectedRow, 0);
        Alert alert = alertDAO.getAlertById(alertId);
        if (alert == null) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết bản tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<AlertReceipt> receipts = receiptDAO.getReceiptsByAlertId(alertId);

        JDialog dialog = new JDialog(this, "Chi Tiết Cảnh Báo ID #" + alertId, true);
        dialog.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        infoPanel.add(new JLabel("Mã bản tin: " + alert.getAlertCode() + " | Mức độ: " + alert.getSeverity()));
        infoPanel.add(new JLabel("Địa điểm / Tiêu đề: " + alert.getLocation()));
        infoPanel.add(new JLabel("Người phát: " + alert.getSenderUsername() + " (IP: " + alert.getSenderIp() + ")"));
        infoPanel.add(new JLabel("Thời gian tạo: " + alert.getCreatedAt()));

        JTextArea txtContent = new JTextArea(alert.getMessage());
        txtContent.setEditable(false);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane spContent = new JScrollPane(txtContent);
        spContent.setBorder(BorderFactory.createTitledBorder("Nội dung bản tin khẩn cấp"));

        String[] ackCols = {"ID Nhật ký", "Máy trạm nhận", "Địa chỉ IP", "Thời gian nhận", "Trạng thái"};
        DefaultTableModel ackModel = new DefaultTableModel(ackCols, 0);
        for (AlertReceipt r : receipts) {
            ackModel.addRow(new Object[]{
                    r.getId(),
                    r.getReceiverName(),
                    r.getReceiverIp(),
                    r.getReceivedAt() != null ? r.getReceivedAt().toString() : "",
                    r.getStatus()
            });
        }
        JTable tblAck = new JTable(ackModel);
        JScrollPane spAck = new JScrollPane(tblAck);
        spAck.setBorder(BorderFactory.createTitledBorder("Danh sách máy trạm đã phản hồi ACK (Tổng: " + receipts.size() + ")"));

        JSplitPane detailSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spContent, spAck);
        detailSplit.setDividerLocation(160);

        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(detailSplit, BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPnl.add(btnClose);
        dialog.add(btnPnl, BorderLayout.SOUTH);

        dialog.setSize(680, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu tệp CSV");
        fileChooser.setSelectedFile(new File("LichSuCanhBao.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {
                // Thêm BOM để Excel nhận diện UTF-8
                fw.write('\ufeff');
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    fw.write(tableModel.getColumnName(i) + (i == tableModel.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");

                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        Object val = tableModel.getValueAt(r, c);
                        String str = val != null ? val.toString().replace(",", " ") : "";
                        fw.write(str + (c == tableModel.getColumnCount() - 1 ? "" : ","));
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Xuất dữ liệu ra file CSV thành công!\n" + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnExportCSV;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnResetFilter;
    private javax.swing.JButton btnViewDetails;
    private javax.swing.JComboBox<String> cmbSeverityFilter;
    private javax.swing.JPanel contentContainer;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblKeyword;
    private javax.swing.JLabel lblSeverity;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane spHistory;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblHistory;
    private javax.swing.JTextField txtSearchKey;
    // End of variables declaration//GEN-END:variables
}
