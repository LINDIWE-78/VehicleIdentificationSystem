
package com.vis.controllers;

import com.vis.dao.InsurancePolicyDAO;
import com.vis.dao.ClaimDAO;
import com.vis.models.InsurancePolicy;
import com.vis.models.Claim;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.util.List;

public class InsuranceController {

    private InsurancePolicyDAO policyDAO = new InsurancePolicyDAO();
    private ClaimDAO claimDAO = new ClaimDAO();

    public VBox getView() {
        VBox mainBox = new VBox(10);
        mainBox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.6);");
        TabPane tabPane = new TabPane();
        Tab policiesTab = new Tab("Insurance Policies", createPoliciesView());
        policiesTab.setClosable(false);
        Tab claimsTab = new Tab("Claims", createClaimsView());
        claimsTab.setClosable(false);
        tabPane.getTabs().addAll(policiesTab, claimsTab);
        mainBox.getChildren().add(tabPane);
        return mainBox;
    }

    private VBox createPoliciesView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<InsurancePolicy> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setPlaceholder(new Label("No data"));

        // Make rows semi‑transparent dark so background image shows through
        tableView.setRowFactory(tv -> {
            TableRow<InsurancePolicy> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupPolicyTable(tableView);
        refreshPolicies(tableView);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by company or policy #...");
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty()) refreshPolicies(tableView);
            else tableView.setItems(tableView.getItems().filtered(p ->
                    p.getInsuranceCompany().toLowerCase().contains(newVal.toLowerCase()) ||
                            p.getPolicyNumber().toLowerCase().contains(newVal.toLowerCase())));
        });

        Button btnAdd = new Button("Add Policy");
        Button btnUpdate = new Button("Update Policy");
        Button btnDelete = new Button("Delete Policy");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addPolicy(tableView));
        btnUpdate.setOnAction(e -> updatePolicy(tableView));
        btnDelete.setOnAction(e -> deletePolicy(tableView));
        btnRefresh.setOnAction(e -> { refreshPolicies(tableView); searchField.clear(); });

        String role = LoginDialog.getCurrentRole();
        if (!"ADMIN".equals(role) && !"INSURANCE".equals(role)) {
            btnAdd.setDisable(true);
            btnUpdate.setDisable(true);
            btnDelete.setDisable(true);
        }

        // ---------- HORIZONTAL BUTTON BOX ----------
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);
        vbox.getChildren().addAll(searchField, tableView, buttonBox);
        return vbox;
    }

    private VBox createClaimsView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: transparent;");

        TableView<Claim> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setPlaceholder(new Label("No claims"));

        // Row transparency for claims table
        tableView.setRowFactory(tv -> {
            TableRow<Claim> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        setupClaimTable(tableView);
        refreshClaims(tableView);

        Button btnAdd = new Button("Add Claim");
        Button btnUpdate = new Button("Update Claim Status");
        Button btnDelete = new Button("Delete Claim");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setOnAction(e -> addClaim(tableView));
        btnUpdate.setOnAction(e -> updateClaim(tableView));
        btnDelete.setOnAction(e -> deleteClaim(tableView));
        btnRefresh.setOnAction(e -> refreshClaims(tableView));

        //  HORIZONTAL BUTTON BOX
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnRefresh);
        vbox.getChildren().addAll(tableView, buttonBox);
        return vbox;
    }

    // ---------- Policy methods ----------
    private void setupPolicyTable(TableView<InsurancePolicy> tableView) {
        TableColumn<InsurancePolicy, Integer> idCol = new TableColumn<>("Policy ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPolicyId()).asObject());
        TableColumn<InsurancePolicy, Integer> vehicleCol = new TableColumn<>("Vehicle ID");
        vehicleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVehicleId()).asObject());
        TableColumn<InsurancePolicy, String> companyCol = new TableColumn<>("Company");
        companyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getInsuranceCompany()));
        TableColumn<InsurancePolicy, String> policyNumCol = new TableColumn<>("Policy #");
        policyNumCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPolicyNumber()));
        TableColumn<InsurancePolicy, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getStartDate()));
        TableColumn<InsurancePolicy, LocalDate> endCol = new TableColumn<>("End Date");
        endCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getEndDate()));
        tableView.getColumns().addAll(idCol, vehicleCol, companyCol, policyNumCol, startCol, endCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshPolicies(TableView<InsurancePolicy> tableView) {
        try { tableView.getItems().setAll(policyDAO.getAll()); }
        catch (Exception e) { AlertHelper.showError("Failed to load policies: " + e.getMessage()); }
    }

    private void addPolicy(TableView<InsurancePolicy> tableView) {
        Dialog<InsurancePolicy> dialog = new Dialog<>();
        dialog.setTitle("Add Insurance Policy");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField vehicleIdField = new TextField(); vehicleIdField.setPromptText("Vehicle ID");
        TextField companyField = new TextField(); companyField.setPromptText("Insurance Company");
        TextField policyNumField = new TextField(); policyNumField.setPromptText("Policy Number");
        DatePicker startDate = new DatePicker(LocalDate.now());
        DatePicker endDate = new DatePicker(LocalDate.now().plusYears(1));
        TextArea coverageArea = new TextArea(); coverageArea.setPromptText("Coverage Details");
        grid.add(new Label("Vehicle ID:"), 0, 0); grid.add(vehicleIdField, 1, 0);
        grid.add(new Label("Company:"), 0, 1); grid.add(companyField, 1, 1);
        grid.add(new Label("Policy #:"), 0, 2); grid.add(policyNumField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3); grid.add(startDate, 1, 3);
        grid.add(new Label("End Date:"), 0, 4); grid.add(endDate, 1, 4);
        grid.add(new Label("Coverage:"), 0, 5); grid.add(coverageArea, 1, 5);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == addBtn) {
                try {
                    InsurancePolicy p = new InsurancePolicy();
                    p.setVehicleId(Integer.parseInt(vehicleIdField.getText()));
                    p.setInsuranceCompany(companyField.getText());
                    p.setPolicyNumber(policyNumField.getText());
                    p.setStartDate(startDate.getValue());
                    p.setEndDate(endDate.getValue());
                    p.setCoverageDetails(coverageArea.getText());
                    return p;
                } catch (NumberFormatException e) { AlertHelper.showError("Invalid Vehicle ID"); return null; }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(p -> {
            try { policyDAO.insert(p); refreshPolicies(tableView); AlertHelper.showInfo("Policy added"); }
            catch (Exception e) { AlertHelper.showError("Add failed: " + e.getMessage()); }
        });
    }

    private void updatePolicy(TableView<InsurancePolicy> tableView) {
        InsurancePolicy selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("Select a policy to update."); return; }
        Dialog<InsurancePolicy> dialog = new Dialog<>();
        dialog.setTitle("Update Insurance Policy");
        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField vehicleIdField = new TextField(String.valueOf(selected.getVehicleId()));
        TextField companyField = new TextField(selected.getInsuranceCompany());
        TextField policyNumField = new TextField(selected.getPolicyNumber());
        DatePicker startDate = new DatePicker(selected.getStartDate());
        DatePicker endDate = new DatePicker(selected.getEndDate());
        TextArea coverageArea = new TextArea(selected.getCoverageDetails());
        grid.add(new Label("Vehicle ID:"), 0, 0); grid.add(vehicleIdField, 1, 0);
        grid.add(new Label("Company:"), 0, 1); grid.add(companyField, 1, 1);
        grid.add(new Label("Policy #:"), 0, 2); grid.add(policyNumField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3); grid.add(startDate, 1, 3);
        grid.add(new Label("End Date:"), 0, 4); grid.add(endDate, 1, 4);
        grid.add(new Label("Coverage:"), 0, 5); grid.add(coverageArea, 1, 5);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> (button == updateBtn) ? selected : null);
        dialog.showAndWait().ifPresent(p -> {
            p.setVehicleId(Integer.parseInt(vehicleIdField.getText()));
            p.setInsuranceCompany(companyField.getText());
            p.setPolicyNumber(policyNumField.getText());
            p.setStartDate(startDate.getValue());
            p.setEndDate(endDate.getValue());
            p.setCoverageDetails(coverageArea.getText());
            try { policyDAO.update(p); refreshPolicies(tableView); AlertHelper.showInfo("Policy updated"); }
            catch (Exception e) { AlertHelper.showError("Update failed: " + e.getMessage()); }
        });
    }

    private void deletePolicy(TableView<InsurancePolicy> tableView) {
        InsurancePolicy selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("Select a policy to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete policy #" + selected.getPolicyNumber() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { policyDAO.delete(selected.getPolicyId()); refreshPolicies(tableView); AlertHelper.showInfo("Policy deleted"); }
            catch (Exception e) { AlertHelper.showError("Delete failed: " + e.getMessage()); }
        }
    }

    //  Claim methods
    private void setupClaimTable(TableView<Claim> tableView) {
        TableColumn<Claim, Integer> idCol = new TableColumn<>("Claim ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getClaimId()).asObject());
        TableColumn<Claim, Integer> policyCol = new TableColumn<>("Policy ID");
        policyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPolicyId()).asObject());
        TableColumn<Claim, LocalDate> dateCol = new TableColumn<>("Claim Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClaimDate()));
        TableColumn<Claim, Number> amountCol = new TableColumn<>("Claim Amount");
        amountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getClaimAmount()));
        TableColumn<Claim, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        tableView.getColumns().addAll(idCol, policyCol, dateCol, amountCol, statusCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshClaims(TableView<Claim> tableView) {
        try {
            tableView.getItems().setAll(claimDAO.getAll());
        }
        catch (Exception e) { AlertHelper.showError("Failed to load claims: " + e.getMessage()); }
    }

    private void addClaim(TableView<Claim> tableView) {
        Dialog<Claim> dialog = new Dialog<>();
        dialog.setTitle("Add Claim");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField policyIdField = new TextField(); policyIdField.setPromptText("Policy ID");
        DatePicker claimDate = new DatePicker(LocalDate.now());
        TextField amountField = new TextField(); amountField.setPromptText("Claim Amount");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Pending", "Approved", "Rejected");
        statusCombo.setValue("Pending");
        grid.add(new Label("Policy ID:"), 0, 0); grid.add(policyIdField, 1, 0);
        grid.add(new Label("Claim Date:"), 0, 1); grid.add(claimDate, 1, 1);
        grid.add(new Label("Amount:"), 0, 2); grid.add(amountField, 1, 2);
        grid.add(new Label("Status:"), 0, 3); grid.add(statusCombo, 1, 3);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == addBtn) {
                try {
                    Claim c = new Claim();
                    c.setPolicyId(Integer.parseInt(policyIdField.getText()));
                    c.setClaimDate(claimDate.getValue());
                    c.setClaimAmount(new java.math.BigDecimal(amountField.getText()));
                    c.setStatus(statusCombo.getValue());
                    return c;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid Policy ID or Amount");
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(c -> {
            try { claimDAO.insert(c); refreshClaims(tableView); AlertHelper.showInfo("Claim added"); }
            catch (Exception e) { AlertHelper.showError("Add failed: " + e.getMessage()); }
        });
    }

    private void updateClaim(TableView<Claim> tableView) {
        Claim selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("Select a claim to update."); return; }
        Dialog<Claim> dialog = new Dialog<>();
        dialog.setTitle("Update Claim Status");
        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Pending", "Approved", "Rejected");
        statusCombo.setValue(selected.getStatus());
        dialog.getDialogPane().setContent(statusCombo);
        dialog.setResultConverter(button -> (button == updateBtn) ? selected : null);
        dialog.showAndWait().ifPresent(c -> {
            c.setStatus(statusCombo.getValue());
            try { claimDAO.update(c); refreshClaims(tableView); AlertHelper.showInfo("Claim status updated"); }
            catch (Exception e) { AlertHelper.showError("Update failed: " + e.getMessage()); }
        });
    }

    private void deleteClaim(TableView<Claim> tableView) {
        Claim selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("Select a claim to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete claim #" + selected.getClaimId() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                claimDAO.delete(selected.getClaimId()); refreshClaims(tableView); AlertHelper.showInfo("Claim deleted");
            }
            catch (Exception e) { AlertHelper.showError("Delete failed: " + e.getMessage()); }
        }
    }
}