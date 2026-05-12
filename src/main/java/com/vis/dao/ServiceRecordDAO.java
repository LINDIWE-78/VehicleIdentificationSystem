package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.ServiceRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRecordDAO {

    public void insert(ServiceRecord record) throws SQLException {
        String sql = "INSERT INTO ServiceRecord (vehicle_id, service_date, service_type, description, cost) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getVehicleId());
            ps.setDate(2, Date.valueOf(record.getServiceDate()));
            ps.setString(3, record.getServiceType());
            ps.setString(4, record.getDescription());
            ps.setBigDecimal(5, record.getCost());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) record.setServiceId(rs.getInt(1));
        }
    }

    public List<ServiceRecord> getAll() throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM ServiceRecord ORDER BY service_date DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractRecord(rs));
        }
        return list;
    }

    public List<ServiceRecord> findByVehicleId(int vehicleId) throws SQLException {
        List<ServiceRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM ServiceRecord WHERE vehicle_id = ? ORDER BY service_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extractRecord(rs));
        }
        return list;
    }

    public void delete(int serviceId) throws SQLException {
        String sql = "DELETE FROM ServiceRecord WHERE service_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.executeUpdate();
        }
    }

    private ServiceRecord extractRecord(ResultSet rs) throws SQLException {
        ServiceRecord r = new ServiceRecord();
        r.setServiceId(rs.getInt("service_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setServiceDate(rs.getDate("service_date").toLocalDate());
        r.setServiceType(rs.getString("service_type"));
        r.setDescription(rs.getString("description"));
        r.setCost(rs.getBigDecimal("cost"));
        return r;
    }
}