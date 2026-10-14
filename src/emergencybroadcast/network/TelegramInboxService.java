package emergencybroadcast.network;

import emergencybroadcast.dao.TelegramTicketDAO;
import emergencybroadcast.model.TelegramTicket;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TelegramInboxService - luồng nền "luôn lắng nghe" bot ticket.
 *
 * Người dùng nhắn bất kỳ tin text nào cho bot ticket là tạo 1 ticket,
 * service poll getUpdates, lưu vào bảng telegram_tickets (chống trùng
 * bằng update_id), rồi báo cho các màn hình đang mở tự refresh.
 *
 * Cau hinh trong telegram.properties (KHONG commit):
 *   ticket.bot.token=...
 *   inbox.enabled=true
 *   inbox.poll.seconds=10
 */
public class TelegramInboxService {

    public interface OnTicketsChangedListener {
        void onTicketsChanged(int unreadCount);
    }

    private static TelegramInboxService instance;

    private String ticketToken = "";
    private String ticketUsername = "";
    private boolean enabled = false;
    private int pollSeconds = 10;
    // Che do chi nhan tin co so phong (loc tin buon chuyen trong nhom chung)
    private boolean roomOnly = true;
    private boolean roomHint = true;

    private final TelegramTicketDAO dao = new TelegramTicketDAO();
    private final CopyOnWriteArrayList<OnTicketsChangedListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;
    private volatile long lastOffset = 0;
    private volatile String lastCheckInfo = "Chua kiem tra";

    private TelegramInboxService() {
        loadConfig();
    }

    public static synchronized TelegramInboxService getInstance() {
        if (instance == null) instance = new TelegramInboxService();
        return instance;
    }

    private void loadConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("telegram.properties")) {
            p.load(in);
        } catch (Exception ignored) {
            try (InputStream in = TelegramInboxService.class.getClassLoader().getResourceAsStream("telegram.properties")) {
                if (in != null) p.load(in);
            } catch (Exception ignored2) {
            }
        }
        ticketToken = p.getProperty("ticket.bot.token", "").trim();
        ticketUsername = p.getProperty("ticket.bot.username", "").trim();
        if (ticketUsername.startsWith("@")) ticketUsername = ticketUsername.substring(1);
        roomOnly = Boolean.parseBoolean(p.getProperty("inbox.room.only", "true").trim());
        roomHint = Boolean.parseBoolean(p.getProperty("inbox.room.hint", "true").trim());
        enabled = Boolean.parseBoolean(p.getProperty("inbox.enabled", "false").trim());
        try {
            pollSeconds = Math.max(5, Integer.parseInt(p.getProperty("inbox.poll.seconds", "10").trim()));
        } catch (NumberFormatException e) {
            pollSeconds = 10;
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public boolean isConfigured() {
        return enabled && !ticketToken.isEmpty();
    }

    public String getLastCheckInfo() {
        return lastCheckInfo;
    }

    public void addListener(OnTicketsChangedListener l) {
        if (l != null) listeners.addIfAbsent(l);
    }

    public void removeListener(OnTicketsChangedListener l) {
        listeners.remove(l);
    }

    /** Bat vong poll nen (goi nhieu lan cung chi chay 1 thread). */
    public synchronized void start() {
        if (running) return;
        if (!isConfigured()) {
            System.out.println("-> [TELEGRAM INBOX] Chua cau hinh ticket bot (xem telegram.properties), khong poll.");
            return;
        }
        running = true;
        lastOffset = dao.getMaxUpdateId() + 1;
        Thread t = new Thread(this::pollLoop, "TelegramInboxPoller");
        t.setDaemon(true);
        t.start();
        System.out.println("-> [TELEGRAM INBOX] Bat dau lang nghe bot ticket (offset=" + lastOffset + ").");
    }

    public synchronized void stop() {
        running = false;
    }

    private void pollLoop() {
        while (running) {
            try {
                // Long-poll: treo toi da ~20s neu chua co tin moi
                String json = httpGet("https://api.telegram.org/bot" + ticketToken
                        + "/getUpdates?offset=" + lastOffset + "&timeout=20");
                List<TelegramTicket> news = parseUpdates(json);
                int added = 0;
                for (TelegramTicket t : news) {
                    // Tin /start (mo tu nut chat rieng) -> gui nut xin SDT, khong luu ticket rac
                    if (t.getMessage() != null && t.getMessage().startsWith("/start")) {
                        try {
                            replyWithKeyboard(t.getChatId(),
                                    "Bam nut ben duoi de gui SDT giup trung tam lien he nhanh hon.",
                                    ASK_PHONE_MARKUP);
                        } catch (Exception ex) {
                            System.err.println("-> [TELEGRAM INBOX] Loi gui nut SDT: " + ex.getMessage());
                        }
                        if (t.getUpdateId() >= lastOffset) lastOffset = t.getUpdateId() + 1;
                        continue;
                    }
                    // Che do loc: tin khong co so phong thi bo (khong tao ticket rac)
                    boolean noRoom = t.getRoom() == null || t.getRoom().trim().isEmpty();
                    if (roomOnly && noRoom
                            && !(t.getMessage() != null && t.getMessage().startsWith("(Da chia se"))) {
                        System.out.println("-> [TELEGRAM INBOX] Bo tin khong co so phong: "
                                + preview(t.getMessage()));
                        if (roomHint) {
                            try {
                                reply(t.getChatId(), "[Trung tam] Tin chua co so phong nen chua tao ticket. "
                                        + "Vui long gui lai kem so phong, VD: mat nuoc phong 101, sdt 0912345678.");
                            } catch (Exception ex) {
                                System.err.println("-> [TELEGRAM INBOX] Loi gui goi y: " + ex.getMessage());
                            }
                        }
                        if (t.getUpdateId() >= lastOffset) lastOffset = t.getUpdateId() + 1;
                        continue;
                    }
                    // Tin chia se contact (nut SDT) -> gan vao ticket gan nhat, khong tao ticket rac.
                    // Tin text (ke ca co go SDT) -> luu ticket moi (cot phone luu kem).
                    boolean isPhoneShare = t.getMessage() != null && t.getMessage().startsWith("(Da chia se");
                    boolean changed = isPhoneShare ? dao.attachPhone(t) : dao.insertIgnore(t);
                    if (changed) {
                        added++;
                        boolean hasPhone = t.getPhone() != null && !t.getPhone().trim().isEmpty();
                        // Xac nhan ngay trong chat de nguoi dung biet ticket da toi trung tam
                        try {
                            if (isPhoneShare) {
                                reply(t.getChatId(), "[Trung tam] Da luu SDT lien he "
                                        + t.getPhone() + " cho ticket gan nhat cua ban.");
                            } else if (hasPhone) {
                                reply(t.getChatId(), "[Trung tam] Da nhan ticket #" + t.getId()
                                        + " kem SDT " + t.getPhone()
                                        + ". Nhan vien se lien he som nhat.");
                            } else if (isGroupChat(t.getChatId())) {
                                // Nhom chung: nut contact khong chay -> nut mo chat rieng
                                replyWithKeyboard(t.getChatId(),
                                        "[Trung tam] Da nhan ticket #" + t.getId()
                                        + ". Noi dung: " + preview(t.getMessage())
                                        + ". Nhan vien se xu ly som nhat. "
                                        + "Nhan nut ben duoi de gui SDT (go truc tiep SDT vao day cung duoc).",
                                        privateChatMarkup());
                            } else {
                                // Chat rieng: nut xin SDT 1 cham
                                replyWithKeyboard(t.getChatId(),
                                        "[Trung tam] Da nhan ticket #" + t.getId()
                                        + ". Noi dung: " + preview(t.getMessage())
                                        + ". Nhan vien se xu ly som nhat. "
                                        + "Bam nut ben duoi de gui SDT giup trung tam lien he nhanh hon.",
                                        ASK_PHONE_MARKUP);
                            }
                        } catch (Exception ex) {
                            System.err.println("-> [TELEGRAM INBOX] Loi gui xac nhan: " + ex.getMessage());
                        }
                    }
                    if (t.getUpdateId() >= lastOffset) lastOffset = t.getUpdateId() + 1;
                }
                lastCheckInfo = "Kiem tra luc " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date())
                        + " | moi: " + added;
                if (added > 0) fireChanged();
            } catch (Exception ex) {
                lastCheckInfo = "Loi poll: " + ex.getMessage();
                System.err.println("-> [TELEGRAM INBOX] " + lastCheckInfo);
                sleepQuiet(pollSeconds * 1000L);
            }
        }
    }

    /** Rut gon noi dung de hien trong tin xac nhan (toi da 120 ky tu). */
    static String preview(String s) {
        if (s == null) return "";
        String oneLine = s.replace('\n', ' ').replace('\r', ' ').trim();
        return oneLine.length() <= 120 ? oneLine : oneLine.substring(0, 120) + "...";
    }

    private void fireChanged() {
        final int unread = dao.countUnread();
        for (OnTicketsChangedListener l : listeners) {
            SwingUtilities.invokeLater(() -> l.onTicketsChanged(unread));
        }
    }

    /** Gui phan hoi tu app toi nguoi dung qua bot ticket (chay nen). */
    public void replyAsync(String chatId, String text) {
        new Thread(() -> {
            try {
                reply(chatId, text);
                System.out.println("-> [TELEGRAM INBOX] Da gui phan hoi toi chat " + chatId);
            } catch (Exception ex) {
                System.err.println("-> [TELEGRAM INBOX] Loi gui phan hoi: " + ex.getMessage());
            }
        }, "TelegramInboxReply").start();
    }

    public void reply(String chatId, String text) throws Exception {
        sendWithMarkup(chatId, text, null);
    }

    /** Gui tin nhan kem ban phim tuy chon (dùng để hiện nút xin SĐT). */
    public void replyWithKeyboard(String chatId, String text, String markupJson) throws Exception {
        sendWithMarkup(chatId, text, markupJson);
    }

    private void sendWithMarkup(String chatId, String text, String markupJson) throws Exception {
        StringBuilder params = new StringBuilder("chat_id=")
                .append(URLEncoder.encode(chatId, StandardCharsets.UTF_8))
                .append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8));
        if (markupJson != null && !markupJson.isEmpty()) {
            params.append("&reply_markup=").append(URLEncoder.encode(markupJson, StandardCharsets.UTF_8));
        }
        byte[] body = params.toString().getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = (HttpURLConnection) new URL(
                "https://api.telegram.org/bot" + ticketToken + "/sendMessage").openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }
        readAll(conn.getInputStream());
    }

    private static String httpGet(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(45000);
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

    private static void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }

    // ---------- Parse JSON toi thieu (khong them thu vien) ----------

    private static final Pattern P_UPDATE_ID = Pattern.compile("\"update_id\"\\s*:\\s*(\\d+)");
    private static final Pattern P_TEXT = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_CHAT_ID = Pattern.compile("\"chat\"\\s*:\\s*\\{[^\\}]*?\"id\"\\s*:\\s*(-?\\d+)");
    private static final Pattern P_FIRST = Pattern.compile("\"from\"\\s*:\\s*\\{[^\\}]*?\"first_name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_LAST = Pattern.compile("\"from\"\\s*:\\s*\\{[^\\}]*?\"last_name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_USER = Pattern.compile("\"from\"\\s*:\\s*\\{[^\\}]*?\"username\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_FROM_ID = Pattern.compile("\"from\"\\s*:\\s*\\{[^\\}]*?\"id\"\\s*:\\s*(\\d+)");
    // Khối contact khi người dùng bấm nút chia sẻ SĐT: "contact":{"phone_number":"...","first_name":"...","user_id":...}
    private static final Pattern P_CONTACT = Pattern.compile("\"contact\"\\s*:\\s*\\{([^\\}]*)\\}");
    private static final Pattern P_PHONE = Pattern.compile("\"phone_number\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern P_CUID = Pattern.compile("\"user_id\"\\s*:\\s*(\\d+)");
    private static final Pattern P_CNAME = Pattern.compile("\"first_name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    // Ban phim xin SDT (chi hien trong chat rieng voi bot)
    private static final String ASK_PHONE_MARKUP =
            "{\"keyboard\":[[{\"text\":\"Chia se SDT lien he\",\"request_contact\":true}]],"
            + "\"resize_keyboard\":true,\"one_time_keyboard\":true}";

    // SDT Viet Nam go trong tin nhan: 0912345678, +84912345678, 84912345678
    private static final Pattern P_PHONE_IN_TEXT =
            Pattern.compile("(?<!\\d)(?:(\\+?84)|(0))(\\d{9})(?!\\d)");

    // So phong: "phong 101", "phòng B1", "P102", "P. 302A" (khong dau cung duoc)
    private static final Pattern P_ROOM =
            Pattern.compile("(?iu)(?:phòng|phong|p\\.?)(?:\\s*)([a-z]?\\d+[a-z]?)");

    /** Trich so phong trong tin nhan, chuan hoa VIET HOA. Tra ve "" neu khong co. */
    static String findRoom(String text) {
        if (text == null) return "";
        Matcher m = P_ROOM.matcher(text);
        if (m.find()) return m.group(1).toUpperCase();
        return "";
    }

    /** Tim SDT trong tin nhan, chuan hoa ve 0xxxxxxxxx. Tra ve "" neu khong co. */
    static String findPhoneInText(String text) {
        if (text == null) return "";
        Matcher m = P_PHONE_IN_TEXT.matcher(text);
        if (m.find()) return "0" + m.group(3);
        return "";
    }

    /** Nut mo chat rieng de gui SDT (danh cho nhom chung - nut contact khong chay trong nhom). */
    private String privateChatMarkup() {
        if (ticketUsername.isEmpty()) return null;
        return "{\"inline_keyboard\":[[{\"text\":\"Gui SDT qua chat rieng\","
                + "\"url\":\"https://t.me/" + ticketUsername + "?start=phone\"}]]}";
    }

    private static boolean isGroupChat(String chatId) {
        return chatId != null && chatId.startsWith("-");
    }

    /** Cat mang result thanh tung object update, trich tin text. Bo qua tin khong co text. */
    static List<TelegramTicket> parseUpdates(String json) {
        List<TelegramTicket> out = new ArrayList<>();
        if (json == null || !json.contains("\"ok\":true")) return out;
        int ri = json.indexOf("\"result\"");
        if (ri < 0) return out;
        int arr = json.indexOf('[', ri);
        if (arr < 0) return out;
        // Cat object theo cap ngoac { } (bo qua trong chuoi)
        List<String> objs = splitTopObjects(json.substring(arr));
        for (String o : objs) {
            try {
                Matcher mId = P_UPDATE_ID.matcher(o);
                if (!mId.find()) continue;
                long updateId = Long.parseLong(mId.group(1));
                String chatId = firstGroup(P_CHAT_ID, o, "0");
                String name = unescape(firstGroup(P_FIRST, o, ""));
                String last = unescape(firstGroup(P_LAST, o, ""));
                String uname = unescape(firstGroup(P_USER, o, ""));
                if (!last.isEmpty()) name = (name + " " + last).trim();
                if (name.isEmpty()) name = uname.isEmpty() ? ("user-" + chatId) : ("@" + uname);
                String fromId = firstGroup(P_FROM_ID, o, "");
                // 1. Tin chia se SDT (nut request_contact) -> gan SDT vao ticket gan nhat
                Matcher mContact = P_CONTACT.matcher(o);
                if (mContact.find()) {
                    String inner = mContact.group(1);
                    String phone = unescape(firstGroup(P_PHONE, inner, "")).trim();
                    if (!phone.isEmpty()) {
                        TelegramTicket ct = new TelegramTicket(updateId, chatId, name,
                                "(Da chia se SDT lien he)");
                        ct.setPhone(phone);
                        ct.setUsername(uname.isEmpty() ? "" : ("@" + uname));
                        String cuid = firstGroup(P_CUID, inner, "");
                        ct.setUserId(cuid.isEmpty() ? fromId : cuid);
                        String cname = unescape(firstGroup(P_CNAME, inner, "")).trim();
                        if (!cname.isEmpty() && name.startsWith("user-")) ct.setSenderName(cname);
                        out.add(ct);
                    }
                    continue;
                }
                // 2. Tin text thuong -> ticket moi (giu lai /start de gui nut SDT)
                Matcher mText = P_TEXT.matcher(o);
                if (!mText.find()) continue; // sticker/anh/... -> bo qua
                String text = unescape(mText.group(1)).trim();
                // Bo @mention dau tin (VD "@suconhe_bot cup nuoc...") cho gon
                text = text.replaceFirst("^@\\S+\\s+", "").trim();
                if (text.isEmpty()) continue;
                TelegramTicket ticket = new TelegramTicket(updateId, chatId, name, text);
                // Thong tin lien he: @username (chat truc tiep qua t.me) + user id (du phong)
                ticket.setUsername(uname.isEmpty() ? "" : ("@" + uname));
                ticket.setUserId(fromId);
                // So phong trong tin (dung de loc + hien thi)
                ticket.setRoom(findRoom(text));
                // Nguoi dung go SDT truc tiep trong tin (cach duy nhat trong nhom chung)
                String typed = findPhoneInText(text);
                if (!typed.isEmpty()) ticket.setPhone(typed);
                out.add(ticket);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private static String firstGroup(Pattern p, String s, String def) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : def;
    }

    /** Tach cac object {...} cap ngoac ngoai cung trong [ ... ]. */
    static List<String> splitTopObjects(String arrJson) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        boolean inStr = false;
        boolean esc = false;
        int start = -1;
        for (int i = 0; i < arrJson.length(); i++) {
            char c = arrJson.charAt(i);
            if (inStr) {
                if (esc) esc = false;
                else if (c == '\\') esc = true;
                else if (c == '"') inStr = false;
            } else {
                if (c == '"') inStr = true;
                else if (c == '{') {
                    if (depth == 0) start = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && start >= 0) {
                        out.add(arrJson.substring(start, i + 1));
                        start = -1;
                    }
                }
            }
        }
        return out;
    }

    /** Giai escape JSON co ban: quote, backslash, slash, b f n r t, uXXXX. */
    static String unescape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                switch (n) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case '/': sb.append('/'); i++; break;
                    case 'b': sb.append('\b'); i++; break;
                    case 'f': sb.append('\f'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case 'u':
                        if (i + 5 < s.length()) {
                            try {
                                sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else sb.append(c);
                        break;
                    default: sb.append(c); break;
                }
            } else sb.append(c);
        }
        return sb.toString();
    }
}
