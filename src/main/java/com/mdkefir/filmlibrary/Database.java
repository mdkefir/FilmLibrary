package com.mdkefir.filmlibrary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {
    public static Connection connect() {
        String url = "jdbc:sqlite:src/main/java/com/mdkefir/filmlibrary/filmlibrary.db";  // Укажите путь к вашей базе данных
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // Создание таблицы пользователей
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL);");
                stmt.execute("DROP TABLE favorites");
                // Создание таблицы избранных фильмов
                stmt.execute("CREATE TABLE IF NOT EXISTS favorites (" +
                        "user_id INTEGER," +
                        "title TEXT NOT NULL," +
                        "year TEXT," +
                        "rating TEXT," +
                        "image_path TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES users(id));");

                System.out.println("Таблицы созданы");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        createNewDatabase(); // Создание базы данных и таблиц при запуске
    }
}

