package controller;
import src.MainApp;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    private MainApp mainApp;
    private ObservableList<User> userData = FXCollections.observableArrayList();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadUserData();
        userTable.setItems(userData);

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue)
        );
    }

    private void loadUserData() {
        // TODO: Load từ database
        userData.add(new User(1, "Nguyễn Văn A", "a@email.com", "0123456789"));
        userData.add(new User(2, "Trần Thị B", "b@email.com", "0987654321"));
    }

    private void showUserDetails(User user) {
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
        }
    }

    @FXML
    private void handleAdd() {
        User newUser = new User(
                userData.size() + 1,
                nameField.getText(),
                emailField.getText(),
                phoneField.getText()
        );

        userData.add(newUser);
        // TODO: Lưu vào database
    }

    @FXML
    private void handleEdit() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            selectedUser.setName(nameField.getText());
            selectedUser.setEmail(emailField.getText());
            selectedUser.setPhone(phoneField.getText());

            userTable.refresh();
            // TODO: Update database
        }
    }

    @FXML
    private void handleDelete() {
        int selectedIndex = userTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            userData.remove(selectedIndex);
            // TODO: Xóa database
        }
    }
}