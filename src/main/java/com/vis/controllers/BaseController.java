package com.vis.controllers;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import java.util.List;

/**
 * Generic controller with a TableView and a ProgressIndicator.
 * Provides a refreshData() method that shows the indicator while loading data in a background thread.
 * Subclasses must implement fetchData(), addEntity(), updateEntity(), deleteEntity().
 * This demonstrates inheritance and polymorphism (required).
 */
public abstract class BaseController<T> {
    protected TableView<T> tableView;
    protected ProgressIndicator progressIndicator;

    public BaseController(TableView<T> tableView, ProgressIndicator progressIndicator) {
        this.tableView = tableView;
        this.progressIndicator = progressIndicator;
    }

    /**
     * Refreshes the table by fetching data from the database.
     * Shows the ProgressIndicator while loading (background thread).
     */
    public void refreshData() {
        progressIndicator.setVisible(true);
        Task<List<T>> task = new Task<>() {
            @Override
            protected List<T> call() throws Exception {
                return fetchData();
            }
        };
        task.setOnSucceeded(e -> {
            tableView.getItems().setAll(task.getValue());
            progressIndicator.setVisible(false);
        });
        task.setOnFailed(e -> progressIndicator.setVisible(false));
        new Thread(task).start();
    }

    protected abstract List<T> fetchData() throws Exception;
    public abstract void addEntity();
    public abstract void updateEntity();
    public abstract void deleteEntity();
}