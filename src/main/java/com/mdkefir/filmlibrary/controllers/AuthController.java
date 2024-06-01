package com.mdkefir.filmlibrary.controllers;

import com.mdkefir.filmlibrary.Database;
import com.mdkefir.filmlibrary.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthController {
    @FXML
    private ToggleButton loginPageButton;

    @FXML
    private ToggleButton registerPageButton;

    @FXML
    private Label secretText;

    @FXML
    private TextField secretField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;


    private Database db;

    private AuthCallback authCallback;

    public void setAuthCallback(AuthCallback authCallback) {
        this.authCallback = authCallback;
    }

    public AuthController() {
        db = new Database();  // Предполагается, что у вас есть класс Database для работы с базой данных
    }

    @FXML
    private void initialize() {

        loginPageButton.setSelected(true);
        handleLoginToggle();


        // Скрываем поле и текст для секретного кода по умолчанию, так как вход активен
        secretText.setVisible(false);
        secretField.setVisible(false);

        loginButton.setOnAction(event -> handleLoginOrRegister());
        loginPageButton.setOnAction(event -> handleLoginToggle());
        registerPageButton.setOnAction(event -> handleRegisterToggle());
    }

    public boolean registerUser(String username, String password, String secretWord) {
        String sql = "INSERT INTO users (username, password, secret_code) VALUES (?, ?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);  // В реальном приложении пароль следует хранить в зашифрованном виде
            pstmt.setString(3, secretWord);
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
        String getSecretCodeSql = "SELECT secret_code FROM users WHERE username = ?";
        String addFriendSql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";

        try (Connection conn = db.connect();
             PreparedStatement getSecretCodeStmt = conn.prepareStatement(getSecretCodeSql);
             PreparedStatement addFriendStmt = conn.prepareStatement(addFriendSql)) {

            // Получение секретного кода друга
            getSecretCodeStmt.setString(1, friendName);
            ResultSet rs = getSecretCodeStmt.executeQuery();

            if (rs.next()) {
                String dbSecretCode = rs.getString("secret_code");

                // Сравнение секретных кодов
                if (secretCode.equals(dbSecretCode)) {
                    // Добавление друга
                    addFriendStmt.setInt(1, userId);
                    addFriendStmt.setString(2, friendName); // предполагается, что в таблице users есть поле id
                    addFriendStmt.executeUpdate();
                    return true;
                } else {
                    System.out.println("Секретный код не соответствует");
                    return false;
                }
            } else {
                System.out.println("Пользователь с именем " + friendName + " не найден");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка добавления друга: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteFriend(int userId, String friendName) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
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
                friends.add(rs.getString("friend_id"));
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения списка друзей: " + e.getMessage());
        }
        return friends;
    }

    @FXML
    private void handleLoginToggle() {
        // Скрываем поле и текст для секретного кода
        secretText.setVisible(false);
        secretField.setVisible(false);
        loginButton.setText("Войти");
    }

    @FXML
    private void handleRegisterToggle() {
        // Показываем поле и текст для секретного кода
        secretText.setVisible(true);
        secretField.setVisible(true);
        loginButton.setText("Зарегистрироваться");
    }

    @FXML
    private void handleLoginOrRegister() {
        if (loginPageButton.isSelected()) {
            handleLogin();
        } else if (registerPageButton.isSelected()) {
            handleRegistration();
        }
    }

    @FXML
    private void handleRegistration() {
        String username = emailField.getText();
        String password = passwordField.getText();
        String secretWord = secretField.getText();

        if (secretWord.isEmpty()) {
            statusLabel.setText("Ошибка: Введите секретное слово");
            return;
        } else if (password.isEmpty()) {
            statusLabel.setText("Ошибка: Введите пароль для регистрации");
            return;
        } else if (username.isEmpty()) {
            statusLabel.setText("Ошибка: Введите имя пользователя для регистрации");
            return;
        }

        if (registerUser(username, password, secretWord)) {
            int userId = getUserId(username);
            User currentUser = new User(userId, username);
            currentUser.setSecretCode(secretWord); // Установка секретного кода пользователя
            authCallback.onLoginSuccess(currentUser); // Сообщаем MainController об успешной регистрации
            statusLabel.setText("Регистрация успешна");
        } else {
            statusLabel.setText("Ошибка регистрации");
        }
    }



    @FXML
    private void handleLogin() {
        String username = emailField.getText();
        String password = passwordField.getText();
        if (loginUser(username, password)) {
            int userId = getUserId(username);
            String secretCode = getUserSecretCode(userId); // Получение секретного кода пользователя
            User currentUser = new User(userId, username);
            currentUser.setSecretCode(secretCode); // Установка секретного кода пользователя
            authCallback.onLoginSuccess(currentUser); // Сообщаем MainController об успешной авторизации
            statusLabel.setText("Авторизация успешна");
        } else {
            statusLabel.setText("Ошибка: Неверный логин или пароль");
        }
    }



    private String getUserSecretCode(int userId) {
        String sql = "SELECT secret_code FROM users WHERE id = ?";
        try (Connection conn = db.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("secret_code");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка получения секретного кода: " + e.getMessage());
        }
        return "";
    }

}
