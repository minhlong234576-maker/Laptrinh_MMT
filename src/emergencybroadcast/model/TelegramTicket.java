package emergencybroadcast.model;

import java.sql.Timestamp;

/**
 * Model ticket do người dùng tạo bằng cách nhắn tin cho bot Telegram.
 * Tương ứng bảng 'telegram_tickets' trong CSDL MySQL.
 */
public class TelegramTicket {

    private int id;
    private long updateId;      // update_id Telegram (chống trùng)
    private String chatId;      // chat Telegram của người gửi
    private String senderName;  // tên hiển thị người gửi
    private String username;    // @username (mở t.me/username là chat được ngay)
    private String userId;      // Telegram user id (dự phòng khi không có username)
    private String phone;       // SĐT người dùng tự chia sẻ qua nút contact
    private String room;        // Số phòng trích từ tin nhắn (VD 101, B1)
    private String message;     // nội dung ticket
    private Timestamp createdAt;
    private String status;      // 'MOI', 'DA_XU_LY'

    public TelegramTicket() {
    }

    public TelegramTicket(long updateId, String chatId, String senderName, String message) {
        this.updateId = updateId;
        this.chatId = chatId;
        this.senderName = senderName;
        this.message = message;
        this.status = "MOI";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getUpdateId() { return updateId; }
    public void setUpdateId(long updateId) { this.updateId = updateId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
