package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // Thông tin kết nối MySQL (XAMPP mặc định)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // XAMPP mặc định không có password

    // Driver JDBC cho MySQL
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Singleton connection (tùy chọn)
    private static Connection connection = null;

    // Load MySQL Driver
    static {
        try {
            Class.forName(DB_DRIVER);
            System.out.println("MySQL JDBC Driver đã được load thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        }
    }

    /**
     * Tạo kết nối mới đến database
     * @return Connection object
     * @throws SQLException nếu không kết nối được
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Kết nối database thành công!");
            return conn;
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Lấy Singleton connection (chỉ tạo 1 lần)
     * @return Connection object
     * @throws SQLException
     */
    public static Connection getSingletonConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = getConnection();
        }
        return connection;
    }

    /**
     * Đóng kết nối
     * @param conn Connection cần đóng
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối database!");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    /**
     * Test kết nối database
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Test kết nối thành công!");
                System.out.println("📊 Database: " + conn.getCatalog());
                System.out.println("🔗 URL: " + conn.getMetaData().getURL());
            }
        } catch (SQLException e) {
            System.err.println("❌ Test kết nối thất bại!");
            e.printStackTrace();
        }
    }

    // Main method để test
    public static void main(String[] args) {
        testConnection();
    }
}