package com.vis.controllers;

import com.vis.dao.AppUserDAO;
import com.vis.models.AppUser;
import com.vis.utils.AlertHelper;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.List;

public class AdminController extends BaseController<AppUser> {
    private AppUserDAO userDAO = new AppUserDAO();

    public AdminController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        refreshData();
    }

    private void setupTableView() {
        TableColumn<AppUser, Integer> idCol = new TableColumn<>("User ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getUserId()).asObject());
        TableColumn<AppUser, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
        TableColumn<AppUser, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole()));
        TableColumn<AppUser, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActive()));
        TableColumn<AppUser, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button("Toggle Active");
            {
                toggleBtn.setOnAction(e -> {
                    AppUser user = getTableView().getItems().get(getIndex());
                    user.setActive(!user.isActive());
                    try {
                        userDAO.update(user);
                        refreshData();
                        AlertHelper.showInfo("User " + user.getUsername() + " active status changed to " + user.isActive());
                    } catch (Exception ex) {
                        AlertHelper.showError("Update failed: " + ex.getMessage());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(toggleBtn);
            }
        });
        tableView.getColumns().addAll(idCol, usernameCol, roleCol, activeCol, actionCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    protected List<AppUser> fetchData() throws Exception {
        return userDAO.getAll();
    }

    @Override
    public void addEntity() {
        Dialog<AppUser> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter user details");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        VBox vbox = new VBox(10);
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");
        ComboBox<String> roleBox = new ComboBox<>(); roleBox.getItems().addAll("ADMIN", "POLICE", "WORKSHOP", "CUSTOMER");
        roleBox.setValue("CUSTOMER");
        CheckBox activeCheck = new CheckBox("Active"); activeCheck.setSelected(true);
        vbox.getChildren().addAll(usernameField, passwordField, roleBox, activeCheck);
        dialog.getDialogPane().setContent(vbox);
        dialog.setResultConverter(button -> {
            if (button == addBtn) {
                AppUser u = new AppUser();
                u.setUsername(usernameField.getText());
                u.setPassword(passwordField.getText());
                u.setRole(roleBox.getValue());
                u.setActive(activeCheck.isSelected());
                return u;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(u -> {
            try {
                userDAO.insert(u);
                refreshData();
                AlertHelper.showInfo("User added");
            } catch (Exception e) {
                AlertHelper.showError("Add failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateEntity() {
        AlertHelper.showInfo("Use Toggle Active button to update user status");
    }

    @Override
    public void deleteEntity() {
        AppUser selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showError("No user selected");
            return;
        }
        try {
            userDAO.delete(selected.getUserId());
            refreshData();
            AlertHelper.showInfo("User deleted");
        } catch (Exception e) {
            AlertHelper.showError("Delete failed: " + e.getMessage());
        }
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10;");
        Label header = new Label("Admin - User Access Management");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Button btnAdd = new Button("Add User");
        Button btnDelete = new Button("Delete User");
        Button btnRefresh = new Button("Refresh");
        btnAdd.setTooltip(new Tooltip("Add a new system user"));
        btnDelete.setTooltip(new Tooltip("Delete selected user"));
        btnRefresh.setTooltip(new Tooltip("Refresh user list"));
        btnAdd.setOnAction(e -> addEntity());
        btnDelete.setOnAction(e -> deleteEntity());
        btnRefresh.setOnAction(e -> refreshData());
        vbox.getChildren().addAll(header, tableView, progressIndicator, btnAdd, btnDelete, btnRefresh);
        return vbox;
    }
}