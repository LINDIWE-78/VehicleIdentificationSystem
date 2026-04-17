package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.Vehicle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO extends BaseDAO<Vehicle> {

    @Override
    public void insert(Vehicle v) throws SQLException {
        String sql = "INSERT INTO Vehicle (registration_number, make, model, year, owner_id) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getRegNumber());
            ps.setString(2, v.getMake());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getYear());
            ps.setInt(5, v.getOwnerId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) v.setVehicleId(rs.getInt(1));
        }
    }

    @Override
    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE Vehicle SET registration_number=?, make=?, model=?, year=?, owner_id=? WHERE vehicle_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, v.getRegNumber());
            ps.setString(2, v.getMake());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getYear());
            ps.setInt(5, v.getOwnerId());
            ps.setInt(6, v.getVehicleId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Vehicle WHERE vehicle_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Vehicle getById(int id) throws SQLException {
        String sql = "SELECT * FROM Vehicle WHERE vehicle_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractVehicle(rs);
            return null;
        }
    }

    @Override
    public List<Vehicle> getAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM Vehicle";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractVehicle(rs));
        }
        return list;
    }

    public List<Vehicle> getAllWithOwner() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM v_vehicle_details ORDER BY vehicle_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Vehicle v = new Vehicle();
                v.setVehicleId(rs.getInt("vehicle_id"));
                v.setRegNumber(rs.getString("registration_number"));
                v.setMake(rs.getString("make"));
                v.setModel(rs.getString("model"));
                v.setYear(rs.getInt("year"));
                v.setOwnerId(rs.getInt("customer_id"));
                v.setOwnerName(rs.getString("owner_name"));
                list.add(v);
            }
        }
        return list;
    }

    private Vehicle extractVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setRegNumber(rs.getString("registration_number"));
        v.setMake(rs.getString("make"));
        v.setModel(rs.getString("model"));
        v.setYear(rs.getInt("year"));
        v.setOwnerId(rs.getInt("owner_id"));
        return v;
    }
}