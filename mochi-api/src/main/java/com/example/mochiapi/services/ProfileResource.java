package com.example.mochiapi.services;

import com.example.mochiapi.models.UserProfile;
import com.example.mochiapi.models.MochiEntity;
import com.example.mochiapi.security.TokenInfo;
import com.example.mochiapi.security.TokenManager;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Path("/profile")
public class ProfileResource {

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfileData(@Context ContainerRequestContext ctx) {
        if (ctx.getCookies().get("session_token") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing session token cookie")
                    .build();
        }

        String token = ctx.getCookies().get("session_token").getValue();
        TokenInfo info = TokenManager.getInfo(token);

        if (info == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired session token")
                    .build();
        }

        int userId = info.getUser_id();
        Connection conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);

            conn.setAutoCommit(false);

            UserProfile userProfile = null;

            String userSql = "SELECT username, level, streak, xp, layout, background FROM mochi_project_test4.Person WHERE user_id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setInt(1, userId);
                try (ResultSet userRs = userStmt.executeQuery()) {
                    if (userRs.next()) {
                        userProfile = new UserProfile();
                        userProfile.setUsername(userRs.getString("username"));
                        userProfile.setLevel(userRs.getInt("level"));
                        userProfile.setStreak(userRs.getInt("streak"));
                        userProfile.setXp(userRs.getInt("xp"));
                        userProfile.setLayout(userRs.getInt("layout"));
                        userProfile.setBackground(userRs.getInt("background"));
                    }
                }
            }

            if (userProfile == null) {
                conn.rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Profile details for user not found")
                        .build();
            }

            String mochiSql = "SELECT color, evolution_stage, costume, name, mood_coefficient FROM mochi_project_test4.MochiEntity WHERE user_id = ?";
            try (PreparedStatement mochiStmt = conn.prepareStatement(mochiSql)) {
                mochiStmt.setInt(1, userId);
                try (ResultSet mochiRs = mochiStmt.executeQuery()) {
                    if (mochiRs.next()) {
                        MochiEntity mochiEntity = new MochiEntity();
                        mochiEntity.setUserId(userId);
                        mochiEntity.setColor(mochiRs.getInt("color"));
                        mochiEntity.setEvolutionStage(mochiRs.getInt("evolution_stage"));
                        mochiEntity.setCostume(mochiRs.getInt("costume"));
                        mochiEntity.setName(mochiRs.getString("name"));
                        mochiEntity.setMoodCoefficient(mochiRs.getDouble("mood_coefficient"));

                        userProfile.setMochi(mochiEntity);
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            return Response.status(Response.Status.OK).entity(userProfile).build();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database runtime failure: " + e.getMessage())
                    .build();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database driver missing mapping details: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("An unexpected processing error occurred: " + e.getMessage())
                    .build();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfileData(@Context ContainerRequestContext ctx, UserProfile updatedProfile) {
        if (ctx.getCookies().get("session_token") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing session token cookie")
                    .build();
        }

        String token = ctx.getCookies().get("session_token").getValue();
        TokenInfo info = TokenManager.getInfo(token);

        if (info == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired session token")
                    .build();
        }

        int userId = info.getUser_id();
        Connection conn = null;

        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);

            conn.setAutoCommit(false);

            String updateSql = "UPDATE mochi_project_test4.Person SET layout = ?, background = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, updatedProfile.getLayout());
                stmt.setInt(2, updatedProfile.getBackground());
                stmt.setInt(3, userId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Profile update failed: User not found")
                            .build();
                }
            }

            UserProfile completeProfile = null;
            String userSql = "SELECT username, level, streak, xp, layout, background FROM mochi_project_test4.Person WHERE user_id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setInt(1, userId);
                try (ResultSet userRs = userStmt.executeQuery()) {
                    if (userRs.next()) {
                        completeProfile = new UserProfile();
                        completeProfile.setUsername(userRs.getString("username"));
                        completeProfile.setLevel(userRs.getInt("level"));
                        completeProfile.setStreak(userRs.getInt("streak"));
                        completeProfile.setXp(userRs.getInt("xp"));
                        completeProfile.setLayout(userRs.getInt("layout"));
                        completeProfile.setBackground(userRs.getInt("background"));
                    }
                }
            }

            String mochiSql = "SELECT color, evolution_stage, costume, name, mood_coefficient FROM mochi_project_test4.MochiEntity WHERE user_id = ?";
            try (PreparedStatement mochiStmt = conn.prepareStatement(mochiSql)) {
                mochiStmt.setInt(1, userId);
                try (ResultSet mochiRs = mochiStmt.executeQuery()) {
                    if (mochiRs.next()) {
                        MochiEntity mochiEntity = new MochiEntity();
                        mochiEntity.setUserId(userId);
                        mochiEntity.setColor(mochiRs.getInt("color"));
                        mochiEntity.setEvolutionStage(mochiRs.getInt("evolution_stage"));
                        mochiEntity.setCostume(mochiRs.getInt("costume"));
                        mochiEntity.setName(mochiRs.getString("name"));
                        mochiEntity.setMoodCoefficient(mochiRs.getDouble("mood_coefficient"));
                        completeProfile.setMochi(mochiEntity);
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            return Response.status(Response.Status.OK).entity(completeProfile).build();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database runtime failure: " + e.getMessage())
                    .build();
        } catch (ClassNotFoundException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database driver missing mapping details: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("An unexpected processing error occurred: " + e.getMessage())
                    .build();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}