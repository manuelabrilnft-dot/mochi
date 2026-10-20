package com.example.mochiapi.services;

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

@Path("/activity")
public class ActivityResources {

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @GET
    @Produces("application/json")
    public Response retrieveActivities(@QueryParam("startDate") @DefaultValue("none") String startDate,
                                       @QueryParam("endDate") @DefaultValue("none") String endDate,
                                       @Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer userId = (Integer) ctx.getProperty("userId");
                if(userId != null && startDate != null && endDate!= null && !startDate.equals("none") && !endDate.equals("none") && !userId.equals("none")){
                    PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.activity WHERE activity_date BETWEEN ? AND ? AND creator_id = ?");
                    statement.setDate(1, Date.valueOf(startDate));
                    statement.setDate(2, Date.valueOf(endDate));
                    statement.setInt(3, userId);
                    ResultSet rs = statement.executeQuery();
                    List<Activity> activities = new ArrayList<>();
                    while(rs.next()){
                        Activity tempActivity = new Activity();
                        tempActivity.setId(rs.getInt("id"));
                        tempActivity.setTitle(rs.getString("title"));
                        tempActivity.setDescription(rs.getString("description"));
                        tempActivity.setCompleted(rs.getBoolean("completed"));
                        tempActivity.setType(rs.getString("type"));
                        tempActivity.setCreator(rs.getInt("creator_id"));
                        tempActivity.setActivityDate(rs.getString("activity_date"));
                        tempActivity.setStartTime(rs.getString("start_time"));
                        tempActivity.setEndTime(rs.getString("end_time"));
                        activities.add(tempActivity);
                    }
                    return Response.status(Response.Status.OK)
                            .entity(activities)
                            .build();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded " + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't retrieve activities")
                .build();
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response retrieveActivities(Activity activity, @Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer user_id = (Integer) ctx.getProperty("userId");
                if(user_id > 0 && activity.getId() > 0){
                    PreparedStatement statement = conn.prepareStatement("UPDATE mochi_project_test4.activity SET title = ?, description = ? WHERE id = ?");
                    statement.setString(1, activity.getTitle());
                    statement.setString(2, activity.getDescription());
                    statement.setInt(3, activity.getId());
                    statement.executeUpdate();
                    activity.setCreator(user_id);
                    return Response.status(Response.Status.OK)
                            .entity(activity)
                            .build();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded" + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't edit activity")
                .build();
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createActivity(Activity activity, @Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer user_id = (Integer) ctx.getProperty("userId");
                if(!dbRepository.isTimeAppropriate(conn, user_id, Time.valueOf(activity.getStartTime()), Time.valueOf(activity.getStartTime()))){
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Activity outside awake hours")
                            .build();
                }
                if(!(dbRepository.isWithinWorkingHours(conn, user_id, Date.valueOf(activity.getActivityDate())))){
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Activity outside working hours ")
                            .build();
                }
                if(user_id > 0){
                    conn.setAutoCommit(false);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO mochi_project_test4.activity (id, title, description, completed, type, creator_id, activity_date, start_time, end_time)\n" +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    int activityAmount = dbRepository.lastActivityId(conn);
                    statement.setInt(1, activityAmount + 1);
                    statement.setString(2, activity.getTitle());
                    statement.setString(3, activity.getDescription());
                    statement.setBoolean(4, activity.isCompleted());
                    statement.setString(5, activity.getType());
                    statement.setInt(6, user_id);
                    statement.setDate(7, Date.valueOf(activity.getActivityDate()));
                    statement.setTime(8, Time.valueOf(activity.getStartTime()));
                    statement.setTime(9, Time.valueOf(activity.getEndTime()));
                    statement.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                    activity.setId(activityAmount + 1);
                    activity.setCreator(user_id);
                    return Response.status(Response.Status.OK)
                            .entity(activity)
                            .build();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded" + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't create activity")
                .build();
    }

    @PATCH
    @Path("/complete/{id}")
    @Produces("application/json")
    public Response completeActivity(@PathParam("id") int id) {
        if (id <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid activity ID")
                    .build();
        }

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {

                conn.setAutoCommit(false);


                String query = "UPDATE mochi_project_test4.activity SET completed = ? WHERE id = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setBoolean(1, true);
                    statement.setInt(2, id);

                    int rowsUpdated = statement.executeUpdate();

                    if (rowsUpdated == 0) {
                        // No rows updated means the ID doesn't exist. Rollback and return 404.
                        conn.rollback();
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Activity not found")
                                .build();
                    }

                    Activity updatedActivity = dbRepository.findActivity(conn, id);

                    conn.commit();
                    conn.setAutoCommit(true);

                    return Response.status(Response.Status.OK)
                            .entity(updatedActivity)
                            .build();

                } catch (Exception e) {
                    // Rollback if any SQL execution or fetching fails
                    conn.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    public Response removeActivity(@PathParam("id") String id) {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
                int idInt = Integer.parseInt(id);
                if (idInt > 0) {
                    PreparedStatement statement = conn.prepareStatement("DELETE FROM mochi_project_test4.activity WHERE id = ?");
                    statement.setInt(1, idInt);

                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        return Response.status(Response.Status.OK)
                                .entity("Activity removed")
                                .build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Activity not found")
                                .build();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found in the classpath.");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database driver configuration error" + e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database error: " + e.getMessage())
                    .build();
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid activity ID")
                .build();
    }
}
