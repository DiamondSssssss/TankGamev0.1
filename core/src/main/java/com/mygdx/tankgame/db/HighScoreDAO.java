package com.mygdx.tankgame.db;

import java.sql.*;

public class HighScoreDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/tankgame";
    private static final String USER = "root";
    private static final String PASS = "12345";

    public static void insertScore(String name, int score) {
        String sql = "INSERT INTO highscores (player_name, score) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, score);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getHighestScore() {
        String sql = "SELECT MAX(score) FROM highscores";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int highest = rs.getInt(1);
                System.out.println("Raw highest score from DB: " + highest);
                if (rs.wasNull()) {
                    System.out.println("Highest score is NULL in DB (table empty).");
                    return 0;
                }
                return highest;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
