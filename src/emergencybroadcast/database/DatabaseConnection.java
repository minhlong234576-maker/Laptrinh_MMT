package emergencybroadcast.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lớp quản lý kết nối Cơ sở dữ liệu MySQL trên XAMPP.
 * Database: emergency_broadcast
 * Host: localhost | Port: 3306 | User: root | Password: ""
 */
public class DatabaseConnection {

    // Cấu hình tham số kết nối - đọc từ db.properties, fallback về mặc định XAMPP
    private static String HOST = "localhost";
    private static String PORT = "3306";
    private static String DB_NAME = "emergency_broadcast";
    private static String USERNAME = "root";
    private static String PASSWORD = "";

    private static String URL;

    // Driver MySQL Connector/J 8.x
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

    static {
        loadConfig();
        URL = buildUrl();
        try {
            // Nạp Driver MySQL JDBC
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            System.err.println("LỖI: Không tìm thấy MySQL Driver! " + e.getMessage());
        }
    }

    private static void loadConfig() {
        Properties p = new Properties();
        // 1. Thử đọc file cạnh working dir (dễ sửa nhất cho sinh viên)
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (Exception ignored) {
            // 2. Thử đọc từ classpath (khi đóng JAR)
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) p.load(in);
            } catch (Exception ignored2) {
            }
        }
        HOST = p.getProperty("db.host", HOST);
        PORT = p.getProperty("db.port", PORT);
        DB_NAME = p.getProperty("db.name", DB_NAME);
        USERNAME = p.getProperty("db.user", USERNAME);
        PASSWORD = p.getProperty("db.password", PASSWORD);
    }

    private static String buildUrl() {
        return "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useUnicode=true&characterEncoding=UTF-8"
            + "&useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh"
            + "&connectTimeout=3000&socketTimeout=10000";
    }

    /** Chuỗi trạng thái ngắn để UI hiển thị (không lộ password). */
    public static String getStatusMessage() {
        return DB_NAME + " @ " + HOST + ":" + PORT + " (user=" + USERNAME + ")";
    }

    /**
     * Phương thức lấy kết nối (Connection) đến MySQL Database.
     * @return java.sql.Connection đối tượng kết nối
     * @throws SQLException nếu xảy ra lỗi kết nối
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Hàm kiểm tra nhanh trạng thái kết nối đến MySQL.
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("-> [THÀNH CÔNG] Kết nối Cơ sở dữ liệu MySQL thành công!");
                System.out.println("   Database: " + DB_NAME + " @ " + HOST + ":" + PORT);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("-> [THẤT BẠI] Không thể kết nối Cơ sở dữ liệu MySQL!");
            System.err.println("   Mã lỗi SQL: " + e.getErrorCode());
            System.err.println("   Chi tiết: " + e.getMessage());
        }
        return false;
    }

    /**
     * Phương thức main cho phép chạy trực tiếp để kiểm thử kết nối trong NetBeans.
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  KIỂM TRA KẾT NỐI DATABASE MYSQL (XAMPP)");
        System.out.println("==================================================");
        testConnection();
    }
}
