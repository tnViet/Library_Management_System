package controller;
import src.MainApp;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Book;



public class BookController {

    @FXML
    private TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, Integer> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;

    @FXML
    private TextField titleField;
    @FXML
    private TextField authorField;
    @FXML
    private TextField yearField;
    @FXML
    private TextField searchField;

    private MainApp mainApp;
    private ObservableList<Book> bookData = FXCollections.observableArrayList();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        // Khởi tạo các cột của bảng
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        // Load data từ database hoặc file
        loadBookData();

        // Set data cho table
        bookTable.setItems(bookData);

        // Listener khi chọn sách
        bookTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBookDetails(newValue)
        );
    }

    private void loadBookData() {
        // TODO: Load từ database
        // Ví dụ dữ liệu mẫu:
        bookData.add(new Book(1, "Lập Trình Java", "Nguyễn Văn A", 2021));
        bookData.add(new Book(2, "Cơ Sở Dữ Liệu", "Trần Thị B", 2020));
        bookData.add(new Book(3, "JavaFX Guide", "Lê Văn C", 2022));
    }

    private void showBookDetails(Book book) {
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            yearField.setText(String.valueOf(book.getYear()));
        } else {
            titleField.setText("");
            authorField.setText("");
            yearField.setText("");
        }
    }

    @FXML
    private void handleAdd() {
        Book newBook = new Book(
                bookData.size() + 1,
                titleField.getText(),
                authorField.getText(),
                Integer.parseInt(yearField.getText())
        );

        bookData.add(newBook);
        // TODO: Lưu vào database

        clearFields();
    }

    @FXML
    private void handleEdit() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            selectedBook.setTitle(titleField.getText());
            selectedBook.setAuthor(authorField.getText());
            selectedBook.setYear(Integer.parseInt(yearField.getText()));

            bookTable.refresh();
            // TODO: Update trong database
        }
    }

    @FXML
    private void handleDelete() {
        int selectedIndex = bookTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            bookData.remove(selectedIndex);
            // TODO: Xóa trong database
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        ObservableList<Book> filteredData = FXCollections.observableArrayList();

        for (Book book : bookData) {
            if (book.getTitle().toLowerCase().contains(keyword) ||
                    book.getAuthor().toLowerCase().contains(keyword)) {
                filteredData.add(book);
            }
        }

        bookTable.setItems(filteredData);
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
    }
}