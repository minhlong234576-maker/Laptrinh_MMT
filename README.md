# Hệ Thống Cảnh Báo Khẩn Cấp (Emergency Broadcast)

Java Swing + UDP Multicast `239.10.10.10:5000` + MySQL (XAMPP).

## Chạy nhanh 1-click (khuyên dùng)

1. Bật **XAMPP → Start MySQL** (port 3306).
2. Mở phpMyAdmin → Import file `database.sql` (chạy lại nhiều lần không lỗi).
3. Double-click **`run.bat`** — tự biên dịch + chạy.
4. Đăng nhập:
   - `admin / admin123` (ADMIN — full quyền)
   - `operator / op123` (OPERATOR — phát tin)
   - `receiver1 / rec123` (RECEIVER — chỉ nhận tin)

Nếu đổi pass/host MySQL: sửa file **`db.properties`** (không cần sửa code).

## Phím tắt tiện ích

- `Ctrl+Q`: mở Phát nhanh khẩn cấp từ Menu/Sender
- `F1..F5`: phát Cháy / Khí độc / An ninh / Mất điện / Lũ (trong QuickAlert)
- `Ctrl+Enter`: phát cảnh báo (Sender) · `Ctrl+R`: tải lại · `Ctrl+L`: làm mới form
- `Ctrl+F / Ctrl+D / Ctrl+E`: lọc / chi tiết / xuất CSV (History) · `ESC`: đóng QuickAlert

## Luồng dùng

Phát (Sender/Quick) → tự lưu MySQL → phát UDP Multicast → máy Nhận tự hiện popup + beep + lưu receipt → Lịch sử tra cứu + xuất CSV (Excel UTF-8).

## Đẩy tin khẩn lên Telegram

Trạm phát (Sender/QuickAlert) và trạm Nhận nào đang chạy đều tự động đẩy tin đạt mức khẩn (mặc định `HIGH` trở lên) lên Telegram qua bot — máy phát đẩy ngay khi phát, không cần chờ máy nhận; chống gửi trùng 1 tin trong 60s.

1. Trên điện thoại mở Telegram, nhắn `/start` cho bot.
2. Lấy `chat.id` — chạy 1 lần (trong thư mục dự án):
   - `run.bat` biên dịch trước, rồi chạy: `java -cp "build/classes-manual;lib/mysql-connector-j-8.3.0.jar;." emergencybroadcast.network.TelegramNotifier`
   - Copy số trong `"chat":{"id":...}`.
3. Mở `telegram.properties`, dán số vào `chat.id`, lưu file.
4. Từ nay: mở 1 trạm Nhận (giữ online làm cầu nối) → có tin HIGH/CRITICAL là Telegram báo ngay.

Đổi mức lọc: sửa `min.severity` (LOW/MEDIUM/HIGH/CRITICAL). Tắt: `enabled=false`.
Cấu hình mẫu nằm ở `telegram.properties` (file riêng từng máy, không commit).

## Nhận ticket từ Telegram (bot ticket)

Người dùng nhắn tin text cho **bot ticket** là tạo 1 ticket, hiện trong màn
hình **"Thông báo Telegram"** (Menu chính với admin/bảo vệ, nút trong trạm
Nhận với receiver). Tại đó có thể: Tải lại, Đánh dấu đã xử lý, Gửi phản hồi
(bot nhắn ngược lại cho người dùng).

- **Nhóm chung:** bot xác nhận kèm nút "Gui SDT qua chat rieng" (Telegram
  chặn nút xin SĐT trong nhóm). Người dùng **gõ trực tiếp SĐT vào tin nhắn**
  (VD `mat nuoc phong 101, sdt 0912345678`) là app tự nhận + gắn đúng người
  theo user ID, kể cả nhóm đông người.
- **Chat riêng 1-1 với bot:** bot hiện nút "Chia se SDT lien he" bấm 1 chạm.

1. Mở phpMyAdmin, chạy thêm đoạn `telegram_tickets` trong `database.sql`
   (bảng `CREATE TABLE IF NOT EXISTS`, chạy lại không lỗi).
2. Người dùng nhắn `/start` rồi nhắn nội dung sự cố cho bot ticket.
3. Mở app: luồng poll nền tự chạy khi vào Menu/Receiver, ticket mới tự
   hiện + badge "Thông báo Telegram (N mới)".
4. Cấu hình trong `telegram.properties`: `ticket.bot.token`,
   `inbox.enabled=true`, `inbox.poll.seconds=10`.

## Khắc phục sự cố

- `Không kết nối được MySQL`: kiểm tra XAMPP đã Start MySQL chưa, đúng `db.properties` chưa.
- `Lỗi lưu DB`: xem console log `Lỗi thực thi insertAlert()` — thường do trùng `alert_code` (bản mới đã sinh mã duy nhất) hoặc chưa import `database.sql`.
- Multicast không qua: thử 2 app trên cùng 1 máy trước (loopback đã bật), tắt firewall cho UDP 5000, cùng LAN.
