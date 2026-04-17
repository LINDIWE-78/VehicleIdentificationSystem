package com.vis.controllers;

import com.vis.dao.InsurancePolicyDAO;
import com.vis.models.InsurancePolicy;
import com.vis.utils.AlertHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;

public class InsuranceController extends BaseController<InsurancePolicy> {
    private InsurancePolicyDAO policyDAO = new InsurancePolicyDAO();

    public InsuranceController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        refreshData();
    }

    private void setupTableView() {
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

    @Override
    protected List<InsurancePolicy> fetchData() throws Exception {
        return policyDAO.getAll();
    }

    @Override
    public void addEntity() { AlertHelper.showInfo("Add policy - implement if needed"); }
    @Override
    public void updateEntity() { AlertHelper.showInfo("Update policy - implement if needed"); }
    @Override
    public void deleteEntity() { AlertHelper.showInfo("Delete policy - implement if needed"); }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");
        Label header = new Label("Insurance - Policy Management");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setTooltip(new Tooltip("Refresh insurance policies"));
        refreshBtn.setOnAction(e -> refreshData());
        vbox.getChildren().addAll(header, tableView, progressIndicator, refreshBtn);
        return vbox;
    }
}