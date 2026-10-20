package com.example.mochiapi.services;

import com.example.mochiapi.models.Challenge;
import com.example.mochiapi.repositories.dbRepository;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/challenge")
public class ChallengeResources {
    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGoalChallenge(Challenge challenge, @Context ContainerRequestContext ctx) {
        Integer user_id = (Integer) ctx.getProperty("userId");
        if (challenge == null || challenge.getTitle() == null || challenge.getTitle().trim().isEmpty() || user_id == null || user_id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid challenge data. Title cannot be blank and a valid Creator ID must be provided.")
                    .build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                conn.setAutoCommit(false);
                try {
                    int newChallengeId = dbRepository.lastChallengeId(conn) + 1;

                    String insertChallengeSql = "INSERT INTO mochi_project_test4.challenge (id, title, description, quantity, goaltemplateid, creator_id) VALUES (?, ?, ?, ?, ?, ?);";
                    try (PreparedStatement stmt = conn.prepareStatement(insertChallengeSql)) {
                        stmt.setInt(1, newChallengeId);
                        stmt.setString(2, challenge.getTitle());
                        stmt.setString(3, challenge.getDescription());
                        stmt.setDouble(4, challenge.getQuantity());
                        stmt.setInt(5, challenge.getGoalTemplateId());
                        stmt.setInt(6, user_id);
                        stmt.executeUpdate();
                    }

                    String insertParticipantSql = "INSERT INTO mochi_project_test4.challengeparticipation (user_id, challenge_id) VALUES (?, ?);";

                    try (PreparedStatement partStmt = conn.prepareStatement(insertParticipantSql)) {
                        partStmt.setInt(1, user_id);
                        partStmt.setInt(2, newChallengeId);
                        partStmt.executeUpdate();
                    }

                    if (challenge.getParticipants() != null && !challenge.getParticipants().isEmpty()) {
                        String findFriendSql = "SELECT p.user_id FROM mochi_project_test4.person p " +
                                "JOIN mochi_project_test4.friendship f " +
                                "ON (f.sender_id = p.user_id AND f.receiver_id = ?) " +
                                "OR (f.receiver_id = p.user_id AND f.sender_id = ?) " +
                                "WHERE p.username = ? AND f.accepted = TRUE;";

                        try (PreparedStatement findFriendStmt = conn.prepareStatement(findFriendSql);
                             PreparedStatement partStmtInsert = conn.prepareStatement(insertParticipantSql)) {

                            for (String username : challenge.getParticipants()) {
                                if (username == null || username.trim().isEmpty()) continue;

                                findFriendStmt.setInt(1, user_id);
                                findFriendStmt.setInt(2, user_id);
                                findFriendStmt.setString(3, username);

                                try (ResultSet rs = findFriendStmt.executeQuery()) {
                                    if (rs.next()) {
                                        int targetUserId = rs.getInt("user_id");

                                        if (targetUserId == user_id) continue;

                                        partStmtInsert.setInt(1, targetUserId);
                                        partStmtInsert.setInt(2, newChallengeId); // (or challenge.getId() for updateChallenge)
                                        partStmtInsert.executeUpdate();
                                    } else {
                                        conn.rollback();
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Couldn't process challenge - participant '" + username + "' is either not found or not your friend.")
                                                .build();
                                    }
                                }
                            }
                        }
                    }

                    conn.commit();
                    challenge.setId(newChallengeId);
                    challenge.setCreatorId(user_id);
                    return Response.status(Response.Status.OK).entity(challenge).build();

                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Database transaction failed: " + e.getMessage())
                            .build();
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Couldn't create challenge").build();
    }

    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    public Response deleteChallenge(@PathParam("id") int challengeId) {
        if (challengeId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid challenge ID provided.")
                    .build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                String deleteChallengeSql = "DELETE FROM mochi_project_test4.challenge WHERE id = ?;";

                try (PreparedStatement stmt = conn.prepareStatement(deleteChallengeSql)) {
                    stmt.setInt(1, challengeId);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected == 0) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Challenge with ID " + challengeId + " not found.")
                                .build();
                    }
                }

                return Response.status(Response.Status.OK)
                        .entity("Challenge deleted successfully.")
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred while trying to delete challenge: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't delete challenge")
                .build();
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateChallenge(Challenge challenge, @Context ContainerRequestContext ctx) {
        Integer user_id = (Integer) ctx.getProperty("userId");
        if (challenge == null || challenge.getId() <= 0 || challenge.getTitle() == null || challenge.getTitle().trim().isEmpty() || user_id == null || user_id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid data. ID, Title, and Creator ID must be provided correctly.")
                    .build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                conn.setAutoCommit(false);
                try {
                    String updateChallengeSql = "UPDATE mochi_project_test4.challenge SET title = ?, description = ?, quantity = ?, goaltemplateid = ? WHERE id = ? AND creator_id = ?;";
                    try (PreparedStatement stmt = conn.prepareStatement(updateChallengeSql)) {
                        stmt.setString(1, challenge.getTitle());
                        stmt.setString(2, challenge.getDescription());
                        stmt.setDouble(3, challenge.getQuantity());
                        stmt.setInt(4, challenge.getGoalTemplateId());
                        stmt.setInt(5, challenge.getId());
                        stmt.setInt(6, user_id);

                        int rowsAffected = stmt.executeUpdate();
                        if (rowsAffected == 0) {
                            conn.rollback();
                            return Response.status(Response.Status.NOT_FOUND)
                                    .entity("Challenge not found or you do not have permission to modify it.")
                                    .build();
                        }
                    }

                    String deleteParticipantsSql = "DELETE FROM mochi_project_test4.challengeparticipation WHERE challenge_id = ?;";
                    try (PreparedStatement delStmt = conn.prepareStatement(deleteParticipantsSql)) {
                        delStmt.setInt(1, challenge.getId());
                        delStmt.executeUpdate();
                    }

                    String insertParticipantSql = "INSERT INTO mochi_project_test4.challengeparticipation (user_id, challenge_id) VALUES (?, ?);";
                    try (PreparedStatement partStmtInsert = conn.prepareStatement(insertParticipantSql)) {
                        partStmtInsert.setInt(1, user_id);
                        partStmtInsert.setInt(2, challenge.getId());
                        partStmtInsert.executeUpdate();
                    }

                    if (challenge.getParticipants() != null && !challenge.getParticipants().isEmpty()) {
                        String findFriendSql = "SELECT p.user_id FROM mochi_project_test4.person p " +
                                "JOIN mochi_project_test4.friendship f " +
                                "ON (f.sender_id = p.user_id AND f.receiver_id = ?) " +
                                "OR (f.receiver_id = p.user_id AND f.sender_id = ?) " +
                                "WHERE p.username = ? AND f.accepted = TRUE;";

                        try (PreparedStatement findFriendStmt = conn.prepareStatement(findFriendSql);
                             PreparedStatement partStmtInsert = conn.prepareStatement(insertParticipantSql)) {

                            for (String username : challenge.getParticipants()) {
                                if (username == null || username.trim().isEmpty()) continue;

                                findFriendStmt.setInt(1, user_id);
                                findFriendStmt.setInt(2, user_id);
                                findFriendStmt.setString(3, username);

                                try (ResultSet rs = findFriendStmt.executeQuery()) {
                                    if (rs.next()) {
                                        int targetUserId = rs.getInt("user_id");

                                        if (targetUserId == user_id) continue;

                                        partStmtInsert.setInt(1, targetUserId);
                                        partStmtInsert.setInt(2, challenge.getId());
                                        partStmtInsert.executeUpdate();
                                    } else {
                                        conn.rollback();
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                .entity("Couldn't process challenge - participant '" + username + "' is either not found or not your friend.")
                                                .build();
                                    }
                                }
                            }
                        }
                    }

                    conn.commit();
                    challenge.setCreatorId(user_id);
                    return Response.status(Response.Status.OK).entity(challenge).build();

                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Database transaction failed during update: " + e.getMessage())
                            .build();
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.BAD_REQUEST).entity("Couldn't update challenge").build();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getChallenge(@PathParam("id") int challengeId, @Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (challengeId <= 0 || userId == null || userId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid challenge ID or User ID provided.")
                    .build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                conn.setAutoCommit(false);

                try {
                    Challenge challenge = null;
                    String fetchChallengeSql = "SELECT title, description, quantity, completed, goaltemplateid, creator_id FROM mochi_project_test4.challenge WHERE id = ?;";

                    try (PreparedStatement stmt = conn.prepareStatement(fetchChallengeSql)) {
                        stmt.setInt(1, challengeId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                challenge = new Challenge();
                                challenge.setId(challengeId);
                                challenge.setTitle(rs.getString("title"));
                                challenge.setDescription(rs.getString("description"));
                                challenge.setCreatorId(rs.getInt("creator_id"));
                                challenge.setQuantity(rs.getDouble("quantity"));
                                challenge.setCompleted(rs.getBoolean("completed"));
                                challenge.setGoalTemplateId(rs.getInt("goaltemplateid"));
                            } else {
                                conn.rollback();
                                return Response.status(Response.Status.NOT_FOUND)
                                        .entity("Challenge with ID " + challengeId + " not found.")
                                        .build();
                            }
                        }
                    }

                    String checkMembershipSql = "SELECT 1 FROM mochi_project_test4.challengeparticipation WHERE user_id = ? AND challenge_id = ?;";
                    try (PreparedStatement memberStmt = conn.prepareStatement(checkMembershipSql)) {
                        memberStmt.setInt(1, userId);
                        memberStmt.setInt(2, challengeId);
                        try (ResultSet rsMember = memberStmt.executeQuery()) {
                            if (!rsMember.next()) {
                                conn.rollback();
                                return Response.status(Response.Status.FORBIDDEN)
                                        .entity("Access denied. You are not a participant of this challenge.")
                                        .build();
                            }
                        }
                    }

                    String fetchLeaderboardSql = "SELECT p.username, cp.progress FROM mochi_project_test4.challengeparticipation cp " +
                            "JOIN mochi_project_test4.person p ON cp.user_id = p.user_id " +
                            "WHERE cp.challenge_id = ? " +
                            "ORDER BY cp.progress DESC;";

                    java.util.Map<String, Double> leaderboardMap = new java.util.LinkedHashMap<>();
                    List<String> participantList = new ArrayList<>();

                    try (PreparedStatement leadStmt = conn.prepareStatement(fetchLeaderboardSql)) {
                        leadStmt.setInt(1, challengeId);
                        try (ResultSet rs = leadStmt.executeQuery()) {
                            while (rs.next()) {
                                String username = rs.getString("username");
                                Double progress = rs.getDouble("progress");
                                leaderboardMap.put(username, progress);
                                participantList.add(username);
                            }
                        }
                    }

                    challenge.setLeaderboard(leaderboardMap);
                    challenge.setParticipants(participantList);

                    conn.commit();
                    return Response.status(Response.Status.OK).entity(challenge).build();

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred while retrieving challenge: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getMyChallenges(@QueryParam("userId") int userId) {
        if (userId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("A valid User ID must be provided to retrieve challenges.")
                    .build();
        }

        List<Challenge> challengesList = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                conn.setAutoCommit(false);

                try {
                    String fetchMyChallengesSql = "SELECT c.id, c.title, c.description, c.quantity, c.completed, c.goaltemplateid, c.creator_id " +
                            "FROM mochi_project_test4.challenge c " +
                            "JOIN mochi_project_test4.challengeparticipation cp ON c.id = cp.challenge_id " +
                            "WHERE cp.user_id = ? " +
                            "ORDER BY c.id ASC;";

                    try (PreparedStatement stmt = conn.prepareStatement(fetchMyChallengesSql)) {
                        stmt.setInt(1, userId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                Challenge challenge = new Challenge();
                                challenge.setId(rs.getInt("id"));
                                challenge.setTitle(rs.getString("title"));
                                challenge.setDescription(rs.getString("description"));
                                challenge.setCreatorId(rs.getInt("creator_id"));
                                challenge.setQuantity(rs.getDouble("quantity"));
                                challenge.setCompleted(rs.getBoolean("completed"));
                                challenge.setGoalTemplateId(rs.getInt("goaltemplateid"));
                                challengesList.add(challenge);
                            }
                        }
                    }

                    if (!challengesList.isEmpty()) {
                        String fetchParticipantsSql = "SELECT p.username FROM mochi_project_test4.challengeparticipation cp " +
                                "JOIN mochi_project_test4.person p ON cp.user_id = p.user_id " +
                                "WHERE cp.challenge_id = ?;";

                        try (PreparedStatement partStmt = conn.prepareStatement(fetchParticipantsSql)) {
                            for (Challenge ch : challengesList) {
                                partStmt.setInt(1, ch.getId());
                                List<String> participants = new ArrayList<>();
                                try (ResultSet rsPart = partStmt.executeQuery()) {
                                    while (rsPart.next()) {
                                        participants.add(rsPart.getString("username"));
                                    }
                                }
                                ch.setParticipants(participants);
                            }
                        }
                    }

                    conn.commit();
                    return Response.status(Response.Status.OK).entity(challengesList).build();

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error occurred while retrieving challenges: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Couldn't retrieve challenges " + e.getMessage()).build();
        }
    }
}