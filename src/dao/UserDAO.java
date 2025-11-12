package dao;

import model.User;
import model.Member;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDAO {

    private MemberDAO memberDAO = new MemberDAO();

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
            pstmt.setString(2, hashPassword(password));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // **SỬA: Thêm user mới + Tự động tạo member nếu role = "member"**
    public boolean addUser(User user) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // Bước 1: Thêm user
            String userQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            int userId;

            try (PreparedStatement pstmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, hashPassword(user.getPassword()));
                pstmt.setString(3, user.getRole());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                        user.setId(userId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Bước 2: Nếu role = "member", tự động tạo member
            if ("member".equalsIgnoreCase(user.getRole())) {
                Member member = new Member();
                member.setUserId(userId);
                member.setName(user.getUsername()); // Tạm thời dùng username
                member.setEmail(user.getUsername() + "@library.com"); // Email mặc định
                member.setPhone("N/A");
                member.setAddress("N/A");

                if (!memberDAO.addMember(member, conn)) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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

    // Xóa user (CASCADE sẽ tự động xóa member)
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
//    public List<User> getAllUsersSortedByUsername() throws SQLException {
//        List<User> users = new ArrayList<>();
//        String query = "SELECT * FROM users ORDER BY username ASC";  // ← Thêm ORDER BY
//
//        try (Connection conn = DBUtil.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                User user = extractUserFromResultSet(rs);
//                users.add(user);
//            }
//        }
//        return users;
//    }
//    public List<User> getAllUsersSortedByRoleAndName() throws SQLException {
//        List<User> users = new ArrayList<>();
//        String query = "SELECT * FROM users ORDER BY " +
//                "CASE role " +
//                "  WHEN 'admin' THEN 1 " +
//                "  WHEN 'librarian' THEN 2 " +
//                "  WHEN 'member' THEN 3 " +
//                "END, " +
//                "username ASC";
//
//        try (Connection conn = DBUtil.getConnection();
//             Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//
//            while (rs.next()) {
//                User user = extractUserFromResultSet(rs);
//                users.add(user);
//            }
//        }
//        return users;
//    }
    // Helper method: Trích xuất User từ ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");

        return new User(id, username, password, role);
    }
}