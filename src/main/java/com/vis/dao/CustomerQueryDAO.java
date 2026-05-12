package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.CustomerQuery;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerQueryDAO {

    public void insert(CustomerQuery q) throws SQLException {
        String sql = "INSERT INTO CustomerQuery (customer_id, vehicle_id, query_text, query_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getCustomerId());
            ps.setInt(2, q.getVehicleId());
            ps.setString(3, q.getQueryText());
            ps.setTimestamp(4, Timestamp.valueOf(q.getQueryDate()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) q.setQueryId(rs.getInt(1));
        }
    }

    public List<CustomerQuery> findByCustomerId(int customerId) throws SQLException {
        List<CustomerQuery> list = new ArrayList<>();
        String sql = "SELECT * FROM CustomerQuery WHERE customer_id = ? ORDER BY query_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extractQuery(rs));
        }
        return list;
    }

    public List<CustomerQuery> findUnanswered() throws SQLException {
        List<CustomerQuery> list = new ArrayList<>();
        String sql = "SELECT * FROM CustomerQuery WHERE response_text IS NULL ORDER BY query_date ASC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractQuery(rs));
        }
        return list;
    }

    public void updateResponse(int queryId, String response) throws SQLException {
        String sql = "UPDATE CustomerQuery SET response_text = ? WHERE query_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, response);
            ps.setInt(2, queryId);
            ps.executeUpdate();
        }
    }

    private CustomerQuery extractQuery(ResultSet rs) throws SQLException {
        CustomerQuery q = new CustomerQuery();
        q.setQueryId(rs.getInt("query_id"));
        q.setCustomerId(rs.getInt("customer_id"));
        q.setVehicleId(rs.getInt("vehicle_id"));
        Timestamp ts = rs.getTimestamp("query_date");
        if (ts != null) q.setQueryDate(ts.toLocalDateTime());
        q.setQueryText(rs.getString("query_text"));
        q.setResponseText(rs.getString("response_text"));
        return q;
    }
}