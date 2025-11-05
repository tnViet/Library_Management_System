package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private CheckBox chkRememberMe;
    @FXML private Label lblError;

    private UserDAO userDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        lblError.setVisible(false);

        // Enter key để login
        txtPassword.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Validation
        if (username.isEmpty()) {
            showError("Vui lòng nhập username!");
            txtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập password!");
            txtPassword.requestFocus();
            return;
        }

        try {
            // Xác thực
            User user = userDAO.authenticate(username, password);

            if (user != null) {
                // Lưu session
                SessionManager.getInstance().setCurrentUser(user);

                // Chuyển đến Dashboard
                loadDashboard(user);
            } else {
                showError("Username hoặc password không đúng!");
                txtPassword.clear();
                txtPassword.requestFocus();
            }
        } catch (SQLException e) {
            showError("Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboard(User user) {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            src.MainApp mainApp = new src.MainApp();
            mainApp.setPrimaryStage(stage);
            mainApp.showMainApp(user);

//            System.out.println("✅ Đăng nhập thành công: " + user.getUsername() + " (" + user.getRole() + ")");

        } catch (Exception e) {
            showError("Không thể load dashboard!");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);

        // Tự động ẩn sau 3 giây
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> lblError.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void openLink() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost/phpmyadmin/index.php?route=/sql&pos=0&db=library_db&table=users"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        System.exit(0);
    }
}