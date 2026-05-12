package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.PoliceReport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceReportDAO {

    public void insert(PoliceReport report) throws SQLException {
        String sql = "INSERT INTO PoliceReport (vehicle_id, report_date, report_type, description, officer_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getVehicleId());
            ps.setDate(2, Date.valueOf(report.getReportDate()));
            ps.setString(3, report.getReportType());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getOfficerName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) report.setReportId(rs.getInt(1));
        }
    }

    public List<PoliceReport> getAll() throws SQLException {
        List<PoliceReport> list = new ArrayList<>();
        String sql = "SELECT * FROM PoliceReport ORDER BY report_date DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractReport(rs));
            }
        }
        return list;
    }

    public void delete(int reportId) throws SQLException {
        String sql = "DELETE FROM PoliceReport WHERE report_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, reportId);
            ps.executeUpdate();
        }
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