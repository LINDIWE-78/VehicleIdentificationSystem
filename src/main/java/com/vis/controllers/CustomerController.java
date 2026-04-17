package com.vis.controllers;

import com.vis.dao.CustomerDAO;
import com.vis.models.Customer;
import com.vis.utils.AlertHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class CustomerController extends BaseController<Customer> {
    private CustomerDAO customerDAO = new CustomerDAO();

    public CustomerController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        refreshData();
    }

    private void setupTableView() {
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
        tableView.getColumns().addAll(idCol, nameCol, addressCol, phoneCol, emailCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    protected List<Customer> fetchData() throws Exception {
        return customerDAO.getAll();
    }

    @Override
    public void addEntity() { AlertHelper.showInfo("Add customer - implement if needed"); }
    @Override
    public void updateEntity() { AlertHelper.showInfo("Update customer - implement if needed"); }
    @Override
    public void deleteEntity() { AlertHelper.showInfo("Delete customer - implement if needed"); }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");
        Label header = new Label("Customer - Customer Records");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setTooltip(new Tooltip("Refresh customer list"));
        refreshBtn.setOnAction(e -> refreshData());
        vbox.getChildren().addAll(header, tableView, progressIndicator, refreshBtn);
        return vbox;
    }
}