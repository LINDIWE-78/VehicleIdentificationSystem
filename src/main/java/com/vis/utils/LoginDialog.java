package com.vis.utils;

import com.vis.vehicleidentificationsystem.MainApp;
import com.vis.db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginDialog {

    private static String currentUser = null;
    private static String currentRole = null;

    public static void authenticateAndShowMain() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login - Vehicle Identification System");
        loginStage.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label titleLabel = new Label("Vehicle Identification System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        GridPane.setConstraints(titleLabel, 0, 0, 2, 1);

        Label userLabel = new Label("Username:");
        GridPane.setConstraints(userLabel, 0, 1);
        TextField usernameField = new TextField();
        GridPane.setConstraints(usernameField, 1, 1);

        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 2);
        PasswordField passwordField = new PasswordField();
        GridPane.setConstraints(passwordField, 1, 2);

        Button loginBtn = new Button("Login");
        Button cancelBtn = new Button("Cancel");
        GridPane.setConstraints(loginBtn, 0, 3);
        GridPane.setConstraints(cancelBtn, 1, 3);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");
        GridPane.setConstraints(messageLabel, 0, 4, 2, 1);

        grid.getChildren().addAll(titleLabel, userLabel, usernameField, passLabel, passwordField, loginBtn, cancelBtn, messageLabel);

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password");
                return;
            }

            // Query matches your AppUser table columns
            String sql = "SELECT username, role FROM AppUser WHERE username = ? AND password = ? AND is_active = true";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentUser = rs.getString("username");
                    currentRole = rs.getString("role");
                    loginStage.close();
                    MainApp.showMainWindow();
                } else {
                    messageLabel.setText("Invalid username or password, or account inactive");
                }
            } catch (Exception ex) {
                messageLabel.setText("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelBtn.setOnAction(e -> System.exit(0));

        Scene scene = new Scene(grid, 400, 250);
        loginStage.setScene(scene);
        loginStage.showAndWait();
    }

    public static String getCurrentUser() {
        return currentUser != null ? currentUser : "Guest";
    }

    public static String getCurrentRole() {
        return currentRole != null ? currentRole : "UNKNOWN";
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }
}