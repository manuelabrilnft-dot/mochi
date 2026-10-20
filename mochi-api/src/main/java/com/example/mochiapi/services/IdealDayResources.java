package com.example.mochiapi.services;

import com.example.mochiapi.models.IdealDay;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import java.sql.*;

@Path("/ideal-day")
public class IdealDayResources {

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @GET
    @Produces("application/json")
    public Response getIdealDay(@Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer userId = (Integer) ctx.getProperty("userId");
                if(userId != null){
                    PreparedStatement statement = conn.prepareStatement("SELECT * FROM mochi_project_test4.idealday WHERE user_id = ?");
                    statement.setInt(1, userId);
                    ResultSet rs = statement.executeQuery();
                    IdealDay idealDay = new IdealDay();
                    while(rs.next()){
                        idealDay.setUserId(rs.getInt("user_id"));
                        idealDay.setMax_working_hours(rs.getInt("max_working_hours"));
                        idealDay.setWake_up_time(rs.getString("wake_up_time"));
                        idealDay.setBedtime(rs.getString("bedtime"));
                    }
                    return Response.status(Response.Status.OK)
                            .entity(idealDay)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded: " + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't retrieve ideal day")
                .build();
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response retrieveActivities(IdealDay idealDay, @Context ContainerRequestContext ctx){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                Integer user_id = (Integer) ctx.getProperty("userId");
                if(user_id > 0){
                    PreparedStatement statement = conn.prepareStatement("UPDATE mochi_project_test4.idealday SET max_working_hours = ?, wake_up_time = ?, bedtime = ? WHERE user_id = ?");
                    statement.setInt(1, idealDay.getMax_working_hours());
                    statement.setTime(2, Time.valueOf(idealDay.getWake_up_time()));
                    statement.setTime(3, Time.valueOf(idealDay.getBedtime()));
                    statement.setInt(4, user_id);
                    statement.executeUpdate();
                    idealDay.setUserId(user_id);
                    return Response.status(Response.Status.OK)
                            .entity(idealDay)
                            .build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded: " + e.getMessage())
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Couldn't edit activity")
                .build();
    }

}

