package com.example.mochiapi.services;

import com.example.mochiapi.models.DailyGoal;
import com.example.mochiapi.models.PersonalGoal;
import com.example.mochiapi.repositories.dbRepository;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/goals")
public class GoalsResources {

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @GET
    @Path("/{date}")
    @Produces("application/json")
    public Response getGoals(@Context ContainerRequestContext ctx, @PathParam("date") String date) {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)) {
                Integer userId = (Integer) ctx.getProperty("userId");
                conn.setAutoCommit(false);
                PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.personalgoal WHERE user_id = ?");
                statement.setInt(1, userId);
                ResultSet rs = statement.executeQuery();
                List<PersonalGoal> goalList = new ArrayList<>();
                while (rs.next()) {
                    PersonalGoal newGoal = new PersonalGoal();
                    newGoal.setId(rs.getInt("id"));
                    newGoal.setUserId(userId);
                    newGoal.setTitle(rs.getString("title"));
                    newGoal.setDescription(rs.getString("description"));
                    newGoal.setCompleted(rs.getBoolean("completed"));
                    newGoal.setProgress(rs.getDouble("progress"));
                    newGoal.setQuantity(rs.getInt("quantity"));
                    List<DailyGoal> dailyGoalList = new ArrayList<>();
                    PreparedStatement statementDaily = conn.prepareStatement("SELECT * FROM mochi_project_test4.dailygoal WHERE user_id = ? AND person_goal_id = ? AND date = ?");
                    statementDaily.setInt(1, userId);
                    statementDaily.setInt(2, rs.getInt("id"));
                    statementDaily.setDate(3, Date.valueOf(date));
                    ResultSet rsDaily = statementDaily.executeQuery();
                    while (rsDaily.next()) {
                        DailyGoal newDaily = new DailyGoal();
                        newDaily.setId(rsDaily.getInt("id"));
                        newDaily.setUserId(rsDaily.getInt("user_id"));
                        newDaily.setTitle(rsDaily.getString("title"));
                        newDaily.setDescription(rsDaily.getString("description"));
                        newDaily.setCompleted(rsDaily.getBoolean("completed"));
                        newDaily.setQuantity(rsDaily.getDouble("quantity"));
                        newDaily.setDate(rsDaily.getString("date"));
                        newDaily.setPersonGoalId(rsDaily.getInt("person_goal_id"));
                        newDaily.setGoalTemplateId(rsDaily.getInt("goaltemplateid"));
                        newDaily.setProgress(rsDaily.getDouble("progress"));
                        dailyGoalList.add(newDaily);
                    }
                    newGoal.setDailyGoals(dailyGoalList);
                    goalList.add(newGoal);
                }

                conn.commit();
                conn.setAutoCommit(true);
                return Response.status(Response.Status.OK)
                        .entity(goalList)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't retrieve goals")
                .build();
    }

    @POST
    @Path("/personal")
    @Produces("application/json")
    @Consumes("application/json")
    public Response createPersonalGoals(@Context ContainerRequestContext ctx, PersonalGoal personalGoal) {
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer user_id = (Integer) ctx.getProperty("userId");
                if(user_id > 0){
                    conn.setAutoCommit(false);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO mochi_project_test4.personalgoal (id, user_id, title, description, completed, progress, quantity)\n" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);");
                    int lastGoal = dbRepository.lastPersonalGoal(conn);
                    statement.setInt(1, lastGoal + 1);
                    statement.setInt(2, user_id);
                    statement.setString(3, personalGoal.getTitle());
                    statement.setString(4, personalGoal.getDescription());
                    statement.setBoolean(5, personalGoal.isCompleted());
                    statement.setDouble(6, personalGoal.getProgress());
                    statement.setDouble(7, personalGoal.getQuantity());
                    System.out.println(personalGoal.getQuantity());
                    statement.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                    personalGoal.setId(lastGoal + 1);
                    personalGoal.setUserId(user_id);
                    return Response.status(Response.Status.OK)
                            .entity(personalGoal)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't create personal goal")
                .build();
    }

    @PUT
    @Path("/personal")
    @Consumes("application/json")
    @Produces("application/json")
    public Response editPersonalGoals(PersonalGoal personalGoal, @Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer user_id = (Integer) ctx.getProperty("userId");
                if(user_id > 0 && personalGoal.getId() > 0){
                    PreparedStatement statement = conn.prepareStatement("UPDATE mochi_project_test4.personalgoal SET title = ?, description = ? WHERE id = ? AND user_id = ?");
                    statement.setString(1, personalGoal.getTitle());
                    statement.setString(2, personalGoal.getDescription());
                    statement.setInt(3, personalGoal.getId());
                    statement.setInt(4, user_id);
                    statement.executeUpdate();
                    personalGoal.setUserId(user_id);
                    return Response.status(Response.Status.OK)
                            .entity(personalGoal)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't edit personal goal")
                .build();
    }

    @DELETE
    @Path("/personal/{id}")
    @Produces("application/json")
    public Response removePersonalGoal(@PathParam("id") String id,  @Context ContainerRequestContext ctx) {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                Integer user_id = (Integer) ctx.getProperty("userId");
                int idInt = Integer.parseInt(id);
                if (idInt > 0) {
                    PreparedStatement statement = conn.prepareStatement("DELETE FROM mochi_project_test4.personalgoal WHERE id = ? AND user_id = ?");
                    statement.setInt(1, idInt);
                    statement.setInt(2, user_id);

                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        return Response.status(Response.Status.OK)
                                .entity("Personal goal removed")
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Personal goal not found")
                                .build();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found in the classpath.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database driver configuration error " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid personal goal ID")
                .build();
    }

    @POST
    @Path("/daily")
    @Produces("application/json")
    @Consumes("application/json")
    public Response createDailyGoal(@Context ContainerRequestContext ctx, DailyGoal dailyGoal) {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                Integer user_id = (Integer) ctx.getProperty("userId");
                if (user_id > 0) {
                    conn.setAutoCommit(false);

                    PreparedStatement statement = conn.prepareStatement(
                            "INSERT INTO mochi_project_test4.dailygoal (id, date, user_id, person_goal_id, quantity, title, description, progress, completed, goalTemplateId)\n" +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                    );

                    int lastGoal = dbRepository.lastDailyGoal(conn);

                    statement.setInt(1, lastGoal + 1);
                    statement.setDate(2, Date.valueOf(dailyGoal.getDate()));
                    statement.setInt(3, user_id);
                    statement.setInt(4, dailyGoal.getPersonGoalId());
                    statement.setDouble(5, dailyGoal.getQuantity());
                    statement.setString(6, dailyGoal.getTitle());
                    statement.setString(7, dailyGoal.getDescription());
                    statement.setDouble(8, dailyGoal.getProgress());
                    statement.setBoolean(9, dailyGoal.isCompleted());
                    statement.setInt(10, dailyGoal.getGoalTemplateId());
                    System.out.println(dailyGoal.getPersonGoalId());
                    statement.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);

                    dailyGoal.setId(lastGoal + 1);
                    dailyGoal.setUserId(user_id);

                    return Response.status(Response.Status.OK)
                            .entity(dailyGoal)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded" + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't create daily goal")
                .build();
    }

    @PUT
    @Path("/daily")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateDailyGoal(DailyGoal dailyGoal, @Context ContainerRequestContext ctx) {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                Integer user_id = (Integer) ctx.getProperty("userId");
                if (user_id > 0 && dailyGoal.getId() > 0) {

                    PreparedStatement statement = conn.prepareStatement(
                            "UPDATE mochi_project_test4.dailygoal SET title = ?, description = ? WHERE id = ? AND user_id = ?"
                    );
                    statement.setString(1, dailyGoal.getTitle());
                    statement.setString(2, dailyGoal.getDescription());
                    statement.setInt(3, dailyGoal.getId());
                    statement.setInt(4, user_id);

                    statement.executeUpdate();
                    dailyGoal.setUserId(user_id);

                    return Response.status(Response.Status.OK)
                            .entity(dailyGoal)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded" + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't edit daily goal")
                .build();
    }

    @DELETE
    @Path("/daily/{id}")
    @Produces("application/json")
    public Response removeDailyGoal(@PathParam("id") String id, @Context ContainerRequestContext ctx) {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                Integer user_id = (Integer) ctx.getProperty("userId");
                int idInt = Integer.parseInt(id);
                if (idInt > 0) {
                    PreparedStatement statement = conn.prepareStatement(
                            "DELETE FROM mochi_project_test4.dailygoal WHERE id = ? AND user_id = ?"
                    );
                    statement.setInt(1, idInt);
                    statement.setInt(2, user_id);

                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        return Response.status(Response.Status.OK)
                                .entity("Daily goal removed")
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Daily goal not found")
                                .build();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found in the classpath.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database driver configuration error " + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid daily goal ID")
                .build();
    }
}
