package dao;

import model.Fine;
import util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    // Lấy tất cả các khoản phạt
    public List<Fine> getAllFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines ORDER BY fine_date DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Fine fine = extractFineFromResultSet(rs);
                fines.add(fine);
            }
        }
        return fines;
    }

    // Lấy phạt theo ID
    public Fine getFineById(int id) throws SQLException {
        String query = "SELECT * FROM fines WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractFineFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Lấy các khoản phạt theo Loan ID
    public List<Fine> getFinesByLoanId(int loanId) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE loan_id = ? ORDER BY fine_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loanId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Fine fine = extractFineFromResultSet(rs);
                    fines.add(fine);
                }
            }
        }
        return fines;
    }

    // Lấy các khoản phạt chưa thanh toán
    public List<Fine> getUnpaidFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE paid = FALSE ORDER BY fine_date DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Fine fine = extractFineFromResultSet(rs);
                fines.add(fine);
            }
        }
        return fines;
    }

    // Lấy các khoản phạt đã thanh toán
    public List<Fine> getPaidFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE paid = TRUE ORDER BY fine_date DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Fine fine = extractFineFromResultSet(rs);
                fines.add(fine);
            }
        }
        return fines;
    }

    // Thêm khoản phạt mới
    public boolean addFine(Fine fine) throws SQLException {
        String query = "INSERT INTO fines (loan_id, amount, paid, fine_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, fine.getLoanId());
            pstmt.setDouble(2, fine.getAmount());
            pstmt.setBoolean(3, fine.isPaid());
            pstmt.setDate(4, Date.valueOf(fine.getFineDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID tự động tăng
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fine.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Cập nhật khoản phạt
    public boolean updateFine(Fine fine) throws SQLException {
        String query = "UPDATE fines SET loan_id = ?, amount = ?, paid = ?, fine_date = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, fine.getLoanId());
            pstmt.setDouble(2, fine.getAmount());
            pstmt.setBoolean(3, fine.isPaid());
            pstmt.setDate(4, Date.valueOf(fine.getFineDate()));
            pstmt.setInt(5, fine.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Cập nhật trạng thái thanh toán
    public boolean updatePaymentStatus(int fineId, boolean paid) throws SQLException {
        String query = "UPDATE fines SET paid = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, paid);
            pstmt.setInt(2, fineId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Xóa khoản phạt
    public boolean deleteFine(int id) throws SQLException {
        String query = "DELETE FROM fines WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Xóa tất cả khoản phạt của một loan
    public boolean deleteFinesByLoanId(int loanId) throws SQLException {
        String query = "DELETE FROM fines WHERE loan_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loanId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Tính tổng số tiền phạt chưa thanh toán theo loan ID
    public double getTotalUnpaidFinesByLoanId(int loanId) throws SQLException {
        String query = "SELECT SUM(amount) as total FROM fines WHERE loan_id = ? AND paid = FALSE";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loanId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    // Tính tổng số tiền phạt (tất cả)
    public double getTotalFinesAmount() throws SQLException {
        String query = "SELECT SUM(amount) as total FROM fines";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    // Đếm số lượng phạt chưa thanh toán
    public int countUnpaidFines() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM fines WHERE paid = FALSE";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // Kiểm tra loan có phạt chưa thanh toán không
    public boolean hasUnpaidFines(int loanId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM fines WHERE loan_id = ? AND paid = FALSE";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loanId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    // Helper method: Trích xuất Fine từ ResultSet
    private Fine extractFineFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int loanId = rs.getInt("loan_id");
        double amount = rs.getDouble("amount");
        boolean paid = rs.getBoolean("paid");
        LocalDate fineDate = rs.getDate("fine_date").toLocalDate();

        return new Fine(id, loanId, amount, paid, fineDate);
    }
}