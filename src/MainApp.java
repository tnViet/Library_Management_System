package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import util.SessionManager;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane mainLayout;
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management System");

        showLogin();
    }
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    // Hiển thị màn hình đăng nhập
    private void showLogin() {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/view/Login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 900);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Library Management System - Đăng nhập");
            primaryStage.sizeToScene();
            primaryStage.setResizable(false);
            primaryStage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Hiển thị Dashboard sau khi đăng nhập thành công
    public void showMainApp(User user) {
        this.currentUser = user;

        primaryStage.setTitle("Library Management System - " + user.getRole().toUpperCase());
        primaryStage.setResizable(true);

        initRootLayout();
        showDashboard();
    }

    // Khởi tạo layout chính với Menu
    private void initRootLayout() {
        mainLayout = new BorderPane();

        // Tạo MenuBar
        MenuBar menuBar = new MenuBar();

        // Menu Hệ thống
        Menu menuSystem = new Menu("Hệ thống");
        MenuItem menuDashboard = new MenuItem("🏠 Trang chủ");
        MenuItem menuLogout = new MenuItem("🚪 Đăng xuất");
        MenuItem menuExit = new MenuItem("❌ Thoát");

        menuDashboard.setOnAction(e -> showDashboard());
        menuLogout.setOnAction(e -> handleLogout());
        menuExit.setOnAction(e -> primaryStage.close());

        menuSystem.getItems().addAll(menuDashboard, new SeparatorMenuItem(), menuLogout, menuExit);

        // Menu Quản lý - động theo role
        Menu menuManage = new Menu("Quản lý");
        setupMenuByRole(menuManage);

        // Thêm menu vào MenuBar
        menuBar.getMenus().addAll(menuSystem, menuManage);

        // Set MenuBar vào layout
        mainLayout.setTop(menuBar);

        // Tạo Scene
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Setup menu theo role
    private void setupMenuByRole(Menu menuManage) {
        if (SessionManager.getInstance().isAdmin()) {
            // Admin có tất cả quyền
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
            // Librarian không có quyền quản lý users
            MenuItem menuBooks = new MenuItem("📚 Quản lý Sách");
            MenuItem menuMembers = new MenuItem("👤 Quản lý Thành viên");
            MenuItem menuLoans = new MenuItem("📖 Quản lý Mượn sách");
            MenuItem menuFines = new MenuItem("💰 Quản lý Phạt");

            menuBooks.setOnAction(e -> showBooks());
            menuMembers.setOnAction(e -> showMembers());
            menuLoans.setOnAction(e -> showLoans());

            menuManage.getItems().addAll(menuBooks, menuMembers, menuLoans, menuFines);
        } else {
            // Member chỉ xem
            MenuItem menuViewBooks = new MenuItem("📚 Xem danh sách sách");
            MenuItem menuMyLoans = new MenuItem("📖 Sách đang mượn");

            menuViewBooks.setOnAction(e -> showMemberBooks());
            menuMyLoans.setOnAction(e -> showMemberLoans());

            menuManage.getItems().addAll(menuViewBooks, menuMyLoans);
        }
    }

    // Hiển thị Dashboard
    private void showDashboard() {
        VBox dashboard = new VBox(25);
        dashboard.setStyle("""
        -fx-padding: 60;
        -fx-alignment: center;
        -fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);
        """);

        Label title = new Label("📚 LIBRARY MANAGEMENT SYSTEM");
        title.setStyle("""
        -fx-font-size: 32;
        -fx-font-weight: bold;
        -fx-text-fill: #2b2b2b;
        """);

        Label userInfo = new Label("👤 Đăng nhập: " + currentUser.getUsername() +
                " | Quyền: " + currentUser.getRole().toUpperCase());
        userInfo.setStyle("""
        -fx-font-size: 16;
        -fx-text-fill: #555;
        -fx-padding: 0 0 20 0;
        """);

        String baseButton = """
        -fx-font-size: 16;
        -fx-min-width: 220;
        -fx-min-height: 50;
        -fx-background-color: #4a90e2;
        -fx-text-fill: white;
        -fx-background-radius: 12;
        -fx-cursor: hand;
        """;

        VBox buttons = new VBox(15);
        buttons.setStyle("-fx-alignment: center;");

        // Tạo buttons theo role
        if (SessionManager.getInstance().isAdmin()) {
            Button btnUsers = new Button("👥 Quản lý Users");
            Button btnBooks = new Button("📚 Quản lý Sách");
            Button btnMembers = new Button("👤 Quản lý Thành viên");
            Button btnLoans = new Button("📖 Quản lý Mượn sách");
            Button btnFines = new Button("💰 Quản lý Phạt");

            for (Button b : new Button[]{btnUsers, btnBooks, btnMembers, btnLoans, btnFines}) {
                b.setStyle(baseButton);
                b.setOnMouseEntered(e -> b.setStyle(baseButton + "-fx-background-color: #357ABD;"));
                b.setOnMouseExited(e -> b.setStyle(baseButton));
            }

            btnUsers.setOnAction(e -> showUsers());
            btnBooks.setOnAction(e -> showBooks());
            btnMembers.setOnAction(e -> showMembers());
            btnLoans.setOnAction(e -> showLoans());
            buttons.getChildren().addAll(btnUsers, btnBooks, btnMembers, btnLoans);

        } else if (SessionManager.getInstance().isLibrarian()) {
            Button btnBooks = new Button("📚 Quản lý Sách");
            Button btnMembers = new Button("👤 Quản lý Thành viên");
            Button btnLoans = new Button("📖 Quản lý Mượn sách");

            for (Button b : new Button[]{btnBooks, btnMembers, btnLoans}) {
                b.setStyle(baseButton);
                b.setOnMouseEntered(e -> b.setStyle(baseButton + "-fx-background-color: #357ABD;"));
                b.setOnMouseExited(e -> b.setStyle(baseButton));
            }

            btnBooks.setOnAction(e -> showBooks());
            btnMembers.setOnAction(e -> showMembers());
            btnLoans.setOnAction(e -> showLoans());

            buttons.getChildren().addAll(btnBooks, btnMembers, btnLoans);

        } else {
            Button btnViewBooks = new Button("📚 Xem danh sách sách");
            Button btnMyLoans = new Button("📖 Sách đang mượn");

            for (Button b : new Button[]{btnViewBooks, btnMyLoans}) {
                b.setStyle(baseButton + "-fx-background-color: #4CAF50;");
                b.setOnMouseEntered(e -> b.setStyle(baseButton + "-fx-background-color: #45a049;"));
                b.setOnMouseExited(e -> b.setStyle(baseButton + "-fx-background-color: #4CAF50;"));
            }

            btnViewBooks.setOnAction(e -> showMemberBooks());
            btnMyLoans.setOnAction(e -> showMemberLoans());

            buttons.getChildren().addAll(btnViewBooks, btnMyLoans);
        }

        // Footer
        Label footer = new Label("© REFUND | " + currentUser.getRole().toUpperCase() + " Mode");
        footer.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-padding: 30 0 0 0;");

        dashboard.getChildren().addAll(title, userInfo, buttons, footer);
        mainLayout.setCenter(dashboard);
    }

    // Xử lý đăng xuất
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Đăng xuất");
        confirmAlert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            showLogin();
        }
    }

    // Admin: Quản lý Users
    private void showUsers() {
        if (SessionManager.getInstance().isAdmin()) {
            loadView("/view/User.fxml");
        } else {
            showAlert("Không có quyền", "Bạn không có quyền truy cập chức năng này!");
        }
    }

    // Quản lý Sách
    private void showBooks() {
        loadView("/view/Book.fxml");
    }

    // Quản lý Thành viên
    private void showMembers() {
        loadView("/view/Member.fxml");
    }

    // Quản lý Mượn sách
    private void showLoans() {
        loadView("/view/Loan.fxml");
    }

    // Member: Xem sách (chỉ sách còn)
    private void showMemberBooks() {
        loadView("/view/MemberBookView.fxml");
    }

    // Member: Xem sách đang mượn
    private void showMemberLoans() {
        loadView("/view/MemberLoanView.fxml");
    }

    // Helper method để load FXML
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainLayout.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    // Hiển thị Alert
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}