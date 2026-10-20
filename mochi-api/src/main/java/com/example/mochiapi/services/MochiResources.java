package com.example.mochiapi.services;

import com.example.mochiapi.models.MochiEntity;
import com.example.mochiapi.security.TokenInfo;
import com.example.mochiapi.security.TokenManager;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.*;

@Path("/mochi")
public class MochiResources {

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMochiPet(@Context ContainerRequestContext ctx, MochiEntity updatedMochi) {
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

            String updateSql = "UPDATE mochi_project_test4.MochiEntity SET name = ?, color = ?, costume = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, updatedMochi.getName());
                stmt.setInt(2, updatedMochi.getColor());
                stmt.setInt(3, updatedMochi.getCostume());
                stmt.setInt(4, userId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Mochi update failed: Pet record not found for this user")
                            .build();
                }
            }

            MochiEntity freshMochi = null;
            String mochiSql = "SELECT color, evolution_stage, costume, name, mood_coefficient FROM mochi_project_test4.MochiEntity WHERE user_id = ?";
            try (PreparedStatement mochiStmt = conn.prepareStatement(mochiSql)) {
                mochiStmt.setInt(1, userId);
                try (ResultSet mochiRs = mochiStmt.executeQuery()) {
                    if (mochiRs.next()) {
                        freshMochi = new MochiEntity();
                        freshMochi.setUserId(userId);
                        freshMochi.setColor(mochiRs.getInt("color"));
                        freshMochi.setEvolutionStage(mochiRs.getInt("evolution_stage"));
                        freshMochi.setCostume(mochiRs.getInt("costume"));
                        freshMochi.setName(mochiRs.getString("name"));
                        freshMochi.setMoodCoefficient(mochiRs.getDouble("mood_coefficient"));
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);

            return Response.status(Response.Status.OK).entity(freshMochi).build();

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