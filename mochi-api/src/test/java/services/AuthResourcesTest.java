package services;

import com.example.mochiapi.models.User;
import com.example.mochiapi.services.AuthResource;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.core.Response;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;

public class AuthResourcesTest {
    private static String dbuser;
    private static String passwd;
    private AuthResource authResource;
    private static final int TEST_USER_ID = 9999;

    @BeforeClass
    public static void setupConnectionDetails() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dbuser = dotenv.get("DB_USER");
        passwd = dotenv.get("DB_PASSWORD");
    }

    @Before
    public void setUp() throws Exception {
        authResource = new AuthResource();
        cleanUp();
    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
    }

    private void cleanUp() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM mochi_project_test4.Person WHERE user_id = ? OR username = ?")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.setString(2, "auth_test_user");
            stmt.executeUpdate();
        }
    }

    @Test
    public void testRegisterUser_InvalidPasswordPattern() {
        User user = new User();
        user.setUserId(TEST_USER_ID);
        user.setUsername("auth_test_user");
        user.setPassword("short");

        Response response = authResource.registerUser(user);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testLoginUser_InvalidCredentials() {
        User dummy = new User();
        dummy.setUsername("non_existent_username");
        dummy.setPassword("wrong");

        Response response = authResource.loginUser(dummy);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}