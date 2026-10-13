package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastSender;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
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
    // Thêm các component mới ngoài GEN-BEGIN block
    private JComboBox<String> cmbAlertType;
    private JTextField txtLocation;
    private JLabel lblStatusBar;

    public SenderFrame(User user) {
        this.currentUser = user;
        this.alertDAO = new AlertDAO();
        initComponents();
        setupCustomStyles();
        setupEvents();
        loadAlertHistory();

        setSize(1050, 770);
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

        // ---- Thêm ComboBox Loại Sự Cố ----
        cmbAlertType = new JComboBox<>(new String[]{
            "🔥 FIRE - Cháy nổ",
            "🔒 SECURITY - An ninh",
            "⚡ POWER - Mất điện",
            "☠️ GAS - Rò khí độc",
            "🌊 FLOOD - Lũ lụt",
            "📢 OTHER - Khác"
        });
        cmbAlertType.setFont(UITheme.FONT_BODY);
        cmbAlertType.setBackground(UITheme.BG_DARK_SECONDARY);
        cmbAlertType.setForeground(UITheme.TEXT_PRIMARY);

        // ---- Thêm field Địa Điểm ----
        txtLocation = new JTextField();
        UITheme.styleTextField(txtLocation);
        txtLocation.setToolTipText("Ví dụ: Tầng 3 - Phòng 302");

        // Chèn vào formPanel trước Severity (index 0)
        JLabel lblType = new JLabel("Loại sự cố:");
        lblType.setFont(UITheme.FONT_BODY_BOLD);
        lblType.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblLoc = new JLabel("Địa điểm xảy ra:");
        lblLoc.setFont(UITheme.FONT_BODY_BOLD);
        lblLoc.setForeground(UITheme.TEXT_PRIMARY);

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.insets = new java.awt.Insets(10, 10, 5, 10);

        // Xây lại thứ tự bằng cách thêm vào đúng vị trí
        // Thêm label + combo type ngay trước lblSeverity
        java.awt.GridBagConstraints gbcType1 = new java.awt.GridBagConstraints();
        gbcType1.gridx = 0; gbcType1.gridy = 0;
        gbcType1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcType1.weightx = 1.0;
        gbcType1.insets = new java.awt.Insets(10, 10, 5, 10);

        java.awt.GridBagConstraints gbcType2 = new java.awt.GridBagConstraints();
        gbcType2.gridx = 0; gbcType2.gridy = 1;
        gbcType2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcType2.weightx = 1.0;
        gbcType2.insets = new java.awt.Insets(0, 10, 10, 10);

        // Shift tất cả component hiện có xuống 4 row
        for (Component c : formPanel.getComponents()) {
            java.awt.GridBagLayout gbl = (java.awt.GridBagLayout) formPanel.getLayout();
            java.awt.GridBagConstraints exist = gbl.getConstraints(c);
            exist.gridy += 4;
            gbl.setConstraints(c, exist);
        }
        // Cập nhật lại lblTitle (tiêu đề bản tin) thành "Tiêu đề / Nội dung tóm tắt"
        lblAlertTitle.setText("Tóm tắt sự cố (tiêu đề):");

        formPanel.add(lblType, gbcType1);
        formPanel.add(cmbAlertType, gbcType2);

        java.awt.GridBagConstraints gbcLoc1 = new java.awt.GridBagConstraints();
        gbcLoc1.gridx = 0; gbcLoc1.gridy = 2;
        gbcLoc1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcLoc1.weightx = 1.0;
        gbcLoc1.insets = new java.awt.Insets(0, 10, 5, 10);

        java.awt.GridBagConstraints gbcLoc2 = new java.awt.GridBagConstraints();
        gbcLoc2.gridx = 0; gbcLoc2.gridy = 3;
        gbcLoc2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbcLoc2.weightx = 1.0;
        gbcLoc2.insets = new java.awt.Insets(0, 10, 10, 10);

        formPanel.add(lblLoc, gbcLoc1);
        formPanel.add(txtLocation, gbcLoc2);

        formPanel.revalidate();
        formPanel.repaint();

        // ---- Tô màu severity trong bảng ----
        applySeverityColorRenderer();

        // ---- Status Bar ----
        lblStatusBar = new JLabel();
        lblStatusBar.setFont(UITheme.FONT_SMALL);
        lblStatusBar.setForeground(UITheme.TEXT_SECONDARY);
        lblStatusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        lblStatusBar.setOpaque(true);
        lblStatusBar.setBackground(UITheme.BG_DARK_PRIMARY);
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String user = currentUser != null ? currentUser.getUsername() + " (" + currentUser.getRole() + ")" : "?";
            lblStatusBar.setText("  🖥 IP: " + ip + "   |   👤 " + user + "   |   🌐 Multicast: 239.10.10.10:5000   |   Ctrl+Q = Phát nhanh khẩn cấp");
        } catch (Exception e) {
            lblStatusBar.setText("Trạng thái hệ thống");
        }
        getContentPane().add(lblStatusBar, BorderLayout.PAGE_END);

        // ---- Nút PHÁT NHANH thêm vào header ----
        JButton btnQuickShortcut = new JButton("PHAT NHANH");
        btnQuickShortcut.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickShortcut.setBackground(new Color(180, 20, 20));
        btnQuickShortcut.setForeground(Color.WHITE);
        btnQuickShortcut.setFocusPainted(false);
        btnQuickShortcut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQuickShortcut.setToolTipText("Mở màn hình phát khẩn cấp 1-click (Ctrl+Q)");
        btnQuickShortcut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 80, 80), 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btnQuickShortcut.addActionListener(e -> new QuickAlertFrame(currentUser).setVisible(true));
        btnQuickShortcut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnQuickShortcut.setBackground(new Color(220, 30, 30));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnQuickShortcut.setBackground(new Color(180, 20, 20));
            }
        });

        // Thêm vào headerPanel cạnh nút Back (tạo sub-panel)
        Component backComp = headerPanel.getComponent(1); // btnBack
        headerPanel.remove(backComp);
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        headerRight.add(btnQuickShortcut);
        headerRight.add(btnBack);
        headerPanel.add(headerRight, java.awt.BorderLayout.EAST);
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
        btnSendAlert.setText("PHÁT CẢNH BÁO");
        formButtonPanel.add(btnSendAlert);

        btnClearForm.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnClearForm.setText("Làm Mới");
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
                "ID", "Mã tin", "Loại", "Mức độ", "Địa điểm", "Thời gian"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
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
        btnRefreshTable.setText("Tải Lại");
        tableButtonPanel.add(btnRefreshTable);

        btnDeleteAlert.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnDeleteAlert.setText("Xóa Bản Tin");
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
        btnDeleteAlert.addActionListener(e -> deleteSelectedAlert());

        // ---- Phím tắt ----
        // Ctrl+Enter hoặc Alt+S = Gửi cảnh báo
        getRootPane().registerKeyboardAction(
            e -> performSendAlert(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        // Ctrl+R = Tải lại bảng
        getRootPane().registerKeyboardAction(
            e -> loadAlertHistory(),
            KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        // Ctrl+L = Làm mới form
        getRootPane().registerKeyboardAction(
            e -> clearForm(),
            KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Ctrl+Q = mở QuickAlertFrame ngay từ SenderFrame
        getRootPane().registerKeyboardAction(
            e -> new QuickAlertFrame(currentUser).setVisible(true),
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Tooltip phím tắt
        btnSendAlert.setToolTipText("Phát cảnh báo (Ctrl+Enter)");
        btnRefreshTable.setToolTipText("Tải lại danh sách (Ctrl+R)");
        btnClearForm.setToolTipText("Làm mới form (Ctrl+L)");
    }

    private void clearForm() {
        txtAlertTitle.setText("");
        txtContent.setText("");
        if (txtLocation != null) txtLocation.setText("");
        cmbSeverity.setSelectedIndex(0);
        if (cmbAlertType != null) cmbAlertType.setSelectedIndex(0);
        txtAlertTitle.requestFocus();
    }

    private void performSendAlert() {
        String title = txtAlertTitle.getText().trim();
        String content = txtContent.getText().trim();
        String location = (txtLocation != null) ? txtLocation.getText().trim() : title;

        String severitySel = (String) cmbSeverity.getSelectedItem();
        String severity = "CRITICAL";
        if (severitySel != null) {
            if (severitySel.contains("HIGH")) severity = "HIGH";
            else if (severitySel.contains("MEDIUM")) severity = "MEDIUM";
            else if (severitySel.contains("LOW")) severity = "LOW";
        }

        // Lấy loại sự cố từ ComboBox mới
        String alertType = "SECURITY";
        if (cmbAlertType != null) {
            String typeSel = (String) cmbAlertType.getSelectedItem();
            if (typeSel != null) {
                if (typeSel.contains("FIRE")) alertType = "FIRE";
                else if (typeSel.contains("POWER")) alertType = "POWER";
                else if (typeSel.contains("GAS")) alertType = "GAS";
                else if (typeSel.contains("FLOOD")) alertType = "FLOOD";
                else if (typeSel.contains("OTHER")) alertType = "OTHER";
                else alertType = "SECURITY";
            }
        }

        // Validation
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề / tóm tắt sự cố!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtAlertTitle.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập địa điểm xảy ra sự cố!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtLocation.requestFocus();
            return;
        }
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung chi tiết cảnh báo!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtContent.requestFocus();
            return;
        }

        // Xác nhận trước khi phát CRITICAL
        if ("CRITICAL".equals(severity)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠️ Bạn sắp phát cảnh báo mức CRITICAL!\nThao tác này sẽ NGAY LẬP TỨC phát đến tất cả máy trạm.\n\nXác nhận phát?",
                "Xác nhận phát cảnh báo CRITICAL", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        // Loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnSendAlert.setEnabled(false);
        btnSendAlert.setText("Đang phát...");

        String finalSeverity = severity;
        String finalAlertType = alertType;
        String finalLocation = location;
        String finalTitle = title;
        String finalContent = content;

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                String alertCode = "ALT-" + System.currentTimeMillis() % 100000;
                String senderUser = currentUser != null ? currentUser.getUsername() : "admin";
                String senderIp = "127.0.0.1";
                try { senderIp = InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignored) {}

                Alert alert = new Alert();
                alert.setAlertCode(alertCode);
                alert.setAlertType(finalAlertType);
                alert.setSeverity(finalSeverity);
                alert.setLocation(finalLocation);
                alert.setMessage(finalTitle + " - " + finalContent);
                alert.setSenderUsername(senderUser);
                alert.setSenderIp(senderIp);

                int alertId = alertDAO.insertAlert(alert);
                if (alertId > 0) {
                    alert.setId(alertId);
                    boolean sent = MulticastSender.sendAlert(alert);
                    return sent ? alertId : -2;
                }
                return -1;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnSendAlert.setEnabled(true);
                btnSendAlert.setText("PHÁT CẢNH BÁO");
                try {
                    int result = get();
                    if (result > 0) {
                        JOptionPane.showMessageDialog(SenderFrame.this,
                            "✅ Đã phát cảnh báo thành công qua UDP Multicast!\n" +
                            "Loại: " + finalAlertType + " | Mức độ: " + finalSeverity + "\n" +
                            "Địa điểm: " + finalLocation,
                            "Phát Thành Công", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        loadAlertHistory();
                    } else if (result == -2) {
                        JOptionPane.showMessageDialog(SenderFrame.this,
                            "⚠️ Cảnh báo đã lưu vào DB nhưng lỗi phát UDP Multicast!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        loadAlertHistory();
                    } else {
                        JOptionPane.showMessageDialog(SenderFrame.this,
                            "❌ Không thể lưu cảnh báo vào cơ sở dữ liệu!",
                            "Lỗi DB", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(SenderFrame.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loadAlertHistory() {
        tableModel.setRowCount(0);
        List<Alert> alerts = alertDAO.getAllAlerts();
        for (Alert a : alerts) {
            tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getAlertCode(),
                    a.getAlertType() != null ? a.getAlertType() : "",
                    a.getSeverity(),
                    a.getLocation(),
                    a.getCreatedAt() != null ? a.getCreatedAt().toString() : ""
            });
        }
    }

    /**
     * Tô màu dòng bảng theo mức độ severity.
     */
    private void applySeverityColorRenderer() {
        tblSentAlerts.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    // Lấy giá trị severity từ cột 3 (Mức độ)
                    Object sev = table.getModel().getValueAt(row, 3);
                    String severity = sev != null ? sev.toString() : "";
                    Color rowBg;
                    switch (severity.toUpperCase()) {
                        case "CRITICAL": rowBg = new Color(80, 20, 20); break;
                        case "HIGH":     rowBg = new Color(80, 45, 15); break;
                        case "MEDIUM":   rowBg = new Color(70, 60, 10); break;
                        case "LOW":      rowBg = new Color(15, 60, 30); break;
                        default:         rowBg = (row % 2 == 0) ? UITheme.BG_DARK_SECONDARY : UITheme.BG_DARK_PRIMARY;
                    }
                    c.setBackground(rowBg);
                    c.setForeground(UITheme.TEXT_PRIMARY);
                }
                return c;
            }
        });
    }

    private void deleteSelectedAlert() {
        int selectedRow = tblSentAlerts.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản tin để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bản tin cảnh báo này không? Thao tác này sẽ xóa cả lịch sử phản hồi liên quan.", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int alertId = (int) tableModel.getValueAt(selectedRow, 0);
            boolean success = alertDAO.deleteAlert(alertId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa bản tin thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAlertHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa bản tin. Có lỗi xảy ra!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
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
