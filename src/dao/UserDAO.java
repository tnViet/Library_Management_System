package dao;

import model.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO {

    // Lấy tất cả users
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY username";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
        }
        return users;
    }


    // Lấy user theo username
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }


    // Xác thực đăng nhập
    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password)); // Hash password trước khi so sánh

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Thêm user mới
    public boolean addUser(User user) throws SQLException {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashPassword(user.getPassword())); // Hash password trước khi lưu
            pstmt.setString(3, user.getRole());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Cập nhật thông tin user
    public boolean updateUser(User user) throws SQLException {
        String query = "UPDATE users SET username = ?, role = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            pstmt.setInt(3, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    // Reset mật khẩu (cho admin)
    public boolean resetPassword(int userId, String newPassword) throws SQLException {
        String query = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, hashPassword(newPassword));
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Xóa user
    public boolean deleteUser(int id) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean isUsernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }




    // Hash password bằng SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Helper method: Trích xuất User từ ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");

        return new User(id, username, password, role);
    }
}