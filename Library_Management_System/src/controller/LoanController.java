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

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoanController implements Initializable {

    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, Integer> colId;
    @FXML private TableColumn<Loan, Integer> colMemberId;
    @FXML private TableColumn<Loan, Integer> colBookId;
    @FXML private TableColumn<Loan, LocalDate> colLoanDate;
    @FXML private TableColumn<Loan, LocalDate> colDueDate;
    @FXML private TableColumn<Loan, LocalDate> colReturnDate;
    @FXML private TableColumn<Loan, Boolean> colReturned;

    @FXML private ComboBox<Member> cmbMember;
    @FXML private ComboBox<Book> cmbBook;
    @FXML private DatePicker dpLoanDate;
    @FXML private DatePicker dpDueDate;
    @FXML private DatePicker dpReturnDate;
    @FXML private CheckBox chkReturned;
    @FXML private TextField txtSearch;
    @FXML private Label lblBookInfo;
    @FXML private Label lblMemberInfo;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnReturn;
    @FXML private Button btnClear;

    private LoanDAO loanDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private ObservableList<Loan> loanList;
    private ObservableList<Member> memberList;
    private ObservableList<Book> bookList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loanDAO = new LoanDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
        loanList = FXCollections.observableArrayList();
        memberList = FXCollections.observableArrayList();
        bookList = FXCollections.observableArrayList();

        // Thiết lập các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMemberId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        colBookId.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        colLoanDate.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        colReturned.setCellValueFactory(new PropertyValueFactory<>("returned"));

        // Custom cell factory cho cột Returned
        colReturned.setCellFactory(column -> new TableCell<Loan, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Đã trả" : "Chưa trả");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" :
                            "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });

        // Custom cell factory cho Due Date (hiển thị quá hạn)
        colDueDate.setCellFactory(column -> new TableCell<Loan, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    Loan loan = getTableView().getItems().get(getIndex());
                    if (!loan.isReturned() && item.isBefore(LocalDate.now())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Load dữ liệu
        loadLoans();
        loadMembers();
        loadAvailableBooks();

        // Thiết lập giá trị mặc định cho DatePicker
        dpLoanDate.setValue(LocalDate.now());
        dpDueDate.setValue(LocalDate.now().plusDays(14)); // Mặc định mượn 14 ngày

        // Lắng nghe sự kiện chọn hàng trong bảng
        loanTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showLoanDetails(newSelection);
                    }
                }
        );

        // Lắng nghe sự kiện thay đổi ngày mượn để tự động tính ngày trả
        dpLoanDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                dpDueDate.setValue(newDate.plusDays(14));
            }
        });

        // Lắng nghe sự kiện chọn sách
        cmbBook.valueProperty().addListener((obs, oldBook, newBook) -> {
            if (newBook != null) {
                lblBookInfo.setText("Tác giả: " + newBook.getAuthor() +
                        " | Còn lại: " + newBook.getAvailableCopies());
            }
        });

        // Lắng nghe sự kiện chọn thành viên
        cmbMember.valueProperty().addListener((obs, oldMember, newMember) -> {
            if (newMember != null) {
                lblMemberInfo.setText("Email: " + newMember.getEmail() +
                        " | SĐT: " + newMember.getPhone());
            }
        });
    }

    private void loadLoans() {
        try {
            loanList.clear();
            loanList.addAll(loanDAO.getAllLoans());
            loanTable.setItems(loanList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách mượn sách: " + e.getMessage());
        }
    }

    private void loadMembers() {
        try {
            memberList.clear();
            memberList.addAll(memberDAO.getAllMembers());
            cmbMember.setItems(memberList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách thành viên: " + e.getMessage());
        }
    }

    private void loadAvailableBooks() {
        try {
            bookList.clear();
            bookList.addAll(bookDAO.getAvailableBooks());
            cmbBook.setItems(bookList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách sách: " + e.getMessage());
        }
    }

    private void showLoanDetails(Loan loan) {
        try {
            // Tìm member và book tương ứng
            Member member = memberDAO.getMemberById(loan.getMemberId());
            Book book = bookDAO.getBookById(loan.getBookId());

            cmbMember.setValue(member);
            cmbBook.setValue(book);
            dpLoanDate.setValue(loan.getLoanDate());
            dpDueDate.setValue(loan.getDueDate());
            dpReturnDate.setValue(loan.getReturnDate());
            chkReturned.setSelected(loan.isReturned());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải chi tiết: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                Member selectedMember = cmbMember.getValue();
                Book selectedBook = cmbBook.getValue();

                // Kiểm tra sách còn không
                if (!bookDAO.isBookAvailable(selectedBook.getId())) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Sách này hiện không còn!");
                    return;
                }

                Loan loan = new Loan(
                        selectedMember.getId(),
                        selectedBook.getId(),
                        dpLoanDate.getValue(),
                        dpDueDate.getValue()
                );

                // Thêm loan và giảm số lượng sách
                if (loanDAO.addLoan(loan)) {
                    bookDAO.decreaseAvailableCopies(selectedBook.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm phiếu mượn thành công!");
                    loadLoans();
                    loadAvailableBooks();
                    clearFields();
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phiếu mượn: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiếu mượn để cập nhật!");
            return;
        }

        if (validateInput()) {
            try {
                selectedLoan.setMemberId(cmbMember.getValue().getId());
                selectedLoan.setBookId(cmbBook.getValue().getId());
                selectedLoan.setLoanDate(dpLoanDate.getValue());
                selectedLoan.setDueDate(dpDueDate.getValue());
                selectedLoan.setReturnDate(dpReturnDate.getValue());
                selectedLoan.setReturned(chkReturned.isSelected());

                loanDAO.updateLoan(selectedLoan);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật phiếu mượn thành công!");
                loadLoans();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phiếu mượn: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleReturn() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiếu mượn để trả!");
            return;
        }

        if (selectedLoan.isReturned()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Sách này đã được trả rồi!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Trả sách");

        // Kiểm tra quá hạn
        LocalDate today = LocalDate.now();
        long daysLate = ChronoUnit.DAYS.between(selectedLoan.getDueDate(), today);

        if (daysLate > 0) {
            confirmAlert.setContentText("Sách trả muộn " + daysLate + " ngày!\n" +
                    "Phí phạt dự kiến: " + (daysLate * 5000) + " VNĐ\n" +
                    "Bạn có muốn tiếp tục?");
        } else {
            confirmAlert.setContentText("Xác nhận trả sách?");
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                loanDAO.returnBook(selectedLoan.getId());
                bookDAO.increaseAvailableCopies(selectedLoan.getBookId());

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Trả sách thành công!");
                loadLoans();
                loadAvailableBooks();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể trả sách: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiếu mượn để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa phiếu mượn");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa phiếu mượn này?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Nếu chưa trả, tăng lại số sách
                if (!selectedLoan.isReturned()) {
                    bookDAO.increaseAvailableCopies(selectedLoan.getBookId());
                }

                loanDAO.deleteLoan(selectedLoan.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa phiếu mượn thành công!");
                loadLoans();
                loadAvailableBooks();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phiếu mượn: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        loanTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadLoans();
            return;
        }

        try {
            int memberId = Integer.parseInt(searchText);
            loanList.clear();
            loanList.addAll(loanDAO.getLoansByMemberId(memberId));
            loanTable.setItems(loanList);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số Member ID hợp lệ!");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (cmbMember.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thành viên!");
            return false;
        }
        if (cmbBook.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn sách!");
            return false;
        }
        if (dpLoanDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày mượn!");
            return false;
        }
        if (dpDueDate.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày hạn trả!");
            return false;
        }
        if (dpDueDate.getValue().isBefore(dpLoanDate.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày hạn trả phải sau ngày mượn!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        cmbMember.setValue(null);
        cmbBook.setValue(null);
        dpLoanDate.setValue(LocalDate.now());
        dpDueDate.setValue(LocalDate.now().plusDays(14));
        dpReturnDate.setValue(null);
        chkReturned.setSelected(false);
        lblBookInfo.setText("");
        lblMemberInfo.setText("");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}