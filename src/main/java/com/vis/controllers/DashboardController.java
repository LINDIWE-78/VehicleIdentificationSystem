package com.vis.controllers;

import com.vis.db.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.sql.*;

public class DashboardController {

    public VBox getView() {
        VBox vbox = new VBox(20);
        vbox.setStyle("-fx-padding: 20; -fx-background-color: rgba(0,0,0,0.6);");
        vbox.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("System Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4a9eff;");

        GridPane cards = new GridPane();
        cards.setHgap(20);
        cards.setVgap(20);
        cards.setAlignment(Pos.CENTER);

        int totalVehicles = getCount("SELECT COUNT(*) FROM Vehicle");
        int totalCustomers = getCount("SELECT COUNT(*) FROM Customer");
        int openViolations = getCount("SELECT COUNT(*) FROM Violation WHERE status = 'Unpaid'");
        int unansweredQueries = getCount("SELECT COUNT(*) FROM CustomerQuery WHERE response_text IS NULL");
        int pendingClaims = getCount("SELECT COUNT(*) FROM Claim WHERE status = 'Pending'");
        int activeUsers = getCount("SELECT COUNT(*) FROM AppUser WHERE is_active = true");

        cards.add(createCard("Total Vehicles", String.valueOf(totalVehicles)), 0, 0);
        cards.add(createCard("Total Customers", String.valueOf(totalCustomers)), 1, 0);
        cards.add(createCard("Open Violations", String.valueOf(openViolations)), 2, 0);
        cards.add(createCard("Unanswered Queries", String.valueOf(unansweredQueries)), 0, 1);
        cards.add(createCard("Pending Claims", String.valueOf(pendingClaims)), 1, 1);
        cards.add(createCard("Active Users", String.valueOf(activeUsers)), 2, 1);

        vbox.getChildren().addAll(title, cards);
        return vbox;
    }

    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private VBox createCard(String labelText, String value) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: rgba(30,42,54,0.9); -fx-background-radius: 10; -fx-padding: 15;");
        card.setPrefWidth(180);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        card.getChildren().addAll(label, valueLabel);
        return card;
    }
}