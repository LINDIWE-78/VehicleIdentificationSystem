package com.vis.controllers;

import com.vis.dao.AppUserDAO;
import com.vis.dao.CustomerQueryDAO;
import com.vis.models.AppUser;
import com.vis.models.CustomerQuery;
import com.vis.utils.AlertHelper;
import com.vis.utils.LoginDialog;
import com.vis.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import java.sql.*;
import java.util.List;

//inheritance
public class AdminController extends BaseController<AppUser> {
    private AppUserDAO userDAO = new AppUserDAO();
    private CustomerQueryDAO queryDAO = new CustomerQueryDAO();
    private TextField searchField;
    private ObservableList<AppUser> masterData = FXCollections.observableArrayList();
    private FilteredList<AppUser> filteredData;

    public AdminController() {
        super(new TableView<>(), new ProgressIndicator());
        setupTableView();
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
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
            { toggleBtn.setOnAction(e -> {
                AppUser user = getTableView().getItems().get(getIndex());
                user.setActive(!user.isActive());
                try { userDAO.update(user); refreshData(); AlertHelper.showInfo("User " + user.getUsername() + " active status changed to " + user.isActive()); }
                catch (Exception ex) { AlertHelper.showError("Update failed: " + ex.getMessage()); }
            }); }
            // Polymorphism
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
    public void refreshData() {
        progressIndicator.setVisible(true);
        new Thread(() -> {
            try {
                List<AppUser> list = fetchData();
                javafx.application.Platform.runLater(() -> {
                    masterData.setAll(list);
                    progressIndicator.setVisible(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    AlertHelper.showError("Failed to load data: " + e.getMessage());
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    @Override
    public void addEntity() {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("Only ADMIN can add users.");
            return;
        }
        Dialog<AppUser> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter user details");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ADMIN", "POLICE", "WORKSHOP", "CUSTOMER", "INSURANCE");
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
            try { userDAO.insert(u); refreshData(); AlertHelper.showInfo("User added"); }
            catch (Exception e) { AlertHelper.showError("Add failed: " + e.getMessage()); }
        });
    }

    @Override
    public void updateEntity() {
        AlertHelper.showInfo("Use Toggle Active button to update user status");
    }

    @Override
    public void deleteEntity() {
        if (!"ADMIN".equals(LoginDialog.getCurrentRole())) {
            AlertHelper.showError("Only ADMIN can delete users.");
            return;
        }
        AppUser selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertHelper.showError("No user selected"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user " + selected.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try { userDAO.delete(selected.getUserId()); refreshData(); AlertHelper.showInfo("User deleted"); }
            catch (Exception e) { AlertHelper.showError("Delete failed: " + e.getMessage()); }
        }
    }

    // ---------- Respond to Customer Queries (fully implemented) ----------
    private void respondToQuery() {
        try {
            List<CustomerQuery> unanswered = queryDAO.findUnanswered();
            if (unanswered.isEmpty()) {
                AlertHelper.showInfo("No unanswered queries.");
                return;
            }
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Respond to Customer Query");
            dialog.setHeaderText("Select a query and type your response");
            ButtonType sendBtn = new ButtonType("Send Response", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(sendBtn, ButtonType.CANCEL);

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));

            ComboBox<CustomerQuery> queryCombo = new ComboBox<>();
            queryCombo.getItems().addAll(unanswered);
            queryCombo.setCellFactory(lv -> new ListCell<CustomerQuery>() {
                @Override
                protected void updateItem(CustomerQuery q, boolean empty) {
                    super.updateItem(q, empty);
                    setText(empty ? null : "Query ID " + q.getQueryId() + " - " + q.getQueryText());
                }
            });
            queryCombo.setButtonCell(queryCombo.getCellFactory().call(null));
            queryCombo.setPromptText("Choose a query");

            TextArea responseArea = new TextArea();
            responseArea.setPromptText("Type your response here...");
            responseArea.setPrefRowCount(4);

            vbox.getChildren().addAll(new Label("Customer Question:"), queryCombo,
                    new Label("Your Response:"), responseArea);
            dialog.getDialogPane().setContent(vbox);

            dialog.setResultConverter(button -> {
                if (button == sendBtn) {
                    CustomerQuery selected = queryCombo.getValue();
                    if (selected == null) {
                        AlertHelper.showError("Select a query.");
                        return null;
                    }
                    String response = responseArea.getText().trim();
                    if (response.isEmpty()) {
                        AlertHelper.showError("Enter a response.");
                        return null;
                    }
                    try {
                        queryDAO.updateResponse(selected.getQueryId(), response);
                        AlertHelper.showInfo("Response sent.");
                    } catch (Exception e) {
                        AlertHelper.showError("Database error: " + e.getMessage());
                    }
                }
                return null;
            });
            dialog.showAndWait();
        } catch (Exception e) {
            AlertHelper.showError("Failed to load queries: " + e.getMessage());
        }
    }

    // ---------- View all customers (joined table) ----------
    private void showAllCustomersDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("All Customers (Detailed View)");
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 10; -fx-background-color: #2c3e50;");

        TableView<Object[]> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setRowFactory(tv -> {
            TableRow<Object[]> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        TableColumn<Object[], Integer> custIdCol = new TableColumn<>("Customer ID");
        custIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty((Integer) c.getValue()[0]).asObject());
        TableColumn<Object[], String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[1]));
        TableColumn<Object[], String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[2]));
        TableColumn<Object[], String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[3]));
        TableColumn<Object[], String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[4]));
        TableColumn<Object[], String> usernameCol = new TableColumn<>("Username (Login)");
        usernameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[5]));
        TableColumn<Object[], String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty((String) c.getValue()[6]));
        TableColumn<Object[], Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty((Boolean) c.getValue()[7]));

        tableView.getColumns().addAll(custIdCol, nameCol, addressCol, phoneCol, emailCol, usernameCol, roleCol, activeCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String sql = "SELECT c.customer_id, c.name, c.address, c.phone, c.email, " +
                "au.username, au.role, au.is_active " +
                "FROM Customer c " +
                "LEFT JOIN AppUser au ON c.email = au.username " +
                "WHERE au.role = 'CUSTOMER' OR au.role IS NULL " +
                "ORDER BY c.customer_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            javafx.collections.ObservableList<Object[]> data = javafx.collections.FXCollections.observableArrayList();
            while (rs.next()) {
                Object[] row = new Object[8];
                row[0] = rs.getInt("customer_id");
                row[1] = rs.getString("name");
                row[2] = rs.getString("address");
                row[3] = rs.getString("phone");
                row[4] = rs.getString("email");
                row[5] = rs.getString("username");
                row[6] = rs.getString("role");
                row[7] = rs.getBoolean("is_active");
                data.add(row);
            }
            tableView.setItems(data);
            if (data.isEmpty()) tableView.setPlaceholder(new Label("No customers found"));
        } catch (SQLException e) {
            AlertHelper.showError("Failed to load customer data: " + e.getMessage());
            e.printStackTrace();
        }

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> dialogStage.close());
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.getChildren().add(closeBtn);
        root.setCenter(tableView);
        root.setBottom(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(10));

        Scene scene = new Scene(root, 900, 500);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    public VBox getView() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 10; -fx-background-color: rgba(0,0,0,0.6);");

        Label header = new Label("Admin - User Access Management");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4a9eff;");

        searchField = new TextField();
        searchField.setPromptText("🔍 Search by username or role...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String text = newVal == null ? "" : newVal.trim().toLowerCase();
            if (text.isEmpty()) {
                filteredData.setPredicate(p -> true);
            } else {
                filteredData.setPredicate(user ->
                        user.getUsername().toLowerCase().contains(text) ||
                                user.getRole().toLowerCase().contains(text)
                );
            }
        });

        tableView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        tableView.setRowFactory(tv -> {
            TableRow<AppUser> row = new TableRow<>();
            row.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(58,90,122,0.8);"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(0,0,0,0.6);"));
            return row;
        });

        Button btnAdd = new Button("Add User");
        Button btnDelete = new Button("Delete User");
        Button btnRefresh = new Button("Refresh");
        Button btnRespond = new Button("Respond to Customer Queries");
        Button btnViewCustomers = new Button("View All Customers");
        btnViewCustomers.setStyle("-fx-background-color: #008CBA;");

        btnAdd.setOnAction(e -> addEntity());
        btnDelete.setOnAction(e -> deleteEntity());
        btnRefresh.setOnAction(e -> {
            searchField.clear();
            refreshData();
        });
        btnRespond.setOnAction(e -> respondToQuery());
        btnViewCustomers.setOnAction(e -> showAllCustomersDialog());

        HBox row1 = new HBox(10);
        row1.getChildren().addAll(btnAdd, btnDelete, btnRefresh);
        HBox row2 = new HBox(10);
        row2.getChildren().addAll(btnRespond, btnViewCustomers);

        vbox.getChildren().addAll(header, searchField, tableView, progressIndicator, row1, row2);
        return vbox;
    }
}