package emergencybroadcast.network;

import emergencybroadcast.model.Alert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * TelegramNotifier - cầu nối đẩy cảnh báo khẩn lên Telegram.
 *
 * Cách hoạt động: MulticastReceiver (trạm Nhận đang chạy) nhận được tin
 * đạt mức khẩn (mặc định HIGH trở lên) thì gọi notifyAsync() để bot
 * gửi tin nhắn ngay, chạy nền, không treo UI.
 *
 * Cấu hình trong file telegram.properties (cạnh file chạy, KHÔNG commit):
 *   bot.token=...   (lấy từ BotFather)
 *   chat.id=...     (xem hướng dẫn lấy ở README)
 *   enabled=true
 *   min.severity=HIGH
 */
public class TelegramNotifier {

    private static String BOT_TOKEN = "";
    private static String CHAT_ID = "";
    private static boolean ENABLED = false;
    private static String MIN_SEVERITY = "HIGH";

    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 8000;

    // Chong day trung 1 tin 2 lan (may phat day + may nhan day) trong 60s
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> recentPush =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PUSH_DEDUP_MS = 60_000;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("telegram.properties")) {
            p.load(in);
        } catch (Exception ignored) {
            try (InputStream in = TelegramNotifier.class.getClassLoader().getResourceAsStream("telegram.properties")) {
                if (in != null) p.load(in);
            } catch (Exception ignored2) {
            }
        }
        BOT_TOKEN = p.getProperty("bot.token", "").trim();
        CHAT_ID = p.getProperty("chat.id", "").trim();
        ENABLED = Boolean.parseBoolean(p.getProperty("enabled", "false").trim());
        MIN_SEVERITY = p.getProperty("min.severity", "HIGH").trim().toUpperCase();
    }

    /** Nạp lại config khi người dùng vừa sửa telegram.properties mà không cần restart. */
    public static void reloadConfig() {
        loadConfig();
    }

    public static boolean isConfigured() {
        return ENABLED && !BOT_TOKEN.isEmpty() && !CHAT_ID.isEmpty();
    }

    private static int severityRank(String s) {
        if (s == null) return 0;
        switch (s.trim().toUpperCase()) {
            case "LOW": return 1;
            case "MEDIUM": return 2;
            case "HIGH": return 3;
            case "CRITICAL": return 4;
            default: return 0;
        }
    }

    /** Tin này có đủ mức khẩn để đẩy lên Telegram không. */
    public static boolean shouldNotify(Alert alert) {
        if (alert == null) return false;
        return severityRank(alert.getSeverity()) >= severityRank(MIN_SEVERITY);
    }

    /**
     * Đẩy tin lên Telegram trên luồng nền (không block luồng nhận UDP / EDT).
     */
    public static void notifyAsync(Alert alert) {
        if (alert == null) return;
        if (!shouldNotify(alert)) {
            System.out.println("-> [TELEGRAM] Bo qua tin muc " + alert.getSeverity()
                    + " (duoi nguong " + MIN_SEVERITY + " trong telegram.properties).");
            return;
        }
        if (!isConfigured()) {
            System.out.println("-> [TELEGRAM] KHONG GUI DUOC: chua cau hinh chat.id (telegram.properties)."
                    + " Lay chat.id roi restart app. enabled=" + ENABLED);
            return;
        }
        // Chong trung: dau phat va dau nhan cung goi ham nay cho 1 tin
        String code = alert.getAlertCode() != null ? alert.getAlertCode() : "";
        long now = System.currentTimeMillis();
        Long last = recentPush.get(code);
        if (!code.isEmpty() && last != null && (now - last) < PUSH_DEDUP_MS) {
            System.out.println("-> [TELEGRAM] Bo qua day trung tin: " + code);
            return;
        }
        if (!code.isEmpty()) {
            recentPush.put(code, now);
            if (recentPush.size() > 500) {
                recentPush.entrySet().removeIf(e -> (now - e.getValue()) > PUSH_DEDUP_MS);
            }
        }
        final Alert a = alert;
        new Thread(() -> {
            try {
                boolean ok = sendMessage(buildMessage(a));
                System.out.println("-> [TELEGRAM] " + (ok ? "Đã gửi tin khẩn tới chat " + CHAT_ID
                        : "Gửi thất bại, xem log lỗi ở trên."));
            } catch (Exception ex) {
                System.err.println("-> [TELEGRAM] Lỗi gửi tin: " + ex.getMessage());
            }
        }, "TelegramNotifier").start();
    }

    /** Soan noi dung tin nhan: chu thuong, khong icon de khoi loi font. */
    static String buildMessage(Alert a) {
        String sev = a.getSeverity() != null ? a.getSeverity() : "?";
        String type = a.getAlertType() != null ? a.getAlertType() : "?";
        String loc = a.getLocation() != null ? a.getLocation() : "Không xác định";
        String code = a.getAlertCode() != null ? a.getAlertCode() : "?";
        String sender = a.getSenderUsername() != null ? a.getSenderUsername() : "system";
        String ip = a.getSenderIp() != null ? a.getSenderIp() : "?";
        String msg = a.getMessage() != null ? a.getMessage() : "";
        return "[CANH BAO KHAN] " + sev + " - " + type + "\n"
                + "- Muc do: " + sev + "\n"
                + "- Loai: " + type + "\n"
                + "- Dia diem: " + loc + "\n"
                + "- Ma tin: " + code + "\n"
                + "- Nguoi phat: " + sender + " (" + ip + ")\n"
                + "- Noi dung: " + msg;
    }

    /** Gửi đồng bộ 1 tin nhắn text qua Bot API. Trả về true nếu Telegram báo ok. */
    public static boolean sendMessage(String text) throws Exception {
        String params = "chat_id=" + URLEncoder.encode(CHAT_ID, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        byte[] body = params.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(
                "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage").openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }
        String resp = readAll(conn.getInputStream());
        return resp != null && resp.contains("\"ok\":true");
    }

    /** Lấy updates gần nhất để tìm chat.id (dùng 1 lần khi setup). */
    public static String fetchUpdates() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                "https://api.telegram.org/bot" + BOT_TOKEN + "/getUpdates").openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestMethod("GET");
        return readAll(conn.getInputStream());
    }

    private static String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    /**
     * Chạy 1 lần để lấy chat.id:
     *   java -cp ... emergencybroadcast.network.TelegramNotifier getid
     * Trước đó: nhắn /start cho bot trên Telegram rồi mới chạy lệnh này.
     */
    public static void main(String[] args) {
        loadConfig();
        if (BOT_TOKEN.isEmpty()) {
            System.out.println("Chua cau hinh bot.token trong telegram.properties");
            return;
        }
        try {
            String updates = fetchUpdates();
            System.out.println("=== getUpdates (tim chat id, nhan /start cho bot truoc) ===");
            System.out.println(updates);
            System.out.println("=> Copy so trong \"chat\":{\"id\":...} vao chat.id trong telegram.properties");
        } catch (Exception ex) {
            System.err.println("Loi goi Telegram API: " + ex.getMessage());
        }
    }
}
