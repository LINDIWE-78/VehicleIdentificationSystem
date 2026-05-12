package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.Claim;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimDAO {

    public void insert(Claim claim) throws SQLException {
        String sql = "INSERT INTO Claim (policy_id, claim_date, claim_amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claim.getPolicyId());
            ps.setDate(2, Date.valueOf(claim.getClaimDate()));
            ps.setBigDecimal(3, claim.getClaimAmount());
            ps.setString(4, claim.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) claim.setClaimId(rs.getInt(1));
        }
    }

    public List<Claim> getAll() throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM Claim ORDER BY claim_date DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractClaim(rs));
        }
        return list;
    }

    public List<Claim> findByPolicyId(int policyId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM Claim WHERE policy_id = ? ORDER BY claim_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, policyId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(extractClaim(rs));
        }
        return list;
    }

    public void update(Claim claim) throws SQLException {
        String sql = "UPDATE Claim SET policy_id=?, claim_date=?, claim_amount=?, status=? WHERE claim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, claim.getPolicyId());
            ps.setDate(2, Date.valueOf(claim.getClaimDate()));
            ps.setBigDecimal(3, claim.getClaimAmount());
            ps.setString(4, claim.getStatus());
            ps.setInt(5, claim.getClaimId());
            ps.executeUpdate();
        }
    }

    public void delete(int claimId) throws SQLException {
        String sql = "DELETE FROM Claim WHERE claim_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, claimId);
            ps.executeUpdate();
        }
    }

    private Claim extractClaim(ResultSet rs) throws SQLException {
        Claim c = new Claim();
        c.setClaimId(rs.getInt("claim_id"));
        c.setPolicyId(rs.getInt("policy_id"));
        c.setClaimDate(rs.getDate("claim_date").toLocalDate());
        c.setClaimAmount(rs.getBigDecimal("claim_amount"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}