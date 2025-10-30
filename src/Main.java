import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private VBox contentArea;
    private Label contentTitle;

    @Override
    public void start(Stage primaryStage) {
        // Main container
        BorderPane root = new BorderPane();

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Content area
        VBox content = createContentArea();
        root.setCenter(content);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Quản Lý Thư Viện");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #2c3e50;");

        // Header
        Label header = new Label("Thư Viện Sách");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");

        // Menu items
        VBox menuItems = new VBox();
        menuItems.getChildren().addAll(
                createMenuItem("🏠 Trang Chủ", false),
                createMenuItem("📚 Sách", true),
                createMenuItem("👤 Độc Giả", false),
                createMenuItem("✍️ Tác Giả", false),
                createMenuItem("🏢 Nhà Xuất Bản", false),
                createMenuItem("📋 Phiếu Mượn", false),
                createMenuItem("📥 Phiếu Nhập", false),
                createMenuItem("📊 Thống Kê", false),
                createMenuItem("⚙️ Chung", false),
                createMenuItem("🚪 Đăng Xuất", false)
        );

        sidebar.getChildren().addAll(header, menuItems);
        return sidebar;
    }

    private HBox createMenuItem(String text, boolean isActive) {
        HBox menuItem = new HBox();
        menuItem.setPadding(new Insets(12, 15, 12, 15));
        menuItem.setAlignment(Pos.CENTER_LEFT);
        menuItem.setSpacing(10);

        String baseStyle = "-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;";
        if (isActive) {
            menuItem.setStyle(baseStyle + "-fx-background-color: #3498db;");
        } else {
            menuItem.setStyle(baseStyle + "-fx-background-color: #2c3e50;");
        }

        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 13));

        menuItem.getChildren().add(label);

        // Hover effect
        menuItem.setOnMouseEntered(e -> {
            if (!isActive) {
                menuItem.setStyle(baseStyle + "-fx-background-color: #34495e;");
            }
        });

        menuItem.setOnMouseExited(e -> {
            if (!isActive) {
                menuItem.setStyle(baseStyle + "-fx-background-color: #2c3e50;");
            }
        });

        // Click event
        menuItem.setOnMouseClicked(e -> {
            handleMenuClick(text, menuItem);
        });

        return menuItem;
    }

    private VBox createContentArea() {
        VBox content = new VBox();
        content.setPadding(new Insets(20));
        content.setSpacing(20);
        content.setStyle("-fx-background-color: #ecf0f1;");

        contentTitle = new Label("Sách");
        contentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        contentTitle.setTextFill(Color.web("#2c3e50"));

        Label placeholder = new Label("Nội dung của trang sẽ hiển thị ở đây...");
        placeholder.setFont(Font.font("Arial", 14));

        content.getChildren().addAll(contentTitle, placeholder);
        contentArea = content;

        return content;
    }

    private void handleMenuClick(String menuText, HBox clickedItem) {
        // Remove active style from all menu items
        VBox sidebar = (VBox) clickedItem.getParent();
        for (var node : sidebar.getChildren()) {
            if (node instanceof HBox) {
                HBox item = (HBox) node;
                String baseStyle = "-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;";
                item.setStyle(baseStyle + "-fx-background-color: #2c3e50;");
            }
        }

        // Set active style for clicked item
        String baseStyle = "-fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;";
        clickedItem.setStyle(baseStyle + "-fx-background-color: #3498db;");

        // Update content title
        String title = menuText.substring(menuText.indexOf(' ') + 1); // Remove emoji
        contentTitle.setText(title);

        System.out.println("Menu clicked: " + title);
    }

    public static void main(String[] args) {
        launch(args);
    }
}