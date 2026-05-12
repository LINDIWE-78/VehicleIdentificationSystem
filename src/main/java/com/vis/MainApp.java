package com.vis;

import com.vis.controllers.*;
import com.vis.models.Vehicle;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import com.vis.db.DBConnection;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;

public class MainApp extends Application {

    private static Stage primaryStage;
    private Scene mainScene;
    private boolean isDark = true;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        LoginDialog.authenticateAndShowMain();
    }

    public static void showMainWindow() {
        if (primaryStage == null) {
            System.err.println("ERROR: primaryStage is null!");
            return;
        }

        try {
            MainApp app = new MainApp();
            Scene scene = app.buildMainScene();

            primaryStage.setScene(scene);
            primaryStage.setTitle("Vehicle Identification System");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Failed to start main window: " + e.getMessage());
        }
    }

    private Scene buildMainScene() {

        BorderPane root = new BorderPane();

        // ---------- Background image ----------
        try {
            URL imageUrl = getClass().getResource("/com/vis/vehicleidentificationsystem/mainscene.png.png");
            if (imageUrl != null) {
                root.setStyle(
                        "-fx-background-image: url('" + imageUrl.toExternalForm() + "');" +
                                "-fx-background-size: cover;" +
                                "-fx-background-position: center;"
                );
            } else {
                System.out.println("Background image not found – using default color.");
            }
        } catch (Exception e) {
            System.err.println("Background error: " + e.getMessage());
        }

        // ---------- Menu bar ----------
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem exportItem = new MenuItem("Export Workshop Data (CSV)");
        exportItem.setOnAction(e -> exportWorkshopData());
        MenuItem testConnectionItem = new MenuItem("Test Database Connection");
        testConnectionItem.setOnAction(e -> testConnection());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(exportItem, testConnectionItem, new SeparatorMenuItem(), exitItem);

        Menu accountMenu = new Menu("Account");
        MenuItem profileItem = new MenuItem("My Profile");
        profileItem.setOnAction(e -> showProfileDialog());
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> logout());
        accountMenu.getItems().addAll(profileItem, new SeparatorMenuItem(), logoutItem);

        Menu viewMenu = new Menu("View");
        MenuItem toggleThemeItem = new MenuItem("Toggle Dark/Light Theme");
        toggleThemeItem.setOnAction(e -> toggleTheme());
        viewMenu.getItems().add(toggleThemeItem);

        menuBar.getMenus().addAll(fileMenu, accountMenu, viewMenu);
        root.setTop(menuBar);

        // ---------- Tab pane ----------
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        WorkshopController workshopController = new WorkshopController();
        CustomerController customerController = new CustomerController();
        InsuranceController insuranceController = new InsuranceController();
        PoliceController policeController = new PoliceController();
        AdminController adminController = new AdminController();
        StatisticsController statisticsController = new StatisticsController();
        DashboardController dashboardController = new DashboardController();

        // Create Statistics tab with refresh listener
        Tab statsTab = new Tab("Statistics", statisticsController.getView());
        statsTab.setOnSelectionChanged(e -> {
            if (statsTab.isSelected()) {
                statisticsController.refreshData();
            }
        });

        String role = LoginDialog.getCurrentRole();

        if ("ADMIN".equals(role)) {
            tabPane.getTabs().addAll(
                    new Tab("Workshop", workshopController.getView()),
                    new Tab("Customer", customerController.getView()),
                    new Tab("Insurance", insuranceController.getView()),
                    new Tab("Police", policeController.getView()),
                    new Tab("Admin", adminController.getView()),
                    new Tab("Dashboard", dashboardController.getView()),
                    statsTab,
                    new Tab("Demo", createDemoPane())
            );
        } else if ("WORKSHOP".equals(role)) {
            tabPane.getTabs().addAll(
                    new Tab("Workshop", workshopController.getView()),
                    statsTab,
                    new Tab("Demo", createDemoPane())
            );
        } else if ("CUSTOMER".equals(role)) {
            tabPane.getTabs().addAll(
                    new Tab("Customer", customerController.getView()),
                    statsTab,
                    new Tab("Demo", createDemoPane())
            );
        } else if ("INSURANCE".equals(role)) {
            tabPane.getTabs().addAll(
                    new Tab("Insurance", insuranceController.getView()),
                    statsTab,
                    new Tab("Demo", createDemoPane())
            );
        } else if ("POLICE".equals(role)) {
            tabPane.getTabs().addAll(
                    new Tab("Police", policeController.getView()),
                    statsTab,
                    new Tab("Demo", createDemoPane())
            );
        } else {
            tabPane.getTabs().addAll(new Tab("Demo", createDemoPane()));
        }

        root.setCenter(tabPane);

        // Status bar
        Label statusLabel = new Label(
                "Connected to PostgreSQL | Logged in as: "
                        + LoginDialog.getCurrentUser()
                        + " (" + role + ")"
        );
        statusLabel.setStyle(
                "-fx-padding: 5;" +
                        "-fx-background-color: rgba(44,62,80,0.8);" +
                        "-fx-text-fill: white;"
        );
        root.setBottom(statusLabel);

        mainScene = new Scene(root, 1200, 700);

        // Load theme CSS
        String themeCss = isDark ? "/dark-theme.css" : "/light-theme.css";
        URL cssUrl = getClass().getResource(themeCss);
        if (cssUrl != null) {
            mainScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Theme CSS not found: " + themeCss);
        }

        return mainScene;
    }

    // ---------- Theme toggle ----------
    private void toggleTheme() {
        if (mainScene == null) return;
        mainScene.getStylesheets().clear();
        String themeCss = isDark ? "/light-theme.css" : "/dark-theme.css";
        URL cssUrl = getClass().getResource(themeCss);
        if (cssUrl != null) {
            mainScene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("Theme CSS not found: " + themeCss);
        }
        isDark = !isDark;
    }

    // ---------- Demo tab ----------
    private VBox createDemoPane() {
        VBox demoBox = new VBox(15);
        demoBox.setStyle("-fx-padding: 20; -fx-background-color: rgba(240,240,240,0.85);");

        Label title = new Label("JavaFX Features Demonstration");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ScrollPane with 20 dummy items
        VBox listContainer = new VBox(5);
        for (int i = 1; i <= 20; i++) {
            Label item = new Label("Vehicle Record Demo Item " + i);
            item.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: lightgray;");
            listContainer.getChildren().add(item);
        }
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setPrefHeight(220);
        scrollPane.setFitToWidth(true);

        // Pagination (5 pages, each with 4 different items)
        Pagination pagination = new Pagination(5, 0);
        pagination.setPageFactory(pageIndex -> {
            VBox pageBox = new VBox(5);
            pageBox.setStyle("-fx-padding: 10;");
            int start = pageIndex * 4 + 1;
            for (int i = 0; i < 4; i++) {
                int itemNumber = start + i;
                if (itemNumber <= 20) {
                    pageBox.getChildren().add(new Label("Demo Item " + itemNumber));
                }
            }
            return pageBox;
        });

        // Progress Bar & Indicator
        Label progressLabel = new Label("System Loading Progress");
        ProgressBar progressBar = new ProgressBar(0.75);
        ProgressIndicator progressIndicator = new ProgressIndicator(0.75);

        // Button with DropShadow + FadeTransition
        Button animatedButton = new Button("Animated Button");
        animatedButton.setEffect(new DropShadow());
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), animatedButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();

        demoBox.getChildren().addAll(
                title,
                scrollPane,
                pagination,
                progressLabel,
                progressBar,
                progressIndicator,
                animatedButton
        );

        // Polymorphism demonstration
        demonstratePolymorphism();

        return demoBox;
    }

    // ---------- Export Workshop Data (CSV) ----------
    private void exportWorkshopData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Vehicle ID,Registration Number,Make,Model,Year");
            List<Vehicle> vehicles = new com.vis.dao.VehicleDAO().getAll();
            for (Vehicle v : vehicles) {
                writer.println(v.getVehicleId() + "," + v.getRegNumber() + "," + v.getMake() + "," + v.getModel() + "," + v.getYear());
            }
            AlertHelper.showInfo("Workshop data exported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Export failed: " + e.getMessage());
        }
    }

    // ---------- Test Database Connection ----------
    private void testConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                AlertHelper.showInfo("Connection to PostgreSQL successful!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Database connection failed: " + e.getMessage());
        }
    }

    // ---------- My Profile (read‑only) ----------
    private void showProfileDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("My Profile");
        Label username = new Label("Username: " + LoginDialog.getCurrentUser());
        Label role = new Label("Role: " + LoginDialog.getCurrentRole());
        VBox content = new VBox(10, username, role);
        content.setStyle("-fx-padding: 20;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ---------- Logout ----------
    private void logout() {
        LoginDialog.logout();
        primaryStage.close();
        LoginDialog.authenticateAndShowMain();
    }

    // ---------- Polymorphism demonstration ----------
    private void demonstratePolymorphism() {
        TableView<String> dummyTable = new TableView<>();
        ProgressIndicator pi = new ProgressIndicator();

        BaseController<String> demo = new BaseController<String>(dummyTable, pi) {
            @Override
            protected java.util.List<String> fetchData() {
                return Arrays.asList(
                        "Inheritance: BaseController is extended",
                        "Polymorphism: refreshData() calls fetchData()",
                        "ProgressIndicator shows while loading",
                        "No changes made to WorkshopController or CustomerController"
                );
            }
            @Override public void addEntity() {}
            @Override public void updateEntity() {}
            @Override public void deleteEntity() {}
        };

        demo.refreshData();
        System.out.println("Polymorphism demo completed. Loaded " + dummyTable.getItems().size() + " items.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}