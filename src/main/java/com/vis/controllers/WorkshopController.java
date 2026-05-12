
package com.vis.controllers;

import com.vis.dao.CustomerDAO;
import com.vis.dao.VehicleDAO;
import com.vis.dao.ServiceRecordDAO;
import com.vis.models.Customer;
import com.vis.models.Vehicle;
import com.vis.models.ServiceRecord;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;        // ADDED for horizontal buttons
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.util.List;

public class WorkshopController {

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private ServiceRecordDAO serviceRecordDAO = new ServiceRecordDAO();

    public VBox getView() {
        VBox mainBox = new VBox(10);
        mainBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.6);");
        TabPane tabPane = new TabPane();
        Tab vehiclesTab = new Tab("Vehicles", createVehiclesView());
        vehiclesTab.setClosable(false);
        Tab serviceTab = new Tab("Service Records", createServiceRecordsView());
        serviceTab.setClosable(false);
        tabPane.getTabs().addAll(vehiclesTab, serviceTab);
        mainBox.getChildren().add(tabPane);
        return mainBox;
    }

    private VBox createVehiclesView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<Vehicle> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setPlaceholder(new Label("No vehicles"));

        // Make rows semi‑transparent dark (background image shows through)
        tableView.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupVehicleTable(tableView);
        refreshVehicles(tableView);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by registration or make...");
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) refreshVehicles(tableView);
            else tableView.setItems(tableView.getItems().filtered(v ->
                    v.getRegNumber().toLowerCase().contains(newVal.toLowerCase()) ||
                            v.getMake().toLowerCase().contains(newVal.toLowerCase())));
        });

        Button btnAdd = new Button("Add Vehicle");
        Button btnDelete = new Button("Delete Vehicle");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addVehicle(tableView));
        btnDelete.setOnAction(e -> deleteVehicle(tableView));
        btnRefresh.setOnAction(e -> { refreshVehicles(tableView); searchField.clear(); });

        String role = LoginDialog.getCurrentRole();
        if (!"ADMIN".equals(role) && !"WORKSHOP".equals(role)) {
            btnDelete.setDisable(true);
            btnDelete.setTooltip(new Tooltip("You do not have permission to delete vehicles."));
        }

        //  HORIZONTAL BUTTON BOX (ADDED)
        HBox buttonBox = new HBox(10);          // 10 pixels spacing
        buttonBox.getChildren().addAll(btnAdd, btnDelete, btnRefresh);
        vbox.getChildren().addAll(searchField, tableView, buttonBox);

        return vbox;
    }

    private VBox createServiceRecordsView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<ServiceRecord> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setPlaceholder(new Label("No service records"));

        // Row transparency
        tableView.setRowFactory(tv -> {
            TableRow<ServiceRecord> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupServiceRecordTable(tableView);
        refreshServiceRecords(tableView);

        Button btnAdd = new Button("Add Service Record");
        Button btnDelete = new Button("Delete Service Record");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addServiceRecord(tableView));
        btnDelete.setOnAction(e -> deleteServiceRecord(tableView));
        btnRefresh.setOnAction(e -> refreshServiceRecords(tableView));

        vbox.getChildren().addAll(tableView, btnAdd, btnDelete, btnRefresh);
        return vbox;
    }

    // Vehicle methods
    private void setupVehicleTable(TableView<Vehicle> tableView) {
        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVehicleId()).asObject());
        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRegNumber()));
        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMake()));
        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getModel()));
        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getYear()).asObject());
        TableColumn<Vehicle, String> ownerCol = new TableColumn<>("Owner Name");
        ownerCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOwnerName()));
        tableView.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol, ownerCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshVehicles(TableView<Vehicle> tableView) {
        try { tableView.getItems().setAll(vehicleDAO.getAllWithOwner()); }
        catch (Exception e) { AlertHelper.showError("Failed to load vehicles: " + e.getMessage()); }
    }

    private void addVehicle(TableView<Vehicle> tableView) {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole()) && !"WORKSHOP".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("No permission to add vehicles.");
            return;
        }
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Add Vehicle");
        dialog.setHeaderText("Enter vehicle details");
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);
        VBox vbox = new VBox(10);
        TextField regField = new TextField(); regField.setPromptText("Registration Number");
        TextField makeField = new TextField(); makeField.setPromptText("Make");
        TextField modelField = new TextField(); modelField.setPromptText("Model");
        TextField yearField = new TextField(); yearField.setPromptText("Year");
        ComboBox<Customer> ownerCombo = new ComboBox<>();
        ownerCombo.setPromptText("Select Owner");
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> customers = customerDAO.getAll();
            ownerCombo.getItems().addAll(customers);
            ownerCombo.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer c, boolean empty) {
                    super.updateItem(c, empty);
                    setText(empty ? null : c.getName() + " (ID: " + c.getCustomerId() + ")");
                }
            });
            ownerCombo.setButtonCell(ownerCombo.getCellFactory().call(null));
        } catch (Exception e) {
            ownerCombo.setPromptText("Error loading customers");
        }
        vbox.getChildren().addAll(regField, makeField, modelField, yearField, ownerCombo);
        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(button -> {
            if (button == addButton) {
                try {
                    Vehicle v = new Vehicle();
                    v.setRegNumber(regField.getText());
                    v.setMake(makeField.getText());
                    v.setModel(modelField.getText());
                    v.setYear(Integer.parseInt(yearField.getText()));
                    Customer selected = ownerCombo.getValue();
                    if (selected == null) { AlertHelper.showError("Select an owner"); return null; }
                    v.setOwnerId(selected.getCustomerId());
                    return v;
                } catch (NumberFormatException e) { AlertHelper.showError("Invalid year"); return null; }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(v -> {
            try { vehicleDAO.insert(v); refreshVehicles(tableView); AlertHelper.showInfo("Vehicle added"); }
            catch (Exception e) { AlertHelper.showError("Add failed: " + e.getMessage()); }
        });
    }

    private void deleteVehicle(TableView<Vehicle> tableView) {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole()) && !"WORKSHOP".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("No permission to delete vehicles.");
            return;
        }
        Vehicle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("No vehicle selected"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getRegNumber() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { vehicleDAO.delete(selected.getVehicleId()); refreshVehicles(tableView); AlertHelper.showInfo("Vehicle deleted"); }
            catch (Exception e) { AlertHelper.showError("Delete failed: " + e.getMessage()); }
        }
    }

    //  Service Record methods
    private void setupServiceRecordTable(TableView<ServiceRecord> tableView) {
        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("Service ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getServiceId()).asObject());
        TableColumn<ServiceRecord, Integer> vehicleCol = new TableColumn<>("Vehicle ID");
        vehicleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVehicleId()).asObject());
        TableColumn<ServiceRecord, LocalDate> dateCol = new TableColumn<>("Service Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getServiceDate()));
        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getServiceType()));
        TableColumn<ServiceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        TableColumn<ServiceRecord, Number> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCost()));
        tableView.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, costCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshServiceRecords(TableView<ServiceRecord> tableView) {
        try {
            tableView.getItems().setAll(serviceRecordDAO.getAll()); }
        catch (Exception e) {
            AlertHelper.showError("Failed to load service records: " + e.getMessage());
        }
    }

    private void addServiceRecord(TableView<ServiceRecord> tableView) {
        Dialog<ServiceRecord> dialog = new Dialog<>();
        dialog.setTitle("Add Service Record");
        dialog.setHeaderText("Enter service details");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        try {
            List<Vehicle> vehicles = vehicleDAO.getAll();
            vehicleCombo.getItems().addAll(vehicles);
            vehicleCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty ? null : v.getRegNumber() + " - " + v.getMake() + " " + v.getModel());
                }
            });
            vehicleCombo.setButtonCell(vehicleCombo.getCellFactory().call(null));
        } catch (Exception e) {
            vehicleCombo.setPromptText("Error loading vehicles");
        }
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField typeField = new TextField(); typeField.setPromptText("Service Type");
        TextArea descArea = new TextArea(); descArea.setPromptText("Description"); descArea.setPrefRowCount(3);
        TextField costField = new TextField(); costField.setPromptText("Cost");
        grid.add(new Label("Vehicle:"), 0, 0); grid.add(vehicleCombo, 1, 0);
        grid.add(new Label("Service Date:"), 0, 1); grid.add(datePicker, 1, 1);
        grid.add(new Label("Service Type:"), 0, 2); grid.add(typeField, 1, 2);
        grid.add(new Label("Description:"), 0, 3); grid.add(descArea, 1, 3);
        grid.add(new Label("Cost:"), 0, 4); grid.add(costField, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == addBtn) {
                Vehicle selected = vehicleCombo.getValue();
                if (selected == null) { AlertHelper.showError("Select a vehicle"); return null; }
                try {
                    ServiceRecord r = new ServiceRecord();
                    r.setVehicleId(selected.getVehicleId());
                    r.setServiceDate(datePicker.getValue());
                    r.setServiceType(typeField.getText());
                    r.setDescription(descArea.getText());
                    r.setCost(new java.math.BigDecimal(costField.getText()));
                    return r;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid cost");
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(r -> {
            try {
                serviceRecordDAO.insert(r); refreshServiceRecords(tableView); AlertHelper.showInfo("Service record added.");
            }
            catch (Exception e) {
                AlertHelper.showError("Add failed: " + e.getMessage());
            }
        });
    }

    private void deleteServiceRecord(TableView<ServiceRecord> tableView) {
        ServiceRecord selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a record to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete service record #" + selected.getServiceId() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                serviceRecordDAO.delete(selected.getServiceId()); refreshServiceRecords(tableView); AlertHelper.showInfo("Service record deleted."); }
            catch (Exception e) {
                AlertHelper.showError("Delete failed: " + e.getMessage());
            }
        }
    }

    public List<Vehicle> getCurrentData() {
        try { return vehicleDAO.getAllWithOwner(); }
        catch (Exception e) { return null; }
    }
}