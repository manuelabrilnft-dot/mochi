package com.example.mochiapi.services;

import com.example.mochiapi.models.User;
import com.example.mochiapi.repositories.dbRepository;
import com.example.mochiapi.security.TokenInfo;
import com.example.mochiapi.security.TokenManager;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Path("/auth")
public class AuthResource {
//    private DataSource getDataSource() throws NamingException {
//        return (DataSource)
//                new InitialContext()
//                        .lookup("java:comp/.env/jdbc/MyDB");
//    }

    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
    static String dbuser = dotenv.get("DB_USER");
    static String passwd = dotenv.get("DB_PASSWORD");

    @POST
    @Path("/register")
    @Consumes("application/json")
    public Response registerUser(User user){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                if(user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() >= 8 && user.getPassword().matches(".*\\d.*") && user.getPassword().matches(".*[A-Z].*")){
                    conn.setAutoCommit(false);
                    int userCount = dbRepository.lastUserId(conn);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO mochi_project_test4.person(user_id, username, password, level, streak, xp, layout, background)"
                            + "VALUES (?, ?, ?, 1, 0, 0, 1, 1);");
                    statement.setInt(1, userCount + 1);
                    statement.setString(2, user.getUsername());
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                    statement.setString(3, hashedPassword);
                    statement.executeUpdate();
                    conn.commit();
                    conn.setAutoCommit(true);
                    String token = TokenManager.addToken(userCount + 1);
                    NewCookie cookie = new NewCookie(
                            "session_token",
                            token,
                            "/",
                            null,
                            "Authentication session",
                            30 * 60,
                            false,
                            true
                    );
                    return Response.status(Response.Status.OK).cookie(cookie).entity("Registration successful").build();
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
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Invalid username or password")
                .build();
    }


    @POST
    @Path("/login")
    @Consumes("application/json")
    @Produces("application/json")
    public Response loginUser(User user){
        try{
            Class.forName("org.postgresql.Driver");
            try(Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser,
                    dbuser, passwd)){
                if(user.getPassword() != null && !user.getPassword().isEmpty()) {
                    PreparedStatement statement = conn.prepareStatement("SELECT user_id, password FROM mochi_project_test4.person WHERE username = ?");
                    statement.setString(1, user.getUsername());
                    ResultSet rs = statement.executeQuery();
                    if (!rs.isBeforeFirst()) {
                        return Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity("Couldn't log in")
                                .build();
                    }
                    String storedHash;
                    int userId = -1;
                    while(rs.next()){
                        storedHash = rs.getString("password");
                        if (!BCrypt.checkpw(user.getPassword(), storedHash)) {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity("Couldn't log in")
                                    .build();
                        }else{
                            userId = rs.getInt("user_id");
                        }
                    }

                    rs.close();
                    String token = TokenManager.addToken(userId);
                    NewCookie cookie = new NewCookie.Builder("session_token")
                            .value(token)
                            .path("/")
                            .comment("Authentication session")
                            .maxAge(30 * 60)
                            .secure(false)
                            .httpOnly(true)
                            .build();
                    return Response.status(Response.Status.OK).cookie(cookie).entity("Login successful").build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.getMessage())
                        .build();
            }
        }catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not loaded");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("JDBC driver not loaded " + e.getMessage())
                    .build();
        }
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Couldn't log in")
                .build();
    }

    @POST
    @Path("/auth/logout")
    public Response logoutUser(@Context ContainerRequestContext ctx) {
        String token = ctx.getCookies().get("session_token").getValue();
        NewCookie cookie = new NewCookie.Builder("session_token")
                .value(token)
                .path("/")
                .comment("Logout session")
                .maxAge(0)
                .secure(false)
                .httpOnly(true)
                .build();
        TokenManager.deleteToken(token);
        return Response.status(Response.Status.OK).cookie(cookie).build();
    }

    @GET
    @Path("/me")
    public Response getMe(@Context ContainerRequestContext ctx) {
        String token = ctx.getCookies().get("session_token").getValue();
        TokenInfo info = TokenManager.getInfo(token);
        return Response.status(Response.Status.OK).entity(info.getUser_id()).build();
    }

}
