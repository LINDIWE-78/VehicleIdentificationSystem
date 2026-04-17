package com.vis.controllers;

import com.vis.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.sql.*;

public class StatisticsController {

    public VBox getView() {
        VBox vbox = new VBox(15);
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("Vehicle Statistics by Make");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Number of Vehicles per Make");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        String sql = "SELECT make, COUNT(*) as count FROM Vehicle GROUP BY make";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String make = rs.getString("make");
                int count = rs.getInt("count");
                pieData.add(new PieChart.Data(make + " (" + count + ")", count));
            }
        } catch (SQLException e) {
            pieData.add(new PieChart.Data("No data", 1));
        }
        pieChart.setData(pieData);
        pieChart.setPrefSize(500, 400);

        vbox.getChildren().addAll(header, pieChart);
        return vbox;
    }
}