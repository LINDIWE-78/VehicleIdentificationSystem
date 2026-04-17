package com.vis.vehicleidentificationsystem;

import com.vis.controllers.*;
import com.vis.models.Vehicle;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;
    private TabPane tabPane;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        mainScene = buildMainScene();
        LoginDialog.authenticateAndShowMain();
    }

    public static void showMainWindow() {
        primaryStage.setTitle("Vehicle Identification System");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private Scene buildMainScene() {
        BorderPane root = new BorderPane();

        // Menu Bar
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem exportItem = new MenuItem("Export Workshop Data");
        exportItem.setOnAction(e -> exportWorkshopData());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        fileMenu.getItems().addAll(exportItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // TabPane
        tabPane = new TabPane();
        WorkshopController workshopController = new WorkshopController();
        CustomerController customerController = new CustomerController();
        InsuranceController insuranceController = new InsuranceController();
        PoliceController policeController = new PoliceController();
        AdminController adminController = new AdminController();
        StatisticsController statisticsController = new StatisticsController();

        Tab workshopTab = new Tab("Workshop", workshopController.getView());
        Tab customerTab = new Tab("Customer", customerController.getView());
        Tab insuranceTab = new Tab("Insurance", insuranceController.getView());
        Tab policeTab = new Tab("Police", policeController.getView());
        Tab adminTab = new Tab("Admin", adminController.getView());
        Tab statsTab = new Tab("Statistics", statisticsController.getView());
        Tab demoTab = new Tab("Demo", createDemoPane());

        tabPane.getTabs().addAll(workshopTab, customerTab, insuranceTab, policeTab, adminTab, statsTab, demoTab);
        root.setCenter(tabPane);

        // Status bar
        Label statusLabel = new Label("✅ Connected to PostgreSQL | Logged in as: " + LoginDialog.getCurrentUser());
        statusLabel.setStyle("-fx-padding: 5; -fx-background-color: #2c3e50; -fx-text-fill: white;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1200, 700);
        String cssPath = getClass().getResource("/style.css") != null ?
                getClass().getResource("/style.css").toExternalForm() : null;
        if (cssPath != null) scene.getStylesheets().add(cssPath);
        return scene;
    }

    private VBox createDemoPane() {
        VBox demoBox = new VBox(10);
        demoBox.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");

        Label scrollLabel = new Label("ScrollPane (20 items):");
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(5);
        for (int i = 1; i <= 20; i++) {
            content.getChildren().add(new Label("Dummy Element " + i));
        }
        scrollPane.setContent(content);
        scrollPane.setPrefHeight(200);

        Label paginationLabel = new Label("Pagination:");
        Pagination pagination = new Pagination(5, 0);
        pagination.setPageFactory(pageIndex -> {
            VBox pageContent = new VBox(5);
            for (int i = 1; i <= 4; i++) {
                pageContent.getChildren().add(new Label("Page " + (pageIndex + 1) + " - Item " + i));
            }
            return pageContent;
        });

        Label progLabel = new Label("Progress Indicators:");
        ProgressBar progressBar = new ProgressBar(0.5);
        ProgressIndicator progressIndicator = new ProgressIndicator(0.5);

        Label effectLabel = new Label("Button with DropShadow + FadeTransition:");
        Button effectButton = new Button("Animated Button");
        effectButton.setEffect(new DropShadow());
        FadeTransition fade = new FadeTransition(Duration.seconds(2), effectButton);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        demoBox.getChildren().addAll(
                scrollLabel, scrollPane,
                paginationLabel, pagination,
                progLabel, progressBar, progressIndicator,
                effectLabel, effectButton
        );
        return demoBox;
    }

    private void exportWorkshopData() {
        Tab workshopTab = tabPane.getTabs().get(0);
        WorkshopController controller = (WorkshopController) workshopTab.getUserData();
        if (controller == null) {
            AlertHelper.showError("Workshop controller not accessible");
            return;
        }
        List<Vehicle> vehicles = controller.getCurrentData();
        if (vehicles == null || vehicles.isEmpty()) {
            AlertHelper.showError("No data to export");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Workshop Export");
        fileChooser.setInitialFileName("workshop_export_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());
        if (file == null) return;
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("ID,Registration,Make,Model,Year,Owner");
            for (Vehicle v : vehicles) {
                writer.printf("%d,%s,%s,%s,%d,%s%n",
                        v.getVehicleId(), v.getRegNumber(), v.getMake(), v.getModel(),
                        v.getYear(), v.getOwnerName() != null ? v.getOwnerName() : "");
            }
            AlertHelper.showInfo("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            AlertHelper.showError("Export failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}