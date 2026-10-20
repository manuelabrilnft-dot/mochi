package services;

import com.example.mochiapi.services.ActivityResources;
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

public class ActivityResourcesTest {
    private static String dbuser;
    private static String passwd;
    private ActivityResources activityResources;
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
        activityResources = new ActivityResources();
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
    public void testRetrieveActivities_InvalidQueryParameters() {
        when(mockContext.getProperty("userId")).thenReturn(TEST_USER_ID);
        Response response = activityResources.retrieveActivities("none", "none", mockContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteActivity_NotFound() {
        Response response = activityResources.removeActivity("999999");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}