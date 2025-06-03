package com.mygdx.tankgame.db;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HighScoreManager {

    private static final String URL = "jdbc:mysql://localhost:3306/tankgame";
    private static final String USER = "root";
    private static final String PASS = "12345";

    public static class ScoreEntry {
        public String name;
        public int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public List<ScoreEntry> getTopScores(int limit) {
        List<ScoreEntry> scores = new ArrayList<>();

        try {
            // NẠP JDBC DRIVER
            Class.forName("com.mysql.cj.jdbc.Driver");

            // KẾT NỐI
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            String sql = "SELECT player_name, score FROM highscores ORDER BY score DESC LIMIT ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.add(new ScoreEntry(rs.getString("player_name"), rs.getInt("score")));
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return scores;
    }

}
