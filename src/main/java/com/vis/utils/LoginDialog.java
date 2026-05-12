package com.vis.utils;

import com.vis.MainApp;
import com.vis.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LoginDialog {

    private static String currentUser = null;
    private static String currentRole = null;

    private static final Map<String, String> userPasswordMap =
            new HashMap<>();

    public static void authenticateAndShowMain() {

        Stage loginStage = new Stage();

        loginStage.setTitle(
                "Login - Vehicle Identification System"
        );

        // ================= ROOT =================
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        // ================= BACKGROUND IMAGE =================
        try {

            Image bgImage = new Image(
                    LoginDialog.class.getResourceAsStream(
                            "/com/vis/vehicleidentificationsystem/mainscene.png.png"
                    )
            );

            BackgroundSize backgroundSize = new BackgroundSize(
                    100,
                    100,
                    true,
                    true,
                    true,
                    true
            );

            BackgroundImage backgroundImage =
                    new BackgroundImage(
                            bgImage,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            backgroundSize
                    );

            root.setBackground(
                    new Background(backgroundImage)
            );

        } catch (Exception e) {

            root.setStyle(
                    "-fx-background-color: black;"
            );

            System.out.println(
                    "Background image failed: "
                            + e.getMessage()
            );
        }

        // ================= LOGIN CARD =================
        VBox card = new VBox(18);

        card.setAlignment(Pos.CENTER);

        // PROFESSIONAL SPACING
        card.setPadding(new Insets(35));

        // INCREASED SIZE
        card.setPrefWidth(430);
        card.setMaxWidth(430);

        card.setPrefHeight(360);
        card.setMaxHeight(360);

        // GLASS EFFECT STYLE
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-color: rgba(255,255,255,0.30);" +
                        "-fx-border-width: 1.2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.55), 25,0,0,6);"
        );

        //  TITLE
        Label title = new Label("Vehicle Login");

        title.setStyle(
                "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label subTitle = new Label("Secure Access");

        subTitle.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: rgba(255,255,255,0.90);"
        );


        GridPane form = new GridPane();

        form.setAlignment(Pos.CENTER);

        form.setHgap(15);
        form.setVgap(20);


        Label userLabel = new Label("Username:");

        userLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-text-fill: white;"
        );

        // USER COMBOBOX
        ComboBox<String> userCombo = new ComboBox<>();

        userCombo.setPrefWidth(240);
        userCombo.setPrefHeight(38);

        userCombo.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-font-size: 13px;"
        );

        //  PASSWORD LABEL
        Label passLabel = new Label("Password:");

        passLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-font-size: 15px;" +
                        "-fx-text-fill: white;"
        );

        //  PASSWORD FIELD
        PasswordField passwordField =
                new PasswordField();

        passwordField.setPrefWidth(240);
        passwordField.setPrefHeight(38);

        passwordField.setStyle(
                "-fx-background-radius: 10;" +
                        "-fx-font-size: 13px;"
        );


        form.add(userLabel, 0, 0);
        form.add(userCombo, 1, 0);

        form.add(passLabel, 0, 1);
        form.add(passwordField, 1, 1);


        loadUsersIntoComboBox(
                userCombo,
                passwordField
        );


        Label messageLabel = new Label();

        messageLabel.setStyle(
                "-fx-text-fill: #ff6b6b;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );


        Button loginBtn = new Button("LOGIN");

        loginBtn.setPrefWidth(120);
        loginBtn.setPrefHeight(42);

        loginBtn.setStyle(
                "-fx-background-color: #28a745;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );


        Button cancelBtn = new Button("EXIT");

        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(42);

        cancelBtn.setStyle(
                "-fx-background-color: #dc3545;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );


        HBox buttonBox =
                new HBox(20, loginBtn, cancelBtn);

        buttonBox.setAlignment(Pos.CENTER);


        card.getChildren().addAll(
                title,
                subTitle,
                form,
                messageLabel,
                buttonBox
        );

        root.getChildren().add(card);


        loginBtn.setOnAction(e -> {

            String selected = userCombo.getValue();

            String username =
                    (selected != null)
                            ? selected.split(" ")[0]
                            : "";

            String password =
                    passwordField.getText().trim();

            if (username.isEmpty()
                    || password.isEmpty()) {

                messageLabel.setText(
                        "Enter username and password"
                );

                return;
            }

            String sql =
                    "SELECT username, role " +
                            "FROM AppUser " +
                            "WHERE username = ? " +
                            "AND password = ? " +
                            "AND is_active = true";

            try (
                    Connection conn =
                            DBConnection.getConnection();

                    PreparedStatement ps =
                            conn.prepareStatement(sql)
            ) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    currentUser =
                            rs.getString("username");

                    currentRole =
                            rs.getString("role");

                    loginStage.close();

                    MainApp.showMainWindow();

                } else {

                    messageLabel.setText(
                            "Invalid username or password"
                    );
                }

            } catch (Exception ex) {

                messageLabel.setText(
                        "Database error"
                );

                ex.printStackTrace();
            }
        });

        // ================= EXIT BUTTON ACTION =================
        cancelBtn.setOnAction(
                e -> System.exit(0)
        );

        // ================= SCENE =================
        Scene scene =
                new Scene(root, 1920, 1080);

        loginStage.setScene(scene);

        loginStage.setMaximized(true);

        loginStage.showAndWait();
    }

    // ================= LOAD USERS =================
    private static void loadUsersIntoComboBox(
            ComboBox<String> comboBox,
            PasswordField passwordField
    ) {

        ObservableList<String> userList =
                FXCollections.observableArrayList();

        String sql =
                "SELECT username, password " +
                        "FROM AppUser " +
                        "WHERE is_active = true";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                String username =
                        rs.getString("username");

                String password =
                        rs.getString("password");

                String display =
                        username +
                                " (" +
                                password +
                                ")";

                userList.add(display);

                userPasswordMap.put(
                        display,
                        password
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            userList.add(
                    "Error loading users"
            );
        }

        comboBox.setItems(userList);

        if (!userList.isEmpty()) {

            comboBox
                    .getSelectionModel()
                    .selectFirst();

            String first = userList.get(0);

            passwordField.setText(
                    userPasswordMap.get(first)
            );
        }

        comboBox.setOnAction(e -> {

            String selected =
                    comboBox.getValue();

            if (selected != null) {

                String pwd =
                        userPasswordMap.get(selected);

                if (pwd != null) {

                    passwordField.setText(pwd);
                }
            }
        });
    }

    // ================= USER INFO =================
    public static String getCurrentUser() {

        return currentUser != null
                ? currentUser
                : "Guest";
    }

    public static String getCurrentRole() {

        return currentRole != null
                ? currentRole
                : "UNKNOWN";
    }


    public static void logout() {

        currentUser = null;
        currentRole = null;
    }

    public static boolean isAuthenticated() {

        return currentUser != null;
    }
}