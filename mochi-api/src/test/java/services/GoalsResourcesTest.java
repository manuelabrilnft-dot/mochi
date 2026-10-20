package services;

import com.example.mochiapi.services.GoalsResources;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.*;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class GoalsResourcesTest {
    private static String dbuser;
    private static String passwd;
    private GoalsResources goalsResources;
    private ContainerRequestContext mockContext;
    private static final int TEST_USER_ID = 9999;

    @BeforeClass
    public static void setupConnectionDetails() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dbuser = dotenv.get("DB_USER");
        passwd = dotenv.get("DB_PASSWORD");
    }

    @Before
    public void setUp() throws Exception {
        goalsResources = new GoalsResources();
        mockContext = Mockito.mock(ContainerRequestContext.class);
        cleanUp();
    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
    }

    private void cleanUp() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM mochi_project_test4.Person WHERE user_id = ?")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.executeUpdate();
        }
    }

    @Test
    public void testGetGoals_EmptyResultsSuccess() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO mochi_project_test4.Person (user_id, username, password) VALUES (?, ?, ?)")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.setString(2, "goals_user");
            stmt.setString(3, "pass");
            stmt.executeUpdate();
        }

        when(mockContext.getProperty("userId")).thenReturn(TEST_USER_ID);
        Response response = goalsResources.getGoals(mockContext, "2026-06-15");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteDailyGoal_NotFound() {
        when(mockContext.getProperty("userId")).thenReturn(TEST_USER_ID);
        Response response = goalsResources.removeDailyGoal("12345", mockContext);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}