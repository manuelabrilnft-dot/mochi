package com.example.mochiapi.services;

import com.example.mochiapi.models.Friendship;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/friendship")
public class FriendshipResources {

    static Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @POST
    @Path("/request")
    @Consumes("application/json")
    @Produces("application/json")
    public Response sendFriendRequest(@QueryParam("friendId") int friendId, @Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (userId == null || friendId <= 0 || userId == friendId) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid friend request criteria.").build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                conn.setAutoCommit(false);

                String checkInverseQuery = "SELECT accepted FROM mochi_project_test4.Friendship WHERE sender_id = ? AND receiver_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkInverseQuery)) {
                    checkStmt.setInt(1, friendId);
                    checkStmt.setInt(2, userId);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            boolean alreadyAccepted = rs.getBoolean("accepted");

                            if (alreadyAccepted) {
                                return Response.status(Response.Status.CONFLICT).entity("You are already friends!").build();
                            }

                            // Inverse request exists and is false
                            String updateQuery = "UPDATE mochi_project_test4.Friendship SET accepted = TRUE WHERE sender_id = ? AND receiver_id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, friendId);
                                updateStmt.setInt(2, userId);
                                updateStmt.executeUpdate();

                                conn.commit();
                                conn.setAutoCommit(true);

                                Friendship friendship = new Friendship();
                                friendship.setSenderId(friendId);
                                friendship.setReceiverId(userId);
                                friendship.setAccepted(true);

                                return Response.status(Response.Status.OK).entity(friendship).build();
                            }
                        }
                    }
                }

                // No inverse request found
                String insertQuery = "INSERT INTO mochi_project_test4.Friendship (sender_id, receiver_id, accepted) VALUES (?, ?, FALSE)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, friendId);
                    insertStmt.executeUpdate();

                    conn.commit();
                    conn.setAutoCommit(true);

                    Friendship friendship = new Friendship();
                    friendship.setSenderId(userId);
                    friendship.setReceiverId(friendId);
                    friendship.setAccepted(false);

                    return Response.status(Response.Status.CREATED).entity(friendship).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/accept")
    @Produces("application/json")
    public Response acceptFriendRequest(@QueryParam("senderId") int senderId, @Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (userId == null || senderId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request details.").build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                // Sender is friend1, Current User is friend
                String query = "UPDATE mochi_project_test4.Friendship SET accepted = TRUE WHERE sender_id = ? AND receiver_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, senderId);
                    stmt.setInt(2, userId);
                    int rowsUpdated = stmt.executeUpdate();

                    if (rowsUpdated == 0) {
                        return Response.status(Response.Status.NOT_FOUND).entity("Friend request not found.").build();
                    }

                    Friendship friendship = new Friendship();
                    friendship.setSenderId(senderId);
                    friendship.setReceiverId(userId);
                    friendship.setAccepted(true);

                    return Response.status(Response.Status.OK).entity(friendship).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response getFriendsList(@Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated.").build();
        }

        List<java.util.Map<String, Object>> friendsList = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                String query = "SELECT p.user_id, p.username FROM mochi_project_test4.Friendship f " +
                        "JOIN mochi_project_test4.person p ON p.user_id = " +
                        "CASE WHEN f.sender_id = ? THEN f.receiver_id ELSE f.sender_id END " +
                        "WHERE (f.sender_id = ? OR f.receiver_id = ?) AND f.accepted = TRUE " +
                        "ORDER BY p.username ASC;";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, userId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.Map<String, Object> friendMap = new java.util.HashMap<>();
                            friendMap.put("id", rs.getInt("user_id"));
                            friendMap.put("username", rs.getString("username"));
                            friendsList.add(friendMap);
                        }
                    }
                    return Response.status(Response.Status.OK).entity(friendsList).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/remove")
    @Produces("application/json")
    public Response removeFriend(@QueryParam("friendId") int friendId, @Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (userId == null || friendId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid credentials.").build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                String query = "DELETE FROM mochi_project_test4.Friendship WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, friendId);
                    stmt.setInt(3, friendId);
                    stmt.setInt(4, userId);

                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        return Response.status(Response.Status.OK).entity("Friendship relationship updated/removed.").build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("Friendship relation not found.").build();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/search")
    @Produces("application/json")
    public Response searchUsers(@QueryParam("query") String searchQuery, @Context ContainerRequestContext ctx) {
        Integer currentUserId = (Integer) ctx.getProperty("userId");
        if (currentUserId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated.").build();
        }

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Search query cannot be empty.").build();
        }

        List<java.util.Map<String, Object>> searchResults = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                String query = "SELECT user_id, username FROM mochi_project_test4.person " +
                        "WHERE username ILIKE ? AND user_id != ? " +
                        "ORDER BY username ASC LIMIT 20;";

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, "%" + searchQuery.trim() + "%");
                    stmt.setInt(2, currentUserId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                            userMap.put("id", rs.getInt("user_id"));
                            userMap.put("username", rs.getString("username"));
                            searchResults.add(userMap);
                        }
                    }

                    return Response.status(Response.Status.OK).entity(searchResults).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error during search: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/requests")
    @Produces("application/json")
    public Response getFriendRequests(@QueryParam("type") String type, @Context ContainerRequestContext ctx) {
        Integer userId = (Integer) ctx.getProperty("userId");
        if (userId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated.").build();
        }

        if (type == null || (!type.equalsIgnoreCase("received") && !type.equalsIgnoreCase("sent"))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid type parameter. Use 'received' or 'sent'.").build();
        }

        List<java.util.Map<String, Object>> requestList = new ArrayList<>();
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                String query;
                if (type.equalsIgnoreCase("received")) {
                    query = "SELECT p.user_id, p.username FROM mochi_project_test4.Friendship f " +
                            "JOIN mochi_project_test4.Person p ON f.sender_id = p.user_id " +
                            "WHERE f.receiver_id = ? AND f.accepted = FALSE " +
                            "ORDER BY p.username ASC;";
                } else {
                    query = "SELECT p.user_id, p.username FROM mochi_project_test4.Friendship f " +
                            "JOIN mochi_project_test4.Person p ON f.receiver_id = p.user_id " +
                            "WHERE f.sender_id = ? AND f.accepted = FALSE " +
                            "ORDER BY p.username ASC;";
                }

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                            userMap.put("id", rs.getInt("user_id"));
                            userMap.put("username", rs.getString("username"));
                            requestList.add(userMap);
                        }
                    }
                    return Response.status(Response.Status.OK).entity(requestList).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage()).build();
        }
    }
}