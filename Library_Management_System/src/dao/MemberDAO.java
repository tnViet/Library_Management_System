package dao;

import model.Member;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    // Lấy tất cả thành viên
    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT * FROM members ORDER BY name";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Member member = extractMemberFromResultSet(rs);
                members.add(member);
            }
        }
        return members;
    }

    // Lấy thành viên theo ID
    public Member getMemberById(int id) throws SQLException {
        String query = "SELECT * FROM members WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // **MỚI: Lấy member theo user_id**
    public Member getMemberByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM members WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMemberFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Tìm kiếm thành viên (theo name, email, phone)
    public List<Member> searchMembers(String keyword) throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT * FROM members WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY name";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member member = extractMemberFromResultSet(rs);
                    members.add(member);
                }
            }
        }
        return members;
    }

    // Thêm thành viên mới (không có user_id)
    public boolean addMember(Member member) throws SQLException {
        String query = "INSERT INTO members (user_id, name, email, phone, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (member.getUserId() != null) {
                pstmt.setInt(1, member.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getPhone());
            pstmt.setString(5, member.getAddress());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // **MỚI: Thêm member với Connection (dùng trong Transaction)**
    public boolean addMember(Member member, Connection conn) throws SQLException {
        String query = "INSERT INTO members (user_id, name, email, phone, address) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (member.getUserId() != null) {
                pstmt.setInt(1, member.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getPhone());
            pstmt.setString(5, member.getAddress());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        member.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Cập nhật thông tin thành viên
    public boolean updateMember(Member member) throws SQLException {
        String query = "UPDATE members SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPhone());
            pstmt.setString(4, member.getAddress());
            pstmt.setInt(5, member.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Xóa thành viên
    public boolean deleteMember(int id) throws SQLException {
        String query = "DELETE FROM members WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM members WHERE email = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Helper method: Trích xuất Member từ ResultSet
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");

        // Xử lý user_id (có thể null)
        Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null;

        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");

        return new Member(id, userId, name, email, phone, address);
    }
}