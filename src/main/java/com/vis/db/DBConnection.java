package com.vis.db;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Database connection handler.
 * Uses DatabaseConfig.properties for settings.
 * Provides both instance‑based methods (openConnection, closeConnection, transactions)
 * and a static getConnection() for backward compatibility with existing DAOs.
 */
public class DBConnection {

    private final String URL;
    private final String user;
    private final String pass;
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;

    // ---------- Static part (for existing DAOs) ----------
    private static DBConnection staticInstance = null;

    private static synchronized DBConnection getStaticInstance() {
        if (staticInstance == null) staticInstance = new DBConnection();
        return staticInstance;
    }

    /**
     * Static method used by DAOs: returns an open connection.
     */
    public static Connection getConnection() throws SQLException {
        DBConnection db = getStaticInstance();
        if (db.getConn() == null || db.getConn().isClosed()) {
            db.openConnection();
        }
        return db.getConn();
    }

    //  Instance part (following professional pattern)
    public DBConnection() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("DatabaseConfig.properties")) {
            if (input == null) {
                throw new RuntimeException("DatabaseConfig.properties not found in resources");
            }
            Properties props = new Properties();
            props.load(input);
            this.URL = props.getProperty("db.url");
            this.user = props.getProperty("db.user");
            this.pass = props.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration: " + e.getMessage(), e);
        }
    }

    public DBConnection(String url, String user, String pass) {
        this.URL = url;
        this.user = user;
        this.pass = pass;
    }

    public void openConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, user, pass);

        }
    }

    public void closeConnection() {
        try {
            if (rs != null && !rs.isClosed()) rs.close();
            if (pstmt != null && !pstmt.isClosed()) pstmt.close();
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            rs = null;
            pstmt = null;
            conn = null;
        }
    }

    public Connection getConn() { return conn; }

    public void createTable(String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating table: " + e.getMessage(), e);
        }
    }

    public void display(String sql) {
        try {
            this.rs = conn.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement getPstmt() { return pstmt; }

    public void setPstmt(String sql) {
        try {
            this.pstmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void executePstmt() {
        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet getRs() { return rs; }

    public void beginTransaction() throws SQLException {
        conn.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollbackTransaction() throws SQLException {
        conn.rollback();
        conn.setAutoCommit(true);
    }
}