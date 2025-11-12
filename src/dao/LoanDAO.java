package dao;

import model.Loan;
import util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    // Lấy tất cả phiếu mượn
    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT l.*, m.name AS member_name " +
                "FROM loans l LEFT JOIN members m ON l.member_id = m.id " +
                "ORDER BY loan_date DESC";


        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {


            while (rs.next()) {
                Loan loan = extractLoanWithMemberName(rs);
                loans.add(loan);
            }
        }
        return loans;
    }



    // Lấy phiếu mượn theo Member ID
    public List<Loan> getLoansByMemberId(int memberId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE member_id = ? ORDER BY loan_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Loan loan = extractLoanFromResultSet(rs);
                    loans.add(loan);
                }
            }
        }
        return loans;
    }




    // Lấy phiếu mượn chưa trả của member
    public List<Loan> getUnreturnedLoansByMemberId(int memberId) throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT * FROM loans WHERE member_id = ? AND returned = FALSE ORDER BY due_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Loan loan = extractLoanFromResultSet(rs);
                    loans.add(loan);
                }
            }
        }
        return loans;
    }

    // Thêm phiếu mượn mới
    public boolean addLoan(Loan loan) throws SQLException {
        String query = "INSERT INTO loans (member_id, book_id, loan_date, due_date, returned) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, loan.getMemberId());
            pstmt.setInt(2, loan.getBookId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(loan.getDueDate()));
            pstmt.setBoolean(5, loan.isReturned());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        loan.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Cập nhật phiếu mượn
    public boolean updateLoan(Loan loan) throws SQLException {
        String query = "UPDATE loans SET member_id = ?, book_id = ?, loan_date = ?, due_date = ?, return_date = ?, returned = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, loan.getMemberId());
            pstmt.setInt(2, loan.getBookId());
            pstmt.setDate(3, Date.valueOf(loan.getLoanDate()));
            pstmt.setDate(4, Date.valueOf(loan.getDueDate()));

            if (loan.getReturnDate() != null) {
                pstmt.setDate(5, Date.valueOf(loan.getReturnDate()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }

            pstmt.setBoolean(6, loan.isReturned());
            pstmt.setInt(7, loan.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    // Trả sách (cập nhật return_date và returned = true)
    public boolean returnBook(int loanId) throws SQLException {
        String query = "UPDATE loans SET return_date = ?, returned = TRUE WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, loanId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }


    // Xóa phiếu mượn
    public boolean deleteLoan(int id) throws SQLException {
        String query = "DELETE FROM loans WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }





    // Helper method: Trích xuất Loan từ ResultSet
    private Loan extractLoanFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int memberId = rs.getInt("member_id");
        int bookId = rs.getInt("book_id");
        LocalDate loanDate = rs.getDate("loan_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();

        Date returnDateSql = rs.getDate("return_date");
        LocalDate returnDate = (returnDateSql != null) ? returnDateSql.toLocalDate() : null;

        boolean returned = rs.getBoolean("returned");

        return new Loan(id, memberId, bookId, loanDate, dueDate, returnDate, returned);
    }
    private Loan extractLoanWithMemberName(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int memberId = rs.getInt("member_id");
        int bookId = rs.getInt("book_id");
        // them cai nay
        String memberName = rs.getString("member_name");
        LocalDate loanDate = rs.getDate("loan_date").toLocalDate();
        LocalDate dueDate = rs.getDate("due_date").toLocalDate();


        Date returnDateSql = rs.getDate("return_date");
        LocalDate returnDate = (returnDateSql != null) ? returnDateSql.toLocalDate() : null;


        boolean returned = rs.getBoolean("returned");


        Loan loan = new Loan(id, memberId, memberName, bookId, loanDate, dueDate, returnDate, returned);


        // Không lưu memberName vào Loan, chỉ dùng để bind TableView
        return loan;
    }

}