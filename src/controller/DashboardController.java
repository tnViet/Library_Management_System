package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

public class DashboardController {

    @FXML private BorderPane mainLayout;
    @FXML private Label lblUsername;
    @FXML private Label lblRole;
    @FXML private MenuBar menuBar;
    @FXML private Menu menuManage;
    @FXML private Menu menuSystem;

    private User currentUser;

    @FXML
    public void initialize() {
        // Sẽ được khởi tạo bởi initData()
    }

    public void initData(User user) {
        this.currentUser = user;
        lblUsername.setText("Xin chào, " + user.getUsername());
        lblRole.setText("Quyền: " + user.getRole().toUpperCase());

        // Thiết lập menu theo quyền
        setupMenuByRole();

        // Hiển thị trang chủ
        showHomePage();
    }

    private void setupMenuByRole() {
        // Xóa tất cả menu items hiện tại
        menuManage.getItems().clear();

        if (SessionManager.getInstance().isAdmin()) {
            // Admin có quyền truy cập tất cả
            MenuItem menuUsers = new MenuItem("👥 Quản lý Users");
            MenuItem menuBooks = new MenuItem("📚 Quản lý Sách");
            MenuItem menuMembers = new MenuItem("👤 Quản lý Thành viên");
            MenuItem menuLoans = new MenuItem("📖 Quản lý Mượn sách");

            menuUsers.setOnAction(e -> showUsers());
            menuBooks.setOnAction(e -> showBooks());
            menuMembers.setOnAction(e -> showMembers());
            menuLoans.setOnAction(e -> showLoans());

            menuManage.getItems().addAll(menuUsers, new SeparatorMenuItem(),
                    menuBooks, menuMembers, menuLoans);
        } else if (SessionManager.getInstance().isLibrarian()) {
            MenuItem menuBooks = new MenuItem("📚 Quản lý Sách");
            MenuItem menuMembers = new MenuItem("👤 Quản lý Thành viên");
            MenuItem menuLoans = new MenuItem("📖 Quản lý Mượn sách");

            menuBooks.setOnAction(e -> showBooks());
            menuMembers.setOnAction(e -> showMembers());
            menuLoans.setOnAction(e -> showLoans());

            menuManage.getItems().addAll(menuBooks, menuMembers, menuLoans);
        } else {
            // Member chỉ xem sách có sẵn và sách đang mượn
            MenuItem menuViewBooks = new MenuItem("📚 Xem danh sách sách");
            MenuItem menuMyLoans = new MenuItem("📖 Sách đang mượn của tôi");

            menuViewBooks.setOnAction(e -> showMemberBooks());
            menuMyLoans.setOnAction(e -> showMyLoans());

            menuManage.getItems().addAll(menuViewBooks, menuMyLoans);
        }
    }


    private void showHomePage() {
        VBox homePage = new VBox(20);
        homePage.setStyle("-fx-padding: 50; -fx-alignment: center;");

        Label welcome = new Label("Chào mừng đến với Hệ thống Quản lý Thư viện");
        welcome.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label userInfo = new Label("Đăng nhập với tài khoản: " + currentUser.getUsername() +
                " (" + currentUser.getRole() + ")");
        userInfo.setStyle("-fx-font-size: 16;");

        homePage.getChildren().addAll(welcome, userInfo);
        mainLayout.setCenter(homePage);
    }

    @FXML
    private void showBooks() {
        loadView("/view/Book.fxml");
    }

    @FXML
    private void showMembers() {
        loadView("/view/Member.fxml");
    }

    @FXML
    private void showLoans() {
        loadView("/view/Loan.fxml");
    }

    @FXML
    private void showMemberBooks() {
        loadView("/view/MemberBookView.fxml");
    }


    @FXML
    private void showUsers() {
        if (SessionManager.getInstance().isAdmin()) {
            loadView("/view/User.fxml");
        } else {
            showAlert("Không có quyền", "Bạn không có quyền truy cập chức năng này!");
        }
    }

    @FXML
    private void showMyLoans() {
        // Load view với filter theo user hiện tại
        loadView("/view/Loan.fxml");
        // TODO: Filter loans by current user
    }


    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Đăng xuất");
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            loadLoginPage();
        }
    }

    private void loadLoginPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Library Management System - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            mainLayout.setCenter(view);
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}