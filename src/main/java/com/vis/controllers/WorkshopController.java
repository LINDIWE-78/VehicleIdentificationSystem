package com.vis.controllers;

import com.vis.dao.CustomerDAO;
import com.vis.dao.VehicleDAO;
import com.vis.models.Customer;
import com.vis.models.Vehicle;
import com.vis.utils.AlertHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class WorkshopController extends BaseController<Vehicle> {
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private TextField searchField;

    public WorkshopController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        refreshData();
    }

    private void setupTableView() {
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

    @Override
    protected List<Vehicle> fetchData() throws Exception {
        return vehicleDAO.getAllWithOwner();
    }

    @Override
    public void addEntity() {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Add Vehicle");
        dialog.setHeaderText("Enter vehicle details");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        TextField regField = new TextField();
        regField.setPromptText("Registration Number");
        TextField makeField = new TextField();
        makeField.setPromptText("Make");
        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        TextField yearField = new TextField();
        yearField.setPromptText("Year");

        // ComboBox for owner selection
        ComboBox<Customer> ownerCombo = new ComboBox<>();
        ownerCombo.setPromptText("Select Owner");
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            List<Customer> customers = customerDAO.getAll();
            ownerCombo.getItems().addAll(customers);
            // Display customer name and ID in the dropdown
            ownerCombo.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer customer, boolean empty) {
                    super.updateItem(customer, empty);
                    setText(empty ? null : customer.getName() + " (ID: " + customer.getCustomerId() + ")");
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
                    Customer selectedOwner = ownerCombo.getValue();
                    if (selectedOwner == null) {
                        AlertHelper.showError("Please select an owner");
                        return null;
                    }
                    v.setOwnerId(selectedOwner.getCustomerId());
                    return v;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid year");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(v -> {
            try {
                vehicleDAO.insert(v);
                refreshData();
                AlertHelper.showInfo("Vehicle added successfully");
            } catch (Exception e) {
                AlertHelper.showError("Failed to add vehicle: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateEntity() {
        Vehicle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No vehicle selected");
            return;
        }
        AlertHelper.showInfo("Update feature – implement similarly to add");
    }

    @Override
    public void deleteEntity() {
        Vehicle selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No vehicle selected");
            return;
        }
        try {
            vehicleDAO.delete(selected.getVehicleId());
            refreshData();
            AlertHelper.showInfo("Vehicle deleted");
        } catch (Exception e) {
            AlertHelper.showError("Delete failed: " + e.getMessage());
        }
    }

    public List<Vehicle> getCurrentData() {
        return tableView.getItems();
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");

        // Header
        Label header = new Label("Workshop - Vehicle Management");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Search bar
        searchField = new TextField();
        searchField.setPromptText("🔍 Search by registration or make...");
        searchField.setStyle("-fx-padding: 8; -fx-background-radius: 5;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                refreshData();
            } else {
                String filter = newVal.toLowerCase();
                tableView.setItems(tableView.getItems().filtered(v ->
                        v.getRegNumber().toLowerCase().contains(filter) ||
                                v.getMake().toLowerCase().contains(filter)
                ));
            }
        });

        Button btnAdd = new Button("Add Vehicle");
        Button btnDelete = new Button("Delete Vehicle");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setTooltip(new Tooltip("Add a new vehicle"));
        btnDelete.setTooltip(new Tooltip("Delete selected vehicle"));
        btnRefresh.setTooltip(new Tooltip("Refresh vehicle list (clears search)"));

        btnAdd.setOnAction(e -> addEntity());
        btnDelete.setOnAction(e -> deleteEntity());
        btnRefresh.setOnAction(e -> {
            searchField.clear();
            refreshData();
        });

        vbox.getChildren().addAll(header, searchField, tableView, progressIndicator, btnAdd, btnDelete, btnRefresh);
        return vbox;
    }
}