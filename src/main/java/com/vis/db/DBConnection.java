package com.vis.db;

import java.sql.*;
import java.io.InputStream;
import java.util.Properties;

public class DBConnection {
    private static Connection conn = null;

    public static Connection getConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("DatabaseConfig.properties")) {
            if (input == null) throw new SQLException("DatabaseConfig.properties not found");
            Properties props = new Properties();
            props.load(input);
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        } catch (Exception e) {
            throw new SQLException("Failed to load DB config", e);
        }
    }

    public static void close() {
        if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}