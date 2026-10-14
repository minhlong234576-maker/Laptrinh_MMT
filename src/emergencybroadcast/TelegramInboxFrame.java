package emergencybroadcast;

import emergencybroadcast.dao.TelegramTicketDAO;
import emergencybroadcast.model.TelegramTicket;
import emergencybroadcast.model.User;
import emergencybroadcast.network.TelegramInboxService;
import emergencybroadcast.ui.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Man hinh "Thong bao tu Telegram" - danh sach ticket nguoi dung tao
 * bang cach nhan tin cho bot ticket. Dung chung cho ADMIN / OPERATOR
 * (bao ve) / RECEIVER. Tu refresh khi co ticket moi.
 */
public class TelegramInboxFrame extends javax.swing.JFrame {

    private final User currentUser;
    private final TelegramTicketDAO ticketDAO;
    private final TelegramInboxService inboxService;
    private final java.util.List<TelegramTicket> current = new java.util.ArrayList<>();
    private DefaultTableModel tableModel;
    private JLabel lblStatusBar;
    private JLabel lblUnread;
    private JTable tblTickets;
    private JScrollPane spTable;
    private JButton btnRefresh;
    private JButton btnResolve;
    private JButton btnReply;
    private JButton btnContact;
    private javax.swing.Timer clockTimer;
    private TelegramInboxService.OnTicketsChangedListener inboxListener;

    public TelegramInboxFrame(User user) {
        this.currentUser = user;
        this.ticketDAO = new TelegramTicketDAO();
        this.inboxService = TelegramInboxService.getInstance();
        this.inboxService.start(); // dam bao luong poll dang chay
        buildUI();
        loadTickets();
        wireEvents();
        setTitle("Thong bao tu Telegram - Ticket nguoi dung");
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(26, 26, 51));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lblTitle = new JLabel("THONG BAO TU TELEGRAM (TICKET NGUOI DUNG)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(255, 77, 77));
        header.add(lblTitle, BorderLayout.WEST);
        lblUnread = new JLabel("Chua doc: 0");
        lblUnread.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUnread.setForeground(UITheme.ACCENT_AMBER);
        header.add(lblUnread, BorderLayout.EAST);
        getContentPane().add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] { "ID", "Nguoi gui", "Lien he", "SDT", "Phong", "Chat ID", "Noi dung", "Thoi gian", "Trang thai" }) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTickets = new JTable(tableModel);
        UITheme.styleTable(tblTickets);
        tblTickets.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    Object st = table.getModel().getValueAt(row, 8);
                    boolean isNew = "MOI".equalsIgnoreCase(st != null ? st.toString() : "");
                    c.setBackground(isNew ? new Color(70, 60, 10)
                            : (row % 2 == 0 ? UITheme.BG_DARK_SECONDARY : UITheme.BG_DARK_PRIMARY));
                    c.setForeground(isNew ? Color.WHITE : UITheme.TEXT_PRIMARY);
                    if (column == 8 && isNew) {
                        c.setForeground(UITheme.ACCENT_AMBER);
                        ((JLabel) c).setFont(UITheme.FONT_BODY_BOLD);
                    }
                }
                return c;
            }
        });
        spTable = new JScrollPane(tblTickets);
        spTable.setBorder(BorderFactory.createTitledBorder("Danh sach ticket (moi nhat truoc)"));
        getContentPane().add(spTable, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRefresh = new JButton("Tai lai");
        btnResolve = new JButton("Danh dau da xu ly");
        btnReply = new JButton("Gui phan hoi");
        btnContact = new JButton("Lien he");
        btnRefresh.setToolTipText("Tai lai danh sach");
        btnResolve.setToolTipText("Chuyen ticket sang DA_XU_LY");
        btnReply.setToolTipText("Gui tin nhan phan hoi toi nguoi dung qua bot");
        btnContact.setToolTipText("Mo chat Telegram truc tiep voi nguoi gui");
        btnPanel.add(btnRefresh);
        btnPanel.add(btnResolve);
        btnPanel.add(btnReply);
        btnPanel.add(btnContact);
        bottom.add(btnPanel, BorderLayout.NORTH);

        lblStatusBar = new JLabel(" San sang");
        lblStatusBar.setFont(UITheme.FONT_SMALL);
        lblStatusBar.setForeground(UITheme.TEXT_SECONDARY);
        lblStatusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        lblStatusBar.setOpaque(true);
        lblStatusBar.setBackground(UITheme.BG_DARK_PRIMARY);
        bottom.add(lblStatusBar, BorderLayout.SOUTH);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        btnRefresh.addActionListener(e -> loadTickets());
        btnResolve.addActionListener(e -> resolveSelected());
        btnReply.addActionListener(e -> replySelected());
        btnContact.addActionListener(e -> contactSelected());

        inboxListener = unread -> loadTickets();
        inboxService.addListener(inboxListener);

        clockTimer = new javax.swing.Timer(5000, e -> updateStatusBar());
        clockTimer.start();
    }

    private void loadTickets() {
        new SwingWorker<List<TelegramTicket>, Void>() {
            @Override protected List<TelegramTicket> doInBackground() {
                return ticketDAO.getAllTickets();
            }
            @Override protected void done() {
                try {
                    List<TelegramTicket> list = get();
                    current.clear();
                    current.addAll(list);
                    tableModel.setRowCount(0);
                    int unread = 0;
                    for (TelegramTicket t : list) {
                        if ("MOI".equalsIgnoreCase(t.getStatus())) unread++;
                        tableModel.addRow(new Object[] {
                            t.getId(), t.getSenderName(), contactText(t),
                            t.getPhone() != null ? t.getPhone() : "",
                            t.getRoom() != null ? t.getRoom() : "",
                            t.getChatId(), t.getMessage(),
                            t.getCreatedAt() != null ? t.getCreatedAt().toString() : "",
                            t.getStatus()
                        });
                    }
                    lblUnread.setText("Chua doc: " + unread);
                    updateStatusBar();
                } catch (Exception ex) {
                    lblStatusBar.setText(" Loi tai ticket: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateStatusBar() {
        lblStatusBar.setText(" " + inboxService.getLastCheckInfo()
                + " | Tong: " + tableModel.getRowCount() + " ticket"
                + " | Nguoi dung tao ticket bang cach nhan tin cho bot ticket");
    }

    /** Chuoi hien thi cot lien he: @username (bam nut Lien he de mo chat). */
    private static String contactText(TelegramTicket t) {
        if (t.getUsername() != null && !t.getUsername().trim().isEmpty()) return t.getUsername().trim();
        if (t.getUserId() != null && !t.getUserId().trim().isEmpty()) return "id:" + t.getUserId().trim();
        return "(khong co)";
    }

    private TelegramTicket selectedTicket() {
        int row = tblTickets.getSelectedRow();
        if (row < 0 || row >= current.size()) return null;
        return current.get(row);
    }

    private void resolveSelected() {
        TelegramTicket t = selectedTicket();
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Chon 1 ticket de danh dau!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ticketDAO.markResolved(t.getId())) {
            loadTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Khong the cap nhat!", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void replySelected() {
        TelegramTicket t = selectedTicket();
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Chon 1 ticket de phan hoi!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String chatId = t.getChatId();
        String who = t.getSenderName();
        String text = JOptionPane.showInputDialog(this,
                "Phan hoi toi " + who + " (chat " + chatId + "):",
                "Gui phan hoi qua Telegram", JOptionPane.PLAIN_MESSAGE);
        if (text == null || text.trim().isEmpty()) return;
        String me = currentUser != null ? currentUser.getUsername() : "trung tam";
        inboxService.replyAsync(chatId, "[Phan hoi tu " + me + "] " + text.trim());
        JOptionPane.showMessageDialog(this, "Da gui phan hoi!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Mo chat Telegram truc tiep voi nguoi gui, uu tien copy SDT neu co.
     */
    private void contactSelected() {
        TelegramTicket t = selectedTicket();
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Chon 1 ticket de lien he!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Co SDT -> copy san vao clipboard de goi ngay
        if (t.getPhone() != null && !t.getPhone().trim().isEmpty()) {
            String phone = t.getPhone().trim();
            try {
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(phone), null);
            } catch (Exception ignored) {
            }
            int go = JOptionPane.showConfirmDialog(this,
                    "SDT " + t.getSenderName() + ": " + phone + "\n(Da copy, bam OK de mo chat Telegram)",
                    "Lien he", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (go != JOptionPane.OK_OPTION) return;
        }
        String url = null;
        if (t.getUsername() != null && !t.getUsername().trim().isEmpty()) {
            url = "https://t.me/" + t.getUsername().trim().replaceFirst("^@", "");
        } else if (t.getUserId() != null && !t.getUserId().trim().isEmpty()) {
            url = "tg://user?id=" + t.getUserId().trim();
        }
        if (url == null) {
            JOptionPane.showMessageDialog(this, "Ticket nay khong co thong tin lien he!", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "Link lien he: " + url, "Lien he", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Khong mo duoc link, copy tay: " + url, "Loi", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (clockTimer != null) clockTimer.stop();
        if (inboxListener != null) inboxService.removeListener(inboxListener);
        super.dispose();
    }
}
