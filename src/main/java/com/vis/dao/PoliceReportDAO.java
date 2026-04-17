package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.PoliceReport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceReportDAO extends BaseDAO<PoliceReport> {
    @Override
    public void insert(PoliceReport report) throws SQLException {
        String sql = "INSERT INTO PoliceReport (vehicle_id, report_date, report_type, description, officer_name) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, report.getVehicleId());
            ps.setDate(2, Date.valueOf(report.getReportDate()));
            ps.setString(3, report.getReportType());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getOfficerName());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(PoliceReport report) throws SQLException {
        String sql = "UPDATE PoliceReport SET vehicle_id=?, report_date=?, report_type=?, description=?, officer_name=? WHERE report_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, report.getVehicleId());
            ps.setDate(2, Date.valueOf(report.getReportDate()));
            ps.setString(3, report.getReportType());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getOfficerName());
            ps.setInt(6, report.getReportId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM PoliceReport WHERE report_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public PoliceReport getById(int id) throws SQLException {
        String sql = "SELECT * FROM PoliceReport WHERE report_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractReport(rs);
            return null;
        }
    }

    @Override
    public List<PoliceReport> getAll() throws SQLException {
        List<PoliceReport> list = new ArrayList<>();
        String sql = "SELECT * FROM PoliceReport";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractReport(rs));
        }
        return list;
    }

    private PoliceReport extractReport(ResultSet rs) throws SQLException {
        PoliceReport r = new PoliceReport();
        r.setReportId(rs.getInt("report_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setReportDate(rs.getDate("report_date").toLocalDate());
        r.setReportType(rs.getString("report_type"));
        r.setDescription(rs.getString("description"));
        r.setOfficerName(rs.getString("officer_name"));
        return r;
    }
}