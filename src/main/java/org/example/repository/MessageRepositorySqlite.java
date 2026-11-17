package org.example.repository;

import org.example.config.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageRepositorySqlite {
    private final DatabaseManager databaseManager;

    public MessageRepositorySqlite(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveMessage(String sender, String content) {
        String sql = "INSERT INTO message (sender, content) VALUES (?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRecentMessages(int limit) {
        List<String> history = new ArrayList<>();
        String sql = "SELECT sender, content, strftime('%H:%M', created_at, 'localtime') as time FROM message ORDER BY id DESC LIMIT ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String time = rs.getString("time");
                    String sender = rs.getString("sender");
                    String content = rs.getString("content");
                    history.add("[" + time + "] " + sender + ": " + content);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        Collections.reverse(history);
        return history;
    }
}
