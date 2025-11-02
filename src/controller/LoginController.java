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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();

            // Truyền user info đến Dashboard
            DashboardController controller = loader.getController();
            controller.initData(user);

            Scene scene = new Scene(root);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Library Management System - " + user.getRole().toUpperCase());
            stage.show();

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
    private void handleCancel() {
        System.exit(0);
    }
}