package com.vis.dao;

import java.sql.SQLException;
import java.util.List;

public abstract class BaseDAO<T> {
    public abstract void insert(T entity) throws SQLException;
    public abstract void update(T entity) throws SQLException;
    public abstract void delete(int id) throws SQLException;
    public abstract T getById(int id) throws SQLException;
    public abstract List<T> getAll() throws SQLException;
}