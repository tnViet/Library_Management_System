
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // Thông tin kết nối MySQL (XAMPP mặc định)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

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


}
