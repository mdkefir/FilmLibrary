package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthController {
    private Database db;

    public AuthController() {
        db = new Database();  // Предполагается, что у вас есть класс Database для работы с базой данных
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);  // В реальном приложении пароль следует хранить в зашифрованном виде
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // Возвращает true, если пользователь найден
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения ID пользователя: " + e.getMessage());
        }
        return -1;  // Возвращаем -1, если пользователь не найден
    }

    public boolean addFriend(int userId, String friendName, String secretCode) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, friendName);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка добавления друга: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFriend(int userId, String friendName) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_name = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, friendName);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка удаления друга: " + e.getMessage());
            return false;
        }
    }

    public List<String> getFriends(int userId) {
        List<String> friends = new ArrayList<>();
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("friend_name"));
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения списка друзей: " + e.getMessage());
        }
        return friends;
    }

}
