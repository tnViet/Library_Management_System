package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management System");

        initRootLayout();
        showDashboard();
    }

    // Khởi tạo layout chính với Menu
    private void initRootLayout() {
        mainLayout = new BorderPane();

        // Tạo MenuBar
        MenuBar menuBar = new MenuBar();

        // Menu Quản lý
        Menu menuManage = new Menu("Quản lý");
        MenuItem menuBooks = new MenuItem("Quản lý Sách");
        MenuItem menuMembers = new MenuItem("Quản lý Thành viên");
        MenuItem menuLoans = new MenuItem("Quản lý Mượn sách");
        MenuItem menuFines = new MenuItem("Quản lý Phạt");

        // Xử lý sự kiện click menu
        menuBooks.setOnAction(e -> showBooks());
        menuMembers.setOnAction(e -> showMembers());
        menuLoans.setOnAction(e -> showLoans());
        menuFines.setOnAction(e -> showFines());

        menuManage.getItems().addAll(menuBooks, menuMembers, menuLoans, menuFines);

        // Menu Hệ thống
        Menu menuSystem = new Menu("Hệ thống");
        MenuItem menuDashboard = new MenuItem("Trang chủ");
        MenuItem menuExit = new MenuItem("Thoát");

        menuDashboard.setOnAction(e -> showDashboard());
        menuExit.setOnAction(e -> primaryStage.close());

        menuSystem.getItems().addAll(menuDashboard, new SeparatorMenuItem(), menuExit);

        // Thêm menu vào MenuBar
        menuBar.getMenus().addAll(menuSystem, menuManage);

        // Set MenuBar vào layout
        mainLayout.setTop(menuBar);

        // Tạo Scene
        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
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

        String baseButton = """
        -fx-font-size: 16;
        -fx-min-width: 220;
        -fx-min-height: 50;
        -fx-background-color: #4a90e2;
        -fx-text-fill: white;
        -fx-background-radius: 12;
        -fx-cursor: hand;
    """;

        Button btnBooks = new Button("Quản lý Sách");
        Button btnMembers = new Button("Quản lý Thành viên");
        Button btnLoans = new Button("Quản lý Mượn sách");
        Button btnFines = new Button("Quản lý Phạt");

        for (Button b : new Button[]{btnBooks, btnMembers, btnLoans, btnFines}) {
            b.setStyle(baseButton);
            b.setOnMouseEntered(e -> b.setStyle(baseButton + "-fx-background-color: #357ABD;"));
            b.setOnMouseExited(e -> b.setStyle(baseButton));
        }

        // Xử lý sự kiện
        btnBooks.setOnAction(e -> showBooks());
        btnMembers.setOnAction(e -> showMembers());
        btnLoans.setOnAction(e -> showLoans());
        btnFines.setOnAction(e -> showFines());

        // Footer
        Label footer = new Label("© 2025 Library Management System");
        footer.setStyle("-fx-text-fill: #666; -fx-font-size: 12; -fx-padding: 30 0 0 0;");

        dashboard.getChildren().addAll(title, btnBooks, btnMembers, btnLoans, btnFines, footer);
        mainLayout.setCenter(dashboard);
    }


    // Hiển thị giao diện Quản lý Sách
    private void showBooks() {
        loadView("/view/Book.fxml");
    }

    // Hiển thị giao diện Quản lý Thành viên
    private void showMembers() {
        loadView("/view/Member.fxml");
    }

    // Hiển thị giao diện Quản lý Mượn sách
    private void showLoans() {
        loadView("/view/Loan.fxml");
    }

    // Hiển thị giao diện Quản lý Phạt
    private void showFines() {
        loadView("/view/Fine.fxml");
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

