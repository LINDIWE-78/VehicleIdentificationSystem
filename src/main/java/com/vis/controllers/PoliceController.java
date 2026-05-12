package com.vis.controllers;

import com.vis.dao.VehicleDAO;
import com.vis.dao.ViolationDAO;
import com.vis.dao.PoliceReportDAO;
import com.vis.models.Vehicle;
import com.vis.models.Violation;
import com.vis.models.PoliceReport;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PoliceController {

    private ViolationDAO violationDAO = new ViolationDAO();
    private PoliceReportDAO reportDAO = new PoliceReportDAO();

    public VBox getView() {
        VBox mainBox = new VBox(10);
        mainBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.6);");
        TabPane tabPane = new TabPane();
        Tab violationsTab = new Tab("Violations", createViolationsView());
        violationsTab.setClosable(false);
        Tab reportsTab = new Tab("Police Reports", createReportsView());
        reportsTab.setClosable(false);
        tabPane.getTabs().addAll(violationsTab, reportsTab);
        mainBox.getChildren().add(tabPane);
        return mainBox;
    }

    private VBox createViolationsView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<Violation> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setPlaceholder(new Label("No violations"));

        // Row transparency
        tableView.setRowFactory(tv -> {
            TableRow<Violation> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupViolationTable(tableView);
        refreshViolations(tableView);

        // Date Range Filter
        HBox dateRangeBox = new HBox(10);
        dateRangeBox.setAlignment(Pos.CENTER_LEFT);
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start date");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End date");
        Button filterBtn = new Button("Apply Date Filter");
        Button clearFilterBtn = new Button("Clear Filter");
        dateRangeBox.getChildren().addAll(new Label("Filter by date:"), startDatePicker, endDatePicker, filterBtn, clearFilterBtn);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by violation type or status...");

        // Combined filter action
        Runnable applyFilters = () -> {
            try {
                List<Violation> all = violationDAO.getAll();
                LocalDate start = startDatePicker.getValue();
                LocalDate end = endDatePicker.getValue();
                String search = searchField.getText().trim().toLowerCase();

                List<Violation> filtered = all.stream()
                        .filter(v -> (start == null || !v.getViolationDate().isBefore(start)) &&
                                (end == null || !v.getViolationDate().isAfter(end)) &&
                                (search.isEmpty() || v.getViolationType().toLowerCase().contains(search) ||
                                        v.getStatus().toLowerCase().contains(search)))
                        .collect(Collectors.toList());
                tableView.getItems().setAll(filtered);
            } catch (Exception e) {
                AlertHelper.showError("Filter failed: " + e.getMessage());
            }
        };

        filterBtn.setOnAction(e -> applyFilters.run());
        clearFilterBtn.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            searchField.clear();
            refreshViolations(tableView);
        });
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters.run());

        Button btnAdd = new Button("Add Violation (uses procedure)");
        Button btnUpdate = new Button("Update Status");
        Button btnDelete = new Button("Delete Violation");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addViolation(tableView));
        btnUpdate.setOnAction(e -> updateViolationStatus(tableView));
        btnDelete.setOnAction(e -> deleteViolation(tableView));
        btnRefresh.setOnAction(e -> {
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            searchField.clear();
            refreshViolations(tableView);
        });

        String role = LoginDialog.getCurrentRole();
        if (!"ADMIN".equals(role)) {
            btnDelete.setDisable(true);
            btnDelete.setTooltip(new Tooltip("Only ADMIN can delete violations."));
        }

        //  HORIZONTAL BUTTON BOX (ADDED)
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);
        vbox.getChildren().addAll(dateRangeBox, searchField, tableView, buttonBox);

        return vbox;
    }

    private VBox createReportsView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<PoliceReport> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent");
        tableView.setPlaceholder(new Label("No police reports"));

        tableView.setRowFactory(tv -> {
            TableRow<PoliceReport> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupReportTable(tableView);
        refreshReports(tableView);

        Button btnAdd = new Button("Add Police Report");
        Button btnDelete = new Button("Delete Report");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addPoliceReport(tableView));
        btnDelete.setOnAction(e -> deletePoliceReport(tableView));
        btnRefresh.setOnAction(e -> refreshReports(tableView));

        // ---------- HORIZONTAL BUTTON BOX (ADDED) ----------
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(btnAdd, btnDelete, btnRefresh);
        vbox.getChildren().addAll(tableView, buttonBox);
        // -------------------------------------------------
        return vbox;
    }

    // ---------- Violation methods (unchanged) ----------
    private void setupViolationTable(TableView<Violation> tableView) {
        TableColumn<Violation, Integer> idCol = new TableColumn<>("Violation ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getViolationId()).asObject());
        TableColumn<Violation, Integer> vehicleCol = new TableColumn<>("Vehicle ID");
        vehicleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVehicleId()).asObject());
        TableColumn<Violation, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getViolationDate()));
        TableColumn<Violation, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getViolationType()));
        TableColumn<Violation, Number> amountCol = new TableColumn<>("Fine Amount");
        amountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getFineAmount()));
        TableColumn<Violation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        tableView.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, amountCol, statusCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshViolations(TableView<Violation> tableView) {
        try { tableView.getItems().setAll(violationDAO.getAll()); }
        catch (Exception e) { AlertHelper.showError("Failed to load violations: " + e.getMessage()); }
    }

    private void addViolation(TableView<Violation> tableView) {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole()) && !"POLICE".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("No permission to add violations.");
            return;
        }
        Dialog<Violation> dialog = new Dialog<>();
        dialog.setTitle("Add Violation (uses stored procedure)");
        dialog.setHeaderText("Enter violation details");
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);
        VBox vbox = new VBox(10);
        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        try {
            VehicleDAO vehicleDAO = new VehicleDAO();
            List<Vehicle> vehicles = vehicleDAO.getAll();
            vehicleCombo.getItems().addAll(vehicles);
            vehicleCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override protected void updateItem(Vehicle v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty ? null : v.getRegNumber() + " - " + v.getMake() + " " + v.getModel());
                }
            });
            vehicleCombo.setButtonCell(vehicleCombo.getCellFactory().call(null));
        } catch (Exception e) { vehicleCombo.setPromptText("Error loading vehicles"); }
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField typeField = new TextField(); typeField.setPromptText("Violation Type");
        TextField amountField = new TextField(); amountField.setPromptText("Fine Amount");
        vbox.getChildren().addAll(vehicleCombo, datePicker, typeField, amountField);
        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(button -> {
            if (button == addButton) {
                try {
                    Vehicle selected = vehicleCombo.getValue();
                    if (selected == null) { AlertHelper.showError("Select a vehicle"); return null; }
                    Violation v = new Violation();
                    v.setVehicleId(selected.getVehicleId());
                    v.setViolationDate(datePicker.getValue());
                    v.setViolationType(typeField.getText());
                    v.setFineAmount(new java.math.BigDecimal(amountField.getText()));
                    return v;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid fine amount");
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(v -> {
            try {
                violationDAO.insert(v); refreshViolations(tableView); AlertHelper.showInfo("Violation added via stored procedure");
            }
            catch (Exception e) {
                AlertHelper.showError("Add failed: " + e.getMessage());
            }
        });
    }

    private void updateViolationStatus(TableView<Violation> tableView) {
        Violation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("No violation selected"); return; }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Violation Status");
        dialog.setHeaderText("Current status: " + selected.getStatus());
        ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Paid", "Unpaid");
        statusCombo.setValue(selected.getStatus());
        dialog.getDialogPane().setContent(statusCombo);
        dialog.setResultConverter(button -> button == updateButton ? statusCombo.getValue() : null);
        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                selected.setStatus(newStatus); violationDAO.update(selected); refreshViolations(tableView); AlertHelper.showInfo("Status updated");
            }
            catch (Exception e) { AlertHelper.showError("Update failed: " + e.getMessage()); }
        });
    }

    private void deleteViolation(TableView<Violation> tableView) {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("Only ADMIN can delete violations.");
            return;
        }
        Violation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("No violation selected"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete violation #" + selected.getViolationId() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                violationDAO.delete(selected.getViolationId()); refreshViolations(tableView); AlertHelper.showInfo("Violation deleted");
            }
            catch (Exception e) { AlertHelper.showError("Delete failed: " + e.getMessage()); }
        }
    }

    // ---------- Police Report methods (unchanged) ----------
    private void setupReportTable(TableView<PoliceReport> tableView) {
        TableColumn<PoliceReport, Integer> idCol = new TableColumn<>("Report ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getReportId()).asObject());
        TableColumn<PoliceReport, Integer> vehicleCol = new TableColumn<>("Vehicle ID");
        vehicleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVehicleId()).asObject());
        TableColumn<PoliceReport, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getReportDate()));
        TableColumn<PoliceReport, String> typeCol = new TableColumn<>("Report Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getReportType()));
        TableColumn<PoliceReport, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        TableColumn<PoliceReport, String> officerCol = new TableColumn<>("Officer Name");
        officerCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOfficerName()));
        tableView.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, officerCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshReports(TableView<PoliceReport> tableView) {
        try { tableView.getItems().setAll(reportDAO.getAll()); }
        catch (Exception e) { AlertHelper.showError("Failed to load police reports: " + e.getMessage()); }
    }

    private void addPoliceReport(TableView<PoliceReport> tableView) {
        Dialog<PoliceReport> dialog = new Dialog<>();
        dialog.setTitle("Add Police Report");
        dialog.setHeaderText("Enter report details");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        try {
            VehicleDAO vehicleDAO = new VehicleDAO();
            List<Vehicle> vehicles = vehicleDAO.getAll();
            vehicleCombo.getItems().addAll(vehicles);
            vehicleCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override protected void updateItem(Vehicle v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty ? null : v.getRegNumber() + " - " + v.getMake() + " " + v.getModel());
                }
            });
            vehicleCombo.setButtonCell(vehicleCombo.getCellFactory().call(null));
        } catch (Exception e) { vehicleCombo.setPromptText("Error loading vehicles"); }
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Accident", "Theft", "Stolen", "Recovered");
        typeCombo.setValue("Accident");
        TextArea descArea = new TextArea(); descArea.setPromptText("Description"); descArea.setPrefRowCount(3);
        TextField officerField = new TextField(); officerField.setPromptText("Officer Name");
        grid.add(new Label("Vehicle:"), 0, 0); grid.add(vehicleCombo, 1, 0);
        grid.add(new Label("Date:"), 0, 1); grid.add(datePicker, 1, 1);
        grid.add(new Label("Report Type:"), 0, 2); grid.add(typeCombo, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descArea, 1, 3);
        grid.add(new Label("Officer Name:"), 0, 4); grid.add(officerField, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == addBtn) {
                Vehicle selected = vehicleCombo.getValue();
                if (selected == null) { AlertHelper.showError("Select a vehicle"); return null; }
                PoliceReport r = new PoliceReport();
                r.setVehicleId(selected.getVehicleId());
                r.setReportDate(datePicker.getValue());
                r.setReportType(typeCombo.getValue());
                r.setDescription(descArea.getText());
                r.setOfficerName(officerField.getText());
                return r;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(r -> {
            try { reportDAO.insert(r); refreshReports(tableView); AlertHelper.showInfo("Police report added."); }
            catch (Exception e) { AlertHelper.showError("Add failed: " + e.getMessage()); }
        });
    }

    private void deletePoliceReport(TableView<PoliceReport> tableView) {
        PoliceReport selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No report selected");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete report #" + selected.getReportId() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                reportDAO.delete(selected.getReportId()); refreshReports(tableView); AlertHelper.showInfo("Report deleted.");
            }
            catch (Exception e) {
                AlertHelper.showError("Delete failed: " + e.getMessage());
            }
        }
    }
}