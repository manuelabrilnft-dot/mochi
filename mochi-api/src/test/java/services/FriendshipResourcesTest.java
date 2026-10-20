package services;

import com.example.mochiapi.services.FriendshipResources;
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

public class FriendshipResourcesTest {
    private static String dbuser;
    private static String passwd;
    private FriendshipResources friendshipResources;
    private ContainerRequestContext mockContext;
    private static final int USER_1 = 9998;
    private static final int USER_2 = 9999;

    @BeforeClass
    public static void setupConnectionDetails() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dbuser = dotenv.get("DB_USER");
        passwd = dotenv.get("DB_PASSWORD");
    }

    @Before
    public void setUp() throws Exception {
        friendshipResources = new FriendshipResources();
        mockContext = Mockito.mock(ContainerRequestContext.class);
        cleanUsers();
    }

    @After
    public void tearDown() throws Exception {
        cleanUsers();
    }

    private void cleanUsers() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM mochi_project_test4.Person WHERE user_id IN (?, ?)")) {
            stmt.setInt(1, USER_1);
            stmt.setInt(2, USER_2);
            stmt.executeUpdate();
        }
    }

    @Test
    public void testSendFriendRequest_InvalidCriteria() {
        when(mockContext.getProperty("userId")).thenReturn(USER_1);
        Response response = friendshipResources.sendFriendRequest(USER_1, mockContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRemoveFriend_NotFound() {
        when(mockContext.getProperty("userId")).thenReturn(USER_1);
        Response response = friendshipResources.removeFriend(USER_2, mockContext);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}