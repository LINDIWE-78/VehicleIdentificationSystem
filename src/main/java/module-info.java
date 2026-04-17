module com.vis.vehicleidentificationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.vis.vehicleidentificationsystem to javafx.fxml;
    exports com.vis.vehicleidentificationsystem;
}