package emergencybroadcast.dao;

import emergencybroadcast.database.DatabaseConnection;
import emergencybroadcast.model.TelegramTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng 'telegram_tickets' (ticket người dùng tạo qua bot Telegram).
 */
public class TelegramTicketDAO {

    /** Lưu ticket mới, bỏ qua nếu update_id đã có (chống trùng khi poll lặp). */
    public boolean insertIgnore(TelegramTicket t) {
        String sql = "INSERT IGNORE INTO telegram_tickets (update_id, chat_id, sender_name, username, user_id, phone, room, message, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getUpdateId());
            ps.setString(2, t.getChatId());
            ps.setString(3, t.getSenderName());
            ps.setString(4, t.getUsername());
            ps.setString(5, t.getUserId());
            ps.setString(6, t.getPhone());
            ps.setString(7, t.getRoom());
            ps.setString(8, t.getMessage());
            ps.setString(9, t.getStatus() != null ? t.getStatus() : "MOI");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) t.setId(keys.getInt(1));
                }
                return true;
            }
            return false; // trùng update_id -> đã lưu trước đó
        } catch (SQLException e) {
            System.err.println("Loi insertIgnore ticket: " + e.getMessage());
            return false;
        }
    }

    public List<TelegramTicket> getAllTickets() {
        List<TelegramTicket> list = new ArrayList<>();
        String sql = "SELECT id, update_id, chat_id, sender_name, username, user_id, phone, room, message, created_at, status FROM telegram_tickets ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Loi getAllTickets: " + e.getMessage());
        }
        return list;
    }

    public int countUnread() {
        String sql = "SELECT COUNT(*) FROM telegram_tickets WHERE status = 'MOI'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Loi countUnread: " + e.getMessage());
        }
        return 0;
    }

    public long getMaxUpdateId() {
        String sql = "SELECT MAX(update_id) FROM telegram_tickets";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            System.err.println("Loi getMaxUpdateId: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gắn SĐT vào ticket gần nhất CỦA ĐÚNG NGƯỜI gửi (quan trọng trong nhóm chung
     * vì chat_id chung cho mọi người). Nếu người đó chưa có ticket thì tạo dòng mới.
     * @return true nếu có gì thay đổi (để gửi xác nhận)
     */
    public boolean attachPhone(TelegramTicket t) {
        // 1. Ticket gan nhat cua dung nguoi nay
        Integer targetId = null;
        if (t.getUserId() != null && !t.getUserId().trim().isEmpty()) {
            targetId = latestId("SELECT id FROM telegram_tickets WHERE chat_id = ? AND user_id = ? ORDER BY id DESC LIMIT 1",
                    t.getChatId(), t.getUserId());
        }
        // 2. Fallback: ticket gan nhat cua chat mà chua ro nguoi (user_id trong)
        if (targetId == null) {
            targetId = latestId("SELECT id FROM telegram_tickets WHERE chat_id = ? AND (user_id IS NULL OR user_id = '') ORDER BY id DESC LIMIT 1",
                    t.getChatId(), null);
        }
        if (targetId != null) {
            String upd = "UPDATE telegram_tickets SET phone = ?, sender_name = ?, username = ?, user_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setString(1, t.getPhone());
                ps.setString(2, t.getSenderName());
                ps.setString(3, t.getUsername());
                ps.setString(4, t.getUserId());
                ps.setInt(5, targetId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Loi attachPhone: " + e.getMessage());
            }
            return false;
        }
        // 3. Nguoi nay chua co ticket nao -> luu dong moi ghi nhan SDT
        return insertIgnore(t);
    }

    private Integer latestId(String sql, String p1, String p2) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p1);
            if (p2 != null) ps.setString(2, p2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Loi latestId: " + e.getMessage());
        }
        return null;
    }

    public boolean markResolved(int id) {
        String sql = "UPDATE telegram_tickets SET status = 'DA_XU_LY' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi markResolved: " + e.getMessage());
            return false;
        }
    }

    private TelegramTicket map(ResultSet rs) throws SQLException {
        TelegramTicket t = new TelegramTicket();
        t.setId(rs.getInt("id"));
        t.setUpdateId(rs.getLong("update_id"));
        t.setChatId(rs.getString("chat_id"));
        t.setSenderName(rs.getString("sender_name"));
        try { t.setUsername(rs.getString("username")); } catch (SQLException ignored) { }
        try { t.setUserId(rs.getString("user_id")); } catch (SQLException ignored) { }
        try { t.setPhone(rs.getString("phone")); } catch (SQLException ignored) { }
        try { t.setRoom(rs.getString("room")); } catch (SQLException ignored) { }
        t.setMessage(rs.getString("message"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        t.setStatus(rs.getString("status"));
        return t;
    }
}
