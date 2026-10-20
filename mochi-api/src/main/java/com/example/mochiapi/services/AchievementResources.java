package com.example.mochiapi.services;

import com.example.mochiapi.models.Achievement;
import com.example.mochiapi.models.Activity;
import com.example.mochiapi.repositories.dbRepository;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/achievement")
public class AchievementResources {
    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @GET
    @Produces("application/json")
    public Response getAchievements(@Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.achievement");
                Integer user_id = (Integer) ctx.getProperty("userId");
                conn.setAutoCommit(false);
                ResultSet rs = statement.executeQuery();
                    List<Achievement> achievementList = new ArrayList<>();
                    while(rs.next()) {
                        Achievement achievement = new Achievement();
                        achievement.setId(rs.getInt("id"));
                        boolean achievementCompleted = dbRepository.isAchievementCompleted(conn, user_id, rs.getInt("id"));
                        achievement.setCompleted(achievementCompleted);
                        if(!achievement.isCompleted()) {
                            switch (achievement.getId()) {
                                case 1 -> {
                                    int amount = dbRepository.amountWorkStudyActivities(conn, user_id);
                                    achievement.setProgress(amount);
                                    if(amount >= 1){
                                        achievement.setProgress(1);
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                                case 2 -> {
                                    int amount = dbRepository.amountWorkStudyActivities(conn, user_id);
                                    achievement.setProgress(amount);
                                    if(amount >= 10){
                                        achievement.setProgress(10);
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                                case 3 -> {
                                    int amount = dbRepository.amountWorkStudyActivities(conn, user_id);
                                    achievement.setProgress(amount);
                                    if(amount >= 50){
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                                case 4 -> {
                                    int completedHatTrick = dbRepository.hasCompletedHatTrick(conn, user_id);
                                    achievement.setProgress(completedHatTrick);
                                    if(completedHatTrick >= 1){
                                        achievement.setProgress(1);
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                                case 5 -> {
                                    int amount = dbRepository.amountCompletedDailySubgoals(conn, user_id);
                                    achievement.setProgress(amount);
                                    if(amount >= 30){
                                        achievement.setProgress(30);
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                                case 6 -> {
                                    int streak = dbRepository.getDailyGoalStreak(conn, user_id);
                                    achievement.setProgress(streak);
                                    if(streak >= 7){
                                        achievement.setProgress(7);
                                        dbRepository.updateAchievementStatus(conn, user_id, achievement.getId());
                                        achievement.setCompleted(true);
                                    }
                                }
                            }
                        }else{
                            achievement.setProgress(rs.getInt("threshold"));
                        }
                        achievement.setTitle(rs.getString("title"));
                        achievement.setDescription(rs.getString("description"));
                        achievement.setXp(rs.getInt("xp"));
                        achievement.setType(rs.getString("type"));
                        achievement.setThreshold(rs.getInt("threshold"));
                        achievementList.add(achievement);
                    }
                    conn.commit();
                    conn.setAutoCommit(true);
                    return Response.status(Response.Status.OK)
                            .entity(achievementList)
                            .build();
                } catch (Exception e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(e.getMessage())
                            .build();
                }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded: " + e.getMessage())
                    .build();
        }
    }
}
