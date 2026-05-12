package com.vis.controllers;

import com.vis.dao.CustomerDAO;
import com.vis.dao.VehicleDAO;
import com.vis.dao.CustomerQueryDAO;
import com.vis.models.Customer;
import com.vis.models.Vehicle;
import com.vis.models.CustomerQuery;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerController {

    private TableView<Customer> customerTable;
    private TableView<Vehicle> vehicleTable;
    private TableView<CustomerQuery> queryTable;
    private TextArea queryTextArea;
    private CustomerDAO customerDAO = new CustomerDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private CustomerQueryDAO queryDAO = new CustomerQueryDAO();

    public VBox getView() {
        VBox vbox = new VBox(15);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: rgba(0,0,0,0.6);");

        // Profile section
        Label profileHeader = new Label("Your Profile");
        profileHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a9eff;");

        customerTable = new TableView<>();
        customerTable.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        // Row transparency
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });
        setupCustomerTable();
        loadLoggedInCustomer();

        Button updateProfileBtn = new Button("Update My Profile");
        updateProfileBtn.setOnAction(e -> updateOwnProfile());

        // Vehicles section
        Label vehicleHeader = new Label("My Vehicles");
        vehicleHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a9eff;");

        vehicleTable = new TableView<>();
        vehicleTable.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        vehicleTable.setRowFactory(tv -> {
            TableRow<Vehicle> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });
        setupVehicleTable();
        loadMyVehicles();

        // Queries section
        Label queryHeader = new Label("My Queries");
        queryHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a9eff;");

        queryTable = new TableView<>();
        queryTable.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        queryTable.setRowFactory(tv -> {
            TableRow<CustomerQuery> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });
        setupQueryTable();
        loadMyQueries();

        // Submit query form
        GridPane queryForm = new GridPane();
        queryForm.setHgap(10);
        queryForm.setVgap(10);
        queryForm.setPadding(new Insets(10, 0, 10, 0));
        Label askLabel = new Label("Ask about your vehicle:");
        queryTextArea = new TextArea();
        queryTextArea.setPromptText("Type your question here...");
        queryTextArea.setPrefRowCount(3);
        queryTextArea.setPrefWidth(400);
        Button submitQueryBtn = new Button("Submit Query");
        submitQueryBtn.setStyle("-fx-background-color: #2ecc71;");
        submitQueryBtn.setOnAction(e -> submitNewQuery());
        queryForm.add(askLabel, 0, 0);
        queryForm.add(queryTextArea, 0, 1);
        queryForm.add(submitQueryBtn, 0, 2);

        vbox.getChildren().addAll(profileHeader, customerTable, updateProfileBtn,
                vehicleHeader, vehicleTable, queryHeader, queryTable, queryForm);
        return vbox;
    }

    // Customer profile
    private void setupCustomerTable() {
        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCustomerId()).asObject());
        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getAddress()));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));
        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        customerTable.getColumns().addAll(idCol, nameCol, addressCol, phoneCol, emailCol);
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadLoggedInCustomer() {
        String currentUser = LoginDialog.getCurrentUser();
        try {
            Customer c = customerDAO.findByEmail(currentUser);
            if (c != null) {
                customerTable.getItems().setAll(c);
            } else {
                customerTable.setPlaceholder(new Label("No customer record found for this user."));
                System.out.println("DEBUG: No customer with email: " + currentUser);
            }
        } catch (Exception e) {
            AlertHelper.showError("Failed to load profile: " + e.getMessage());
        }
    }

    private void updateOwnProfile() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Please select your record to update.");
            return;
        }
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Update Your Profile");
        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField addressField = new TextField(selected.getAddress());
        TextField phoneField = new TextField(selected.getPhone());
        TextField emailField = new TextField(selected.getEmail());

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1); grid.add(addressField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(emailField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == updateBtn) {
                selected.setName(nameField.getText());
                selected.setAddress(addressField.getText());
                selected.setPhone(phoneField.getText());
                selected.setEmail(emailField.getText());
                return selected;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(c -> {
            try {
                customerDAO.update(c);
                loadLoggedInCustomer();
                AlertHelper.showInfo("Profile updated.");
            } catch (Exception e) {
                AlertHelper.showError("Update failed: " + e.getMessage());
            }
        });
    }

    //  My Vehicles
    private void setupVehicleTable() {
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
        vehicleTable.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol);
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadMyVehicles() {
        String currentUser = LoginDialog.getCurrentUser();
        try {
            Customer c = customerDAO.findByEmail(currentUser);
            if (c != null) {
                List<Vehicle> vehicles = vehicleDAO.findByOwnerId(c.getCustomerId());
                vehicleTable.getItems().setAll(vehicles);
                if (vehicles.isEmpty()) {
                    vehicleTable.setPlaceholder(new Label("You have no registered vehicles."));
                }
            } else {
                vehicleTable.setPlaceholder(new Label("No customer record."));
            }
        } catch (Exception e) {
            AlertHelper.showError("Failed to load vehicles: " + e.getMessage());
        }
    }

    //  Queries
    private void setupQueryTable() {
        TableColumn<CustomerQuery, Integer> qidCol = new TableColumn<>("ID");
        qidCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQueryId()).asObject());
        TableColumn<CustomerQuery, String> qtextCol = new TableColumn<>("Your Question");
        qtextCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getQueryText()));
        TableColumn<CustomerQuery, String> responseCol = new TableColumn<>("Response");
        responseCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getResponseText()));
        TableColumn<CustomerQuery, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getQueryDate() != null ? c.getValue().getQueryDate().toString() : ""));
        queryTable.getColumns().addAll(qidCol, qtextCol, responseCol, dateCol);
        queryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadMyQueries() {
        String currentUser = LoginDialog.getCurrentUser();
        try {
            Customer c = customerDAO.findByEmail(currentUser);
            if (c != null) {
                List<CustomerQuery> queries = queryDAO.findByCustomerId(c.getCustomerId());
                queryTable.getItems().setAll(queries);
                if (queries.isEmpty()) {
                    queryTable.setPlaceholder(new Label("No queries yet."));
                }
            } else {
                queryTable.setPlaceholder(new Label("No customer record."));
            }
        } catch (Exception e) {
            AlertHelper.showError("Failed to load queries: " + e.getMessage());
        }
    }

    private void submitNewQuery() {
        String question = queryTextArea.getText().trim();
        if (question.isEmpty()) {
            AlertHelper.showError("Please enter a question.");
            return;
        }
        String currentUser = LoginDialog.getCurrentUser();
        try {
            Customer c = customerDAO.findByEmail(currentUser);
            if (c == null) {
                AlertHelper.showError("No customer record found.");
                return;
            }
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(c.getCustomerId());
            if (vehicles.isEmpty()) {
                AlertHelper.showError("You have no registered vehicles to ask about.");
                return;
            }
            int vehicleId = vehicles.get(0).getVehicleId();

            CustomerQuery query = new CustomerQuery();
            query.setCustomerId(c.getCustomerId());
            query.setVehicleId(vehicleId);
            query.setQueryText(question);
            query.setQueryDate(LocalDateTime.now());

            queryDAO.insert(query);
            queryTextArea.clear();
            loadMyQueries();
            AlertHelper.showInfo("Your question has been submitted. An admin will respond later.");
        } catch (Exception e) {
            AlertHelper.showError("Failed to submit query: " + e.getMessage());
        }
    }
}