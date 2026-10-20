package services;

import com.example.mochiapi.services.AchievementResources;
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

public class AchievementResourcesTest {
    private static String dbuser;
    private static String passwd;
    private AchievementResources achievementResources;
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
        achievementResources = new AchievementResources();
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
    public void testGetAchievements_NullUserContext() {
        when(mockContext.getProperty("userId")).thenReturn(null);
        Response response = achievementResources.getAchievements(mockContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}