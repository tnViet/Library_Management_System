package dao;

import model.Book;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // Lấy tất cả sách
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY title";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        }
        return books;
    }

    // Lấy sách theo ID
    public Book getBookById(int id) throws SQLException {
        String query = "SELECT * FROM books WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Tìm kiếm sách (theo title, author, hoặc publisher)
    public List<Book> searchBooks(String keyword) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR publisher LIKE ? ORDER BY title";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    books.add(book);
                }
            }
        }
        return books;
    }

    // Lấy sách theo tác giả
    public List<Book> getBooksByAuthor(String author) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE author LIKE ? ORDER BY title";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + author + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    books.add(book);
                }
            }
        }
        return books;
    }

    // Lấy sách có sẵn (available copies > 0)
    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE available_copies > 0 ORDER BY title";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        }
        return books;
    }

    // Lấy sách hết hàng (available copies = 0)
    public List<Book> getOutOfStockBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE available_copies = 0 ORDER BY title";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        }
        return books;
    }

    // Thêm sách mới
    public boolean addBook(Book book) throws SQLException {
        String query = "INSERT INTO books (title, author, publisher, year, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getYear());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Lấy ID tự động tăng
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        book.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Cập nhật thông tin sách
    public boolean updateBook(Book book) throws SQLException {
        String query = "UPDATE books SET title = ?, author = ?, publisher = ?, year = ?, total_copies = ?, available_copies = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getYear());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setInt(7, book.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Cập nhật số lượng sách có sẵn (dùng khi mượn/trả sách)
    public boolean updateAvailableCopies(int bookId, int newAvailableCount) throws SQLException {
        String query = "UPDATE books SET available_copies = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newAvailableCount);
            pstmt.setInt(2, bookId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Giảm số lượng sách có sẵn (khi mượn sách)
    public boolean decreaseAvailableCopies(int bookId) throws SQLException {
        String query = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Tăng số lượng sách có sẵn (khi trả sách)
    public boolean increaseAvailableCopies(int bookId) throws SQLException {
        String query = "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Xóa sách
    public boolean deleteBook(int id) throws SQLException {
        String query = "DELETE FROM books WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Đếm tổng số sách
    public int getTotalBooksCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM books";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // Đếm tổng số bản sách (tổng tất cả copies)
    public int getTotalCopiesCount() throws SQLException {
        String query = "SELECT SUM(total_copies) as total FROM books";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // Đếm số sách có sẵn
    public int getAvailableCopiesCount() throws SQLException {
        String query = "SELECT SUM(available_copies) as available FROM books";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("available");
            }
        }
        return 0;
    }

    // Kiểm tra sách có sẵn để mượn không
    public boolean isBookAvailable(int bookId) throws SQLException {
        String query = "SELECT available_copies FROM books WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, bookId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_copies") > 0;
                }
            }
        }
        return false;
    }

    // Helper method: Trích xuất Book từ ResultSet
    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String publisher = rs.getString("publisher");
        int year = rs.getInt("year");
        int totalCopies = rs.getInt("total_copies");
        int availableCopies = rs.getInt("available_copies");

        return new Book(id, title, author, publisher, year, totalCopies, availableCopies);
    }
}