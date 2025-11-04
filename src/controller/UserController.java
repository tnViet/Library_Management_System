package controller;

import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.User;
import util.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRole;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private TextField txtSearch;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnResetPassword;
    @FXML private Button btnClear;

    private UserDAO userDAO;
    private ObservableList<User> userList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Kiểm tra quyền admin
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Bạn không có quyền truy cập!");
            return;
        }

        userDAO = new UserDAO();
        userList = FXCollections.observableArrayList();

        // Thiết lập các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Custom cell factory cho cột Role
        colRole.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if ("admin".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if ("librarian".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        // Setup ComboBox Role
        cmbRole.setItems(FXCollections.observableArrayList("admin", "librarian", "member"));
        cmbRole.setValue("member");

        // Load dữ liệu
        loadUsers();

        // Lắng nghe sự kiện chọn hàng
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        showUserDetails(newSelection);
                    }
                }
        );
    }

    private void loadUsers() {
        try {
            userList.clear();
            userList.addAll(userDAO.getAllUsers());
            userTable.setItems(userList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách users: " + e.getMessage());
        }
    }

    private void showUserDetails(User user) {
        txtUsername.setText(user.getUsername());
        txtPassword.clear();
        cmbRole.setValue(user.getRole());
    }

    @FXML
    private void handleAdd() {
        if (validateInput()) {
            try {
                if (userDAO.isUsernameExists(txtUsername.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Username đã tồn tại!");
                    return;
                }

                User user = new User(
                        txtUsername.getText().trim(),
                        txtPassword.getText(),
                        cmbRole.getValue()
                );

                userDAO.addUser(user);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm user thành công!");
                loadUsers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdate() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn user để cập nhật!");
            return;
        }

        if (txtUsername.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập username!");
            return;
        }

        try {
            String newUsername = txtUsername.getText().trim();
            if (!newUsername.equals(selectedUser.getUsername()) &&
                    userDAO.isUsernameExists(newUsername)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Username đã tồn tại!");
                return;
            }

            selectedUser.setUsername(newUsername);
            selectedUser.setRole(cmbRole.getValue());

            userDAO.updateUser(selectedUser);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật user thành công!");
            loadUsers();
            clearFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật user: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn user để xóa!");
            return;
        }

        if (selectedUser.getId() == SessionManager.getInstance().getCurrentUserId()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể xóa tài khoản đang đăng nhập!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa user");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa user: " + selectedUser.getUsername() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userDAO.deleteUser(selectedUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa user thành công!");
                loadUsers();
                clearFields();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPassword() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn user để reset password!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password cho user: " + selectedUser.getUsername());
        dialog.setContentText("Nhập password mới:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            try {
                userDAO.resetPassword(selectedUser.getId(), result.get());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Reset password thành công!");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể reset password: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        userTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadUsers();
            return;
        }

        try {
            userList.clear();
            User user = userDAO.getUserByUsername(searchText);
            if (user != null) {
                userList.add(user);
            }
            userTable.setItems(userList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (txtUsername.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập username!");
            return false;
        }
        if (txtPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập password!");
            return false;
        }
        if (txtPassword.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Password phải có ít nhất 6 ký tự!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtUsername.clear();
        txtPassword.clear();
        cmbRole.setValue("member");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}