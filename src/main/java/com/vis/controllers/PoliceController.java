package com.vis.controllers;

import com.vis.dao.VehicleDAO;
import com.vis.dao.ViolationDAO;
import com.vis.models.Vehicle;
import com.vis.models.Violation;
import com.vis.utils.AlertHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;

public class PoliceController extends BaseController<Violation> {
    private ViolationDAO violationDAO = new ViolationDAO();

    public PoliceController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        refreshData();
    }

    private void setupTableView() {
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

    @Override
    protected List<Violation> fetchData() throws Exception {
        return violationDAO.getAll();
    }

    @Override
    public void addEntity() {
        Dialog<Violation> dialog = new Dialog<>();
        dialog.setTitle("Add Violation (uses stored procedure)");
        dialog.setHeaderText("Enter violation details");
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        VBox vbox = new VBox(10);

        // Vehicle selection dropdown – avoids foreign key errors
        ComboBox<Vehicle> vehicleCombo = new ComboBox<>();
        vehicleCombo.setPromptText("Select Vehicle");
        try {
            VehicleDAO vehicleDAO = new VehicleDAO();
            List<Vehicle> vehicles = vehicleDAO.getAll();
            vehicleCombo.getItems().addAll(vehicles);
            vehicleCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle vehicle, boolean empty) {
                    super.updateItem(vehicle, empty);
                    setText(empty ? null : vehicle.getRegNumber() + " - " + vehicle.getMake() + " " + vehicle.getModel());
                }
            });
            vehicleCombo.setButtonCell(vehicleCombo.getCellFactory().call(null));
        } catch (Exception e) {
            vehicleCombo.setPromptText("Error loading vehicles");
        }

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPromptText("Violation Date");
        TextField typeField = new TextField();
        typeField.setPromptText("Violation Type");
        TextField amountField = new TextField();
        amountField.setPromptText("Fine Amount");

        vbox.getChildren().addAll(vehicleCombo, datePicker, typeField, amountField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                try {
                    Vehicle selectedVehicle = vehicleCombo.getValue();
                    if (selectedVehicle == null) {
                        AlertHelper.showError("Please select a vehicle");
                        return null;
                    }
                    Violation v = new Violation();
                    v.setVehicleId(selectedVehicle.getVehicleId());
                    v.setViolationDate(datePicker.getValue());
                    v.setViolationType(typeField.getText());
                    v.setFineAmount(new java.math.BigDecimal(amountField.getText()));
                    return v;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid number format for fine amount");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(v -> {
            try {
                violationDAO.insert(v);
                refreshData();
                AlertHelper.showInfo("Violation added via stored procedure 'add_violation'");
            } catch (Exception e) {
                AlertHelper.showError("Failed to add violation: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateEntity() {
        Violation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No violation selected");
            return;
        }
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
                selected.setStatus(newStatus);
                violationDAO.update(selected);
                refreshData();
                AlertHelper.showInfo("Status updated");
            } catch (Exception e) {
                AlertHelper.showError("Update failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void deleteEntity() {
        Violation selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No violation selected");
            return;
        }
        try {
            violationDAO.delete(selected.getViolationId());
            refreshData();
            AlertHelper.showInfo("Violation deleted");
        } catch (Exception e) {
            AlertHelper.showError("Delete failed: " + e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");
        Label header = new Label("Police - Traffic Violations");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnAdd = new Button("Add Violation (uses procedure)");
        Button btnUpdate = new Button("Update Status");
        Button btnDelete = new Button("Delete Violation");
        Button btnRefresh = new Button("Refresh");

        btnAdd.setTooltip(new Tooltip("Add a new traffic violation (calls stored procedure)"));
        btnUpdate.setTooltip(new Tooltip("Update violation status (Paid/Unpaid)"));
        btnDelete.setTooltip(new Tooltip("Delete selected violation"));
        btnRefresh.setTooltip(new Tooltip("Refresh violation list"));

        btnAdd.setOnAction(e -> addEntity());
        btnUpdate.setOnAction(e -> updateEntity());
        btnDelete.setOnAction(e -> deleteEntity());
        btnRefresh.setOnAction(e -> refreshData());

        vbox.getChildren().addAll(header, tableView, progressIndicator, btnAdd, btnUpdate, btnDelete, btnRefresh);
        return vbox;
    }
}