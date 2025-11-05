package controller;

import dao.BookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Book;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class BookController implements Initializable {

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colPublisher;
    @FXML private TableColumn<Book, Integer> colYear;
    @FXML private TableColumn<Book, Integer> colTotalCopies;
    @FXML private TableColumn<Book, Integer> colAvailableCopies;

    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextField txtPublisher;
    @FXML private TextField txtYear;
    @FXML private TextField txtTotalCopies;
    @FXML private TextField txtAvailableCopies;
    @FXML private TextField txtSearch;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    private BookDAO bookDAO;
    private ObservableList<Book> bookList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookDAO = new BookDAO();
        bookList = FXCollections.observableArrayList();

        // Thiết lập các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPublisher.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colTotalCopies.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        colAvailableCopies.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));

        // Custom cell factory cho cột Available Copies (hiển thị màu)
        colAvailableCopies.setCellFactory(column -> new TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item == 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item < 3) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // Load dữ liệu ban đầu
        loadBooks();

        // Lắng nghe sự kiện chọn hàng trong bảng
        bookTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showBookDetails(newSelection);
                    }
                }
        );
    }

    private void loadBooks() {
        try {
            bookList.clear();
            bookList.addAll(bookDAO.getAllBooks());
            bookTable.setItems(bookList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách sách: " + e.getMessage());
        }
    }

    private void showBookDetails(Book book) {
        txtTitle.setText(book.getTitle());
        txtAuthor.setText(book.getAuthor());
        txtPublisher.setText(book.getPublisher());
        txtYear.setText(String.valueOf(book.getYear()));
        txtTotalCopies.setText(String.valueOf(book.getTotalCopies()));
        txtAvailableCopies.setText(String.valueOf(book.getAvailableCopies()));
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                Book book = new Book(
                        0, // ID sẽ tự động tăng trong database
                        txtTitle.getText().trim(),
                        txtAuthor.getText().trim(),
                        txtPublisher.getText().trim(),
                        Integer.parseInt(txtYear.getText().trim()),
                        Integer.parseInt(txtTotalCopies.getText().trim()),
                        Integer.parseInt(txtAvailableCopies.getText().trim())
                );

                bookDAO.addBook(book);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm sách mới thành công!");
                loadBooks();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm sách: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sách để cập nhật!");
            return;
        }

        if (validateInput()) {
            try {
                selectedBook.setTitle(txtTitle.getText().trim());
                selectedBook.setAuthor(txtAuthor.getText().trim());
                selectedBook.setPublisher(txtPublisher.getText().trim());
                selectedBook.setYear(Integer.parseInt(txtYear.getText().trim()));
                selectedBook.setTotalCopies(Integer.parseInt(txtTotalCopies.getText().trim()));
                selectedBook.setAvailableCopies(Integer.parseInt(txtAvailableCopies.getText().trim()));

                bookDAO.updateBook(selectedBook);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật sách thành công!");
                loadBooks();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật sách: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sách để xóa!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa sách");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa sách: " + selectedBook.getTitle() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                bookDAO.deleteBook(selectedBook.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa sách thành công!");
                loadBooks();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa sách: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        bookTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadBooks();
            return;
        }

        try {
            bookList.clear();
            bookList.addAll(bookDAO.searchBooks(searchText));
            bookTable.setItems(bookList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtTitle.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên sách!");
            return false;
        }
        if (txtAuthor.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tác giả!");
            return false;
        }
        if (txtPublisher.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập nhà xuất bản!");
            return false;
        }
        if (txtYear.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập năm xuất bản!");
            return false;
        }
        if (txtTotalCopies.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tổng số bản!");
            return false;
        }
        if (txtAvailableCopies.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số bản có sẵn!");
            return false;
        }

        try {
            int year = Integer.parseInt(txtYear.getText().trim());
            if (year < 1000 || year > 9999) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Năm xuất bản không hợp lệ!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Năm xuất bản phải là số!");
            return false;
        }

        try {
            int total = Integer.parseInt(txtTotalCopies.getText().trim());
            int available = Integer.parseInt(txtAvailableCopies.getText().trim());

            if (total <= 0 || available < 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số lượng sách không hợp lệ!");
                return false;
            }
            if (available > total) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số bản có sẵn không được lớn hơn tổng số bản!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số lượng sách phải là số nguyên!");
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtTitle.clear();
        txtAuthor.clear();
        txtPublisher.clear();
        txtYear.clear();
        txtTotalCopies.clear();
        txtAvailableCopies.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}