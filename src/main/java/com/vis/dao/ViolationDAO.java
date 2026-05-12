package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.Violation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//inheritance
public class ViolationDAO extends BaseDAO<Violation> {

    //  CORRECTED: Use CALL statement for PostgreSQL procedure
    @Override
    public void insert(Violation v) throws SQLException {
        String sql = "CALL add_violation(?, ?, ?, ?)"; //procedure
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, v.getVehicleId());
            ps.setDate(2, Date.valueOf(v.getViolationDate()));
            ps.setString(3, v.getViolationType());
            ps.setBigDecimal(4, v.getFineAmount());
            ps.execute();
        }
    }

    @Override
    public void update(Violation v) throws SQLException {
        String sql = "UPDATE Violation SET violation_date=?, violation_type=?, fine_amount=?, status=? WHERE violation_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(v.getViolationDate()));
            ps.setString(2, v.getViolationType());
            ps.setBigDecimal(3, v.getFineAmount());
            ps.setString(4, v.getStatus());
            ps.setInt(5, v.getViolationId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Violation WHERE violation_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Violation getById(int id) throws SQLException {
        String sql = "SELECT * FROM Violation WHERE violation_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractViolation(rs);
            return null;
        }
    }

    @Override
    public List<Violation> getAll() throws SQLException {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT * FROM Violation ORDER BY violation_date DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractViolation(rs));
        }
        return list;
    }

    private Violation extractViolation(ResultSet rs) throws SQLException {
        Violation v = new Violation();
        v.setViolationId(rs.getInt("violation_id"));
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setViolationDate(rs.getDate("violation_date").toLocalDate());
        v.setViolationType(rs.getString("violation_type"));
        v.setFineAmount(rs.getBigDecimal("fine_amount"));
        v.setStatus(rs.getString("status"));
        return v;
    }
}