package com.example.mochiapi.repositories;

import com.example.mochiapi.models.Activity;
import com.example.mochiapi.services.ActivityResources;

import java.sql.*;

public class dbRepository {
//    private Connection conn;
//
//    public dbService(DataSource source){
//        try{
//            this.conn = source.getConnection();
//        } catch (SQLException e) {
//            System.out.println("Error: " + e);
//        }
//    }

    public static int checkUserCount(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT COUNT(user_id) AS total FROM mochi_project_test4.person;");
        ResultSet rs = statement.executeQuery();
        int totalUsers = 0;
        while(rs.next()){
            totalUsers = rs.getInt("total");
        }
        rs.close();
        return totalUsers;
    }

    public static int checkActivityCount(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT COUNT(id) AS total FROM mochi_project_test4.activity;");
        ResultSet rs = statement.executeQuery();
        int totalActivities = 0;
        while(rs.next()){
            totalActivities = rs.getInt("total");
        }
        rs.close();
        return totalActivities;
    }

    public static boolean checkFirstActivity(Connection conn, int user_id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT completed FROM mochi_project_test4.activity WHERE creator_id = ? ORDER BY activity_date ASC LIMIT 1;");
        statement.setInt(1, user_id);
        ResultSet rs = statement.executeQuery();
        boolean completed = false;
        while(rs.next()){
            completed = rs.getBoolean("completed");
        }
        return completed;
    }

    public static int amountWorkStudyActivities(Connection conn, int user_id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT COUNT(id) AS amount FROM mochi_project_test4.activity WHERE creator_id = ? AND completed = true AND (type = 'work' OR type = 'study');");
        statement.setInt(1, user_id);
        ResultSet rs = statement.executeQuery();
        int amount = 0;
        while(rs.next()){
            amount = rs.getInt("amount");
            System.out.println();
            return amount;

        }
        return amount;
    }

    public static void updateAchievementStatus(Connection conn, int user_id, int achievement_id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("UPDATE mochi_project_test4.achievementassociation SET completed = true WHERE user_id = ? AND achievement_id = ?");
        statement.setInt(1, user_id);
        statement.setInt(2, achievement_id);
        statement.executeUpdate();
    }

    public static Activity findActivity(Connection conn, int id) throws SQLException {
        String query = "SELECT id, title, description, completed, type, creator_id, activity_date, start_time, end_time " +
                "FROM mochi_project_test4.activity WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Initialize using the default constructor
                    Activity activity = new Activity();

                    // Use setter methods to assign fields
                    activity.setId(rs.getInt("id"));
                    activity.setTitle(rs.getString("title"));
                    activity.setDescription(rs.getString("description"));
                    activity.setCompleted(rs.getBoolean("completed"));
                    activity.setType(rs.getString("type"));
                    activity.setCreator(rs.getInt("creator_id"));

                    java.sql.Date date = rs.getDate("activity_date");
                    if (date != null) {
                        activity.setStartTime(date.toString());
                    }

                    java.sql.Time start = rs.getTime("start_time");
                    if (start != null) {
                        activity.setStartTime(start.toString());
                    }

                    java.sql.Time end = rs.getTime("end_time");
                    if (end != null) {
                        activity.setEndTime(end.toString());
                    }

                    return activity;
                }
            }
        }

        return null;
    }

    public static int lastUserId(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT user_id FROM mochi_project_test4.person ORDER BY user_id DESC LIMIT 1;");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            int last = rs.getInt("user_id");
            rs.close();
            statement.close();
            return last;
        }

        rs.close();
        statement.close();
        return 0;

    }

    public static int lastActivityId(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT id FROM mochi_project_test4.activity ORDER BY id DESC LIMIT 1;");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            int last = rs.getInt("id");
            rs.close();
            statement.close();
            return last;
        }

        rs.close();
        statement.close();
        return 0;
    }

    public static int lastChallenge(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT id FROM mochi_project_test4.challenge ORDER BY id DESC LIMIT 1;");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            int last = rs.getInt("id");
            rs.close();
            statement.close();
            return last;
        }

        rs.close();
        statement.close();
        return 0;
    }

    public static int lastPersonalGoal(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT id FROM mochi_project_test4.personalgoal ORDER BY id DESC LIMIT 1;");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            int last = rs.getInt("id");
            rs.close();
            statement.close();
            return last;
        }

        rs.close();
        statement.close();
        return 0;
    }

    public static int lastChallengeId(Connection conn) throws SQLException {
        String query = "SELECT id FROM mochi_project_test4.challenge ORDER BY id DESC LIMIT 1;";
        try (PreparedStatement statement = conn.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return 0;
    }

    public static int lastDailyGoal(Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT id FROM mochi_project_test4.dailygoal ORDER BY id DESC LIMIT 1;");
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            int last = rs.getInt("id");
            rs.close();
            statement.close();
            return last;
        }

        rs.close();
        statement.close();
        return 0;
    }

    public static boolean isTimeAppropriate(Connection conn, int user_id, Time start, Time finish) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.idealday WHERE user_id = ?;");
        statement.setInt(1, user_id);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            Time wakeUp = rs.getTime("wake_up_time");
            Time bedtime = rs.getTime("bedtime");
            return !(start.before(wakeUp) || start.after(bedtime) || finish.before(wakeUp) || finish.after(bedtime));
        }
        rs.close();
        statement.close();
        return false;
    }

    public static boolean isWithinWorkingHours(Connection conn, int user_id, Date date) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.idealday WHERE user_id = ?;");
        statement.setInt(1, user_id);
        conn.setAutoCommit(false);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            double workingHours = rs.getDouble("max_working_hours");
            PreparedStatement statementAmount = conn.prepareStatement("SELECT COUNT(id) AS amount FROM mochi_project_test4.activity WHERE creator_id = ? AND activity_date = ? AND (type = 'work' OR type = 'study');");
            statementAmount.setInt(1, user_id);
            statementAmount.setDate(2, date);
            ResultSet rsAmount = statementAmount.executeQuery();
            int amount = 0;
            if(rsAmount.next()){
                amount = rsAmount.getInt("amount");
            }
            rsAmount.close();
            statementAmount.close();
            System.out.println((double) amount /2);
            System.out.println(workingHours);
            return (double) amount /2 <= workingHours;
        }
        rs.close();
        statement.close();
        conn.commit();
        conn.setAutoCommit(true);
        return false;
    }

    public static boolean isAchievementCompleted(Connection conn, int user_id, int achievement_id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT completed FROM mochi_project_test4.achievementassociation WHERE user_id = ? AND achievement_id = ?;");
        statement.setInt(1, user_id);
        statement.setInt(2, achievement_id);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
           return rs.getBoolean("completed");
        }

        rs.close();
        statement.close();
        return false;
    }

    public static int hasCompletedHatTrick(Connection conn, int user_id) throws SQLException {
        String query = "SELECT COUNT(*) FROM (" +
                "  SELECT date FROM mochi_project_test4.DailyGoal " +
                "  WHERE user_id = ? AND completed = true " +
                "  GROUP BY date HAVING COUNT(id) >= 3" +
                ") AS hat_tricks;";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return 1;
                }
            }
        }
        return 0;
    }

    public static int amountCompletedDailySubgoals(Connection conn, int user_id) throws SQLException {
        String query = "SELECT COUNT(id) AS amount FROM mochi_project_test4.DailyGoal WHERE user_id = ? AND completed = true;";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("amount");
                }
            }
        }
        return 0;
    }

    public static int getDailyGoalStreak(Connection conn, int user_id) throws SQLException {
        String query = "SELECT streak FROM mochi_project_test4.Person WHERE user_id = ?;";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user_id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("streak");
                }
            }
        }
        return 0;
    }
}
