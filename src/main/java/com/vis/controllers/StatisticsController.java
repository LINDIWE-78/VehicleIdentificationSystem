package com.vis.controllers;

import com.vis.db.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.sql.*;

public class StatisticsController {

    private VBox container;
    private GridPane chartGrid;
    private PieChart pieChart;
    private BarChart<String, Number> barChart;
    private LineChart<String, Number> lineChart;
    private XYChart.Series<String, Number> barSeries;
    private XYChart.Series<String, Number> lineSeries;

    public VBox getView() {
        container = new VBox(15);
        container.setStyle("-fx-padding: 20; -fx-background-color: rgba(0,0,0,0.6); -fx-alignment: center;");

        Label header = new Label("Vehicle Statistics");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        // ---------- Pie Chart ----------
        pieChart = new PieChart();
        pieChart.setTitle("Vehicles per Make");
        pieChart.setLabelsVisible(true);
        pieChart.setPrefSize(400, 300);

        // ---------- Bar Chart ----------
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Year");
        yAxis.setLabel("Number of Vehicles");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Vehicles per Year");
        barChart.setPrefSize(400, 300);
        barSeries = new XYChart.Series<>();
        barSeries.setName("Vehicles");
        barChart.getData().add(barSeries);

        // ---------- Line Chart (light background, black text) ----------
        CategoryAxis monthAxis = new CategoryAxis();
        NumberAxis countAxis = new NumberAxis();
        monthAxis.setLabel("Month");
        countAxis.setLabel("Number of Violations");
        lineChart = new LineChart<>(monthAxis, countAxis);
        lineChart.setTitle("Violations per Month (last 12 months)");
        lineChart.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
        lineChart.setPrefSize(400, 300);
        lineSeries = new XYChart.Series<>();
        lineSeries.setName("Violations");
        lineChart.getData().add(lineSeries);

        // Arrange charts in a grid
        chartGrid = new GridPane();
        chartGrid.setHgap(20);
        chartGrid.setVgap(20);
        chartGrid.add(pieChart, 0, 0);
        chartGrid.add(barChart, 1, 0);
        chartGrid.add(lineChart, 0, 1, 2, 1);

        container.getChildren().addAll(header, chartGrid);

        // Load initial data
        refreshData();

        return container;
    }

    /**
     * Reloads all chart data from the database.
     * Call this method whenever you need to refresh the statistics.
     */
    public void refreshData() {
        Platform.runLater(() -> {
            // ---------- Pie chart data ----------
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            String sqlPie = "SELECT make, COUNT(*) as count FROM Vehicle GROUP BY make";
            try (Statement st = DBConnection.getConnection().createStatement();
                 ResultSet rs = st.executeQuery(sqlPie)) {
                while (rs.next()) {
                    String make = rs.getString("make");
                    int count = rs.getInt("count");
                    pieData.add(new PieChart.Data(make + " (" + count + ")", count));
                }
            } catch (SQLException e) {
                pieData.add(new PieChart.Data("No data", 1));
            }
            pieChart.setData(pieData);

            // Force pie chart labels to white – try repeatedly until successful
            forcePieLabelsWhite();

            // ---------- Bar chart data ----------
            barSeries.getData().clear();
            String sqlBar = "SELECT year, COUNT(*) as count FROM Vehicle WHERE year IS NOT NULL GROUP BY year ORDER BY year";
            try (Statement st = DBConnection.getConnection().createStatement();
                 ResultSet rs = st.executeQuery(sqlBar)) {
                while (rs.next()) {
                    int year = rs.getInt("year");
                    int count = rs.getInt("count");
                    barSeries.getData().add(new XYChart.Data<>(String.valueOf(year), count));
                }
            } catch (SQLException e) {
                barSeries.getData().add(new XYChart.Data<>("No data", 1));
            }
            // Bar chart styling (white text)
            barChart.lookupAll(".chart-title").forEach(n -> n.setStyle("-fx-text-fill: white;"));
            barChart.lookupAll(".axis-label").forEach(n -> n.setStyle("-fx-text-fill: white;"));
            barChart.lookupAll(".axis").forEach(n -> n.setStyle("-fx-tick-label-fill: white;"));
            barChart.lookupAll(".chart-legend-item").forEach(n -> n.setStyle("-fx-text-fill: white;"));

            // ---------- Line chart data (light background, black text) ----------
            lineSeries.getData().clear();
            String sqlLine = "SELECT TO_CHAR(violation_date, 'YYYY-MM') as month, COUNT(*) as count " +
                    "FROM Violation " +
                    "WHERE violation_date >= CURRENT_DATE - INTERVAL '12 months' " +
                    "GROUP BY month ORDER BY month";
            try (Statement st = DBConnection.getConnection().createStatement();
                 ResultSet rs = st.executeQuery(sqlLine)) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    int count = rs.getInt("count");
                    lineSeries.getData().add(new XYChart.Data<>(month, count));
                }
            } catch (SQLException e) {
                lineSeries.getData().add(new XYChart.Data<>("No data", 1));
            }
            // Line chart text styling (black on white background)
            lineChart.lookupAll(".chart-title").forEach(n -> n.setStyle("-fx-text-fill: black; -fx-font-weight: bold;"));
            lineChart.lookupAll(".axis-label").forEach(n -> n.setStyle("-fx-text-fill: black;"));
            lineChart.lookupAll(".axis").forEach(n -> n.setStyle("-fx-tick-label-fill: black;"));
            lineChart.lookupAll(".chart-legend-item").forEach(n -> n.setStyle("-fx-text-fill: black;"));
            Node line = lineChart.lookup(".chart-series-line");
            if (line != null) line.setStyle("-fx-stroke: black;");
        });
    }

    /**
     * Repeatedly attempts to set pie chart label colour to white until successful.
     * This solves the timing issue where labels are not yet rendered when we first try.
     */
    private void forcePieLabelsWhite() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(50), event -> {
                    // Check if pie chart labels exist
                    boolean allWhite = true;
                    for (Node n : pieChart.lookupAll(".chart-pie-label")) {
                        n.setStyle("-fx-fill: white;");
                        // If any label does not have white fill, keep trying
                        if (!n.getStyle().contains("white")) allWhite = false;
                    }
                    // Also set title white
                    pieChart.lookupAll(".chart-title").forEach(n -> n.setStyle("-fx-text-fill: white;"));
                    if (allWhite) {
                        // Stop once we have set white on all labels (we set them anyway)
                        // We'll just stop after a few tries
                    }
                })
        );
        timeline.setCycleCount(10); // try 10 times (500 ms)
        timeline.play();
    }
}