package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.InsurancePolicy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsurancePolicyDAO extends BaseDAO<InsurancePolicy> {
    @Override
    public void insert(InsurancePolicy policy) throws SQLException {
        String sql = "INSERT INTO InsurancePolicy (vehicle_id, insurance_company, policy_number, start_date, end_date, coverage_details) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, policy.getVehicleId());
            ps.setString(2, policy.getInsuranceCompany());
            ps.setString(3, policy.getPolicyNumber());
            ps.setDate(4, Date.valueOf(policy.getStartDate()));
            ps.setDate(5, Date.valueOf(policy.getEndDate()));
            ps.setString(6, policy.getCoverageDetails());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(InsurancePolicy policy) throws SQLException {
        String sql = "UPDATE InsurancePolicy SET vehicle_id=?, insurance_company=?, policy_number=?, start_date=?, end_date=?, coverage_details=? WHERE policy_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, policy.getVehicleId());
            ps.setString(2, policy.getInsuranceCompany());
            ps.setString(3, policy.getPolicyNumber());
            ps.setDate(4, Date.valueOf(policy.getStartDate()));
            ps.setDate(5, Date.valueOf(policy.getEndDate()));
            ps.setString(6, policy.getCoverageDetails());
            ps.setInt(7, policy.getPolicyId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM InsurancePolicy WHERE policy_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public InsurancePolicy getById(int id) throws SQLException {
        String sql = "SELECT * FROM InsurancePolicy WHERE policy_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractPolicy(rs);
            return null;
        }
    }

    @Override
    public List<InsurancePolicy> getAll() throws SQLException {
        List<InsurancePolicy> list = new ArrayList<>();
        String sql = "SELECT * FROM InsurancePolicy";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractPolicy(rs));
        }
        return list;
    }

    private InsurancePolicy extractPolicy(ResultSet rs) throws SQLException {
        InsurancePolicy p = new InsurancePolicy();
        p.setPolicyId(rs.getInt("policy_id"));
        p.setVehicleId(rs.getInt("vehicle_id"));
        p.setInsuranceCompany(rs.getString("insurance_company"));
        p.setPolicyNumber(rs.getString("policy_number"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        p.setEndDate(rs.getDate("end_date").toLocalDate());
        p.setCoverageDetails(rs.getString("coverage_details"));
        return p;
    }
}