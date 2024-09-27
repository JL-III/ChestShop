package com.Acrobot.Breeze.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database class, which can be used to connect to JDBC
 *
 * @author Acrobot
 */
public class Database {
    private final String uri;
    private final String username;
    private final String password;

    public Database(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    public Database(String uri) {
        this.uri = uri;
        this.username = null;
        this.password = null;
    }

    /**
     * @return Connection to the database
     * @throws SQLException exception
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(uri, username, password);
    }
}
