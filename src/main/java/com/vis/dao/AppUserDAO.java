package com.vis.dao;

import com.vis.db.DBConnection;
import com.vis.models.AppUser;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppUserDAO extends BaseDAO<AppUser> {

    @Override
    public void insert(AppUser user) throws SQLException {
        String sql = "INSERT INTO AppUser (username, password, role, is_active) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());  // plain text
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isActive());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) user.setUserId(rs.getInt(1));
        }
    }

    @Override
    public void update(AppUser user) throws SQLException {
        String sql = "UPDATE AppUser SET username=?, password=?, role=?, is_active=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getUserId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM AppUser WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public AppUser getById(int id) throws SQLException {
        String sql = "SELECT * FROM AppUser WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUser(rs);
            return null;
        }
    }

    @Override
    public List<AppUser> getAll() throws SQLException {
        List<AppUser> list = new ArrayList<>();
        String sql = "SELECT * FROM AppUser ORDER BY user_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(extractUser(rs));
        }
        return list;
    }

    private AppUser extractUser(ResultSet rs) throws SQLException {
        AppUser u = new AppUser();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
}