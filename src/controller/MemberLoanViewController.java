package controller;

import dao.LoanDAO;
import dao.BookDAO;
import dao.MemberDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Loan;
import model.Book;
import model.Member;
import util.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class MemberLoanViewController implements Initializable {

    @FXML private TableView<LoanDisplay> loanTable;
    @FXML private TableColumn<LoanDisplay, Integer> colId;
    @FXML private TableColumn<LoanDisplay, String> colBookTitle;
    @FXML private TableColumn<LoanDisplay, LocalDate> colLoanDate;
    @FXML private TableColumn<LoanDisplay, LocalDate> colDueDate;
    @FXML private TableColumn<LoanDisplay, String> colStatus;
    @FXML private TableColumn<LoanDisplay, String> colDaysLeft;

    @FXML private Label lblWelcome;
    @FXML private Label lblTotalLoans;
    @FXML private Label lblOverdueLoans;

    private LoanDAO loanDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private ObservableList<LoanDisplay> loanList;
    private int currentMemberId;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loanDAO = new LoanDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        loanList = FXCollections.observableArrayList();

        // Lấy member ID từ username (giả sử username = email trong members)
        try {
            String username = SessionManager.getInstance().getCurrentUsername();
            // Tạm thời dùng member ID = 1, bạn cần mapping username -> member
            currentMemberId = 1; // TODO: Lấy member ID từ username

            lblWelcome.setText("Sách đang mượn của: " + username);
        } catch (Exception e) {
            currentMemberId = 1;
        }

        // Thiết lập các cột
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookTitle.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDaysLeft.setCellValueFactory(new PropertyValueFactory<>("daysLeft"));

        // Custom cell factory cho Status
        colStatus.setCellFactory(column -> new TableCell<LoanDisplay, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Quá hạn")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item.contains("Sắp hết hạn")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // Custom cell factory cho Days Left
        colDaysLeft.setCellFactory(column -> new TableCell<LoanDisplay, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Quá")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #333;");
                    }
                }
            }
        });

        loadMyLoans();
    }

    private void loadMyLoans() {
        try {
            loanList.clear();
            var loans = loanDAO.getUnreturnedLoansByMemberId(currentMemberId);

            int overdueCount = 0;
            for (Loan loan : loans) {
                Book book = bookDAO.getBookById(loan.getBookId());
                String bookTitle = (book != null) ? book.getTitle() : "Unknown";

                long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
                String status;
                String daysLeft;

                if (daysUntilDue < 0) {
                    status = "Quá hạn";
                    daysLeft = "Quá " + Math.abs(daysUntilDue) + " ngày";
                    overdueCount++;
                } else if (daysUntilDue <= 3) {
                    status = "Sắp hết hạn";
                    daysLeft = daysUntilDue + " ngày";
                } else {
                    status = "Bình thường";
                    daysLeft = daysUntilDue + " ngày";
                }

                loanList.add(new LoanDisplay(
                        loan.getId(),
                        bookTitle,
                        loan.getLoanDate(),
                        loan.getDueDate(),
                        status,
                        daysLeft
                ));
            }

            loanTable.setItems(loanList);
            lblTotalLoans.setText("Tổng: " + loans.size() + " cuốn");
            lblOverdueLoans.setText("Quá hạn: " + overdueCount + " cuốn");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách mượn sách: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class để hiển thị trong TableView
    public static class LoanDisplay {
        private int id;
        private String bookTitle;
        private LocalDate loanDate;
        private LocalDate dueDate;
        private String status;
        private String daysLeft;

        public LoanDisplay(int id, String bookTitle, LocalDate loanDate, LocalDate dueDate,
                           String status, String daysLeft) {
            this.id = id;
            this.bookTitle = bookTitle;
            this.loanDate = loanDate;
            this.dueDate = dueDate;
            this.status = status;
            this.daysLeft = daysLeft;
        }

        public int getId() { return id; }
        public String getBookTitle() { return bookTitle; }
        public LocalDate getLoanDate() { return loanDate; }
        public LocalDate getDueDate() { return dueDate; }
        public String getStatus() { return status; }
        public String getDaysLeft() { return daysLeft; }
    }
}