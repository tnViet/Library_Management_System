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

    // Lấy thành viên theo email
    public Member getMemberByEmail(String email) throws SQLException {
        String query = "SELECT * FROM members WHERE email = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

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

    // Lấy thành viên có phiếu mượn đang hoạt động
    public List<Member> getMembersWithActiveLoans() throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT DISTINCT m.* FROM members m " +
                "INNER JOIN loans l ON m.id = l.member_id " +
                "WHERE l.returned = FALSE " +
                "ORDER BY m.name";

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

    // Lấy thành viên có phiếu mượn quá hạn
    public List<Member> getMembersWithOverdueLoans() throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT DISTINCT m.* FROM members m " +
                "INNER JOIN loans l ON m.id = l.member_id " +
                "WHERE l.returned = FALSE AND l.due_date < CURDATE() " +
                "ORDER BY m.name";

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

    // Thêm thành viên mới
    public boolean addMember(Member member) throws SQLException {
        String query = "INSERT INTO members (name, email, phone, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPhone());
            pstmt.setString(4, member.getAddress());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID tự động tăng
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

    // Kiểm tra số điện thoại đã tồn tại chưa
    public boolean isPhoneExists(String phone) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM members WHERE phone = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, phone);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Đếm tổng số thành viên
    public int getTotalMembersCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM members";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // Đếm số thành viên đang mượn sách
    public int getActiveMembersCount() throws SQLException {
        String query = "SELECT COUNT(DISTINCT member_id) as count FROM loans WHERE returned = FALSE";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // Đếm số sách thành viên đang mượn
    public int getMemberActiveLoanCount(int memberId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM loans WHERE member_id = ? AND returned = FALSE";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    // Đếm tổng số sách thành viên đã mượn (bao gồm cả đã trả)
    public int getMemberTotalLoanCount(int memberId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM loans WHERE member_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    // Kiểm tra thành viên có phiếu mượn đang hoạt động không
    public boolean hasActiveLoans(int memberId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM loans WHERE member_id = ? AND returned = FALSE";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Kiểm tra thành viên có phiếu mượn quá hạn không
    public boolean hasOverdueLoans(int memberId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM loans WHERE member_id = ? AND returned = FALSE AND due_date < CURDATE()";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Lấy top thành viên mượn nhiều sách nhất
    public List<Member> getTopBorrowers(int limit) throws SQLException {
        List<Member> members = new ArrayList<>();
        String query = "SELECT m.*, COUNT(l.id) as loan_count " +
                "FROM members m " +
                "LEFT JOIN loans l ON m.id = l.member_id " +
                "GROUP BY m.id " +
                "ORDER BY loan_count DESC " +
                "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Member member = extractMemberFromResultSet(rs);
                    members.add(member);
                }
            }
        }
        return members;
    }

    // Helper method: Trích xuất Member từ ResultSet
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");

        return new Member(id, name, email, phone, address);
    }
}