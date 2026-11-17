package org.example.repository;

import org.example.config.DatabaseManager;
import org.example.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthRepositorySqlite implements AuthRepository {
    private final DatabaseManager databaseManager;

    public AuthRepositorySqlite(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO user (username, password) VALUES (?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<User> findUserByUserName(String userName) {
        String sql = "SELECT id, username, password FROM user WHERE username = ?";
        User user = null;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userName);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String username = resultSet.getString("username");
                    String password = resultSet.getString("password");
                    user = new User(id, username, password);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(user);
    }
}
