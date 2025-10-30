package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RootLayoutController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox menuHome;
    @FXML
    private HBox menuBook;
    @FXML
    private HBox menuUser;
    @FXML
    private HBox menuAuthor;
    // ... các menu khác

    private src.MainApp mainApp;

    public void setMainApp(src.MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        // Set menu Book là active mặc định
        setActiveMenu(menuBook);
    }

    @FXML
    private void handleHome() {
        setActiveMenu(menuHome);
        // mainApp.showHomeView();
    }

    @FXML
    private void handleBook() {
        setActiveMenu(menuBook);
        mainApp.showBookView();
    }

    @FXML
    private void handleUser() {
        setActiveMenu(menuUser);
        mainApp.showUserView();
    }

    @FXML
    private void handleAuthor() {
        setActiveMenu(menuAuthor);
        mainApp.showAuthorView();
    }

    private void setActiveMenu(HBox activeMenu) {
        // Remove active style from all menus
        menuHome.getStyleClass().remove("menu-item-active");
        menuBook.getStyleClass().remove("menu-item-active");
        menuUser.getStyleClass().remove("menu-item-active");
        menuAuthor.getStyleClass().remove("menu-item-active");

        // Add active style to selected menu
        activeMenu.getStyleClass().add("menu-item-active");
    }
}