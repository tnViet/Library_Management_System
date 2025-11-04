package controller;

import dao.BookDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Book;
import util.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MemberBookViewController implements Initializable {

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Integer> colId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colPublisher;
    @FXML private TableColumn<Book, Integer> colYear;
    @FXML private TableColumn<Book, Integer> colAvailableCopies;

    @FXML private TextField txtSearch;
    @FXML private Label lblWelcome;

    private BookDAO bookDAO;
    private ObservableList<Book> bookList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookDAO = new BookDAO();
        bookList = FXCollections.observableArrayList();

        // Hiển thị thông tin user
        lblWelcome.setText("Xin chào, " + SessionManager.getInstance().getCurrentUsername() +
                " - Danh sách sách có sẵn");

        // Thiết lập các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colPublisher.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colAvailableCopies.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));

        // Custom cell factory cho cột Available Copies
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
                        setText("Hết sách");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item < 3) {
                        setText(item + " (Sắp hết)");
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setText(item + " (Còn nhiều)");
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // Load chỉ sách có sẵn
        loadAvailableBooks();
    }

    private void loadAvailableBooks() {
        try {
            bookList.clear();
            bookList.addAll(bookDAO.getAvailableBooks());
            bookTable.setItems(bookList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách sách: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadAvailableBooks();
            return;
        }

        try {
            bookList.clear();
            // Tìm kiếm và lọc chỉ sách có sẵn
            bookDAO.searchBooks(searchText).stream()
                    .filter(book -> book.getAvailableCopies() > 0)
                    .forEach(bookList::add);
            bookTable.setItems(bookList);

            if (bookList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy sách có sẵn!");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}