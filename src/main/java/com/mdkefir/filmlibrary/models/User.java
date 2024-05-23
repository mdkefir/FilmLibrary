package com.mdkefir.filmlibrary.models;

public class User {
    private int id;  // идентификатор пользователя
    private String username;  // имя пользователя
    private String password;  // пароль пользователя

    // Конструктор
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    // Геттер для идентификатора пользователя
    public int getId() {
        return id;
    }

    // Геттеры для других полей
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
