package services;

import com.example.mochiapi.models.IdealDay;
import com.example.mochiapi.services.IdealDayResources;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class IdealDayResourcesTest {

    private static String dbuser;
    private static String passwd;
    private IdealDayResources idealDayResources;
    private ContainerRequestContext mockContext;
    private static final int TEST_USER_ID = 8888;

    @BeforeClass
    public static void setupConnectionDetails() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dbuser = dotenv.get("DB_USER");
        passwd = dotenv.get("DB_PASSWORD");
    }

    @Before
    public void setUp() throws Exception {
        idealDayResources = new IdealDayResources();
        mockContext = Mockito.mock(ContainerRequestContext.class);
        cleanTestUser();
    }

    @After
    public void tearDown() throws Exception {
        cleanTestUser();
    }

    private void cleanTestUser() throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd)) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM mochi_project_test4.Person WHERE user_id = ?")) {
                stmt.setInt(1, TEST_USER_ID);
                stmt.executeUpdate();
            }
        }
    }

    private void insertTestPerson() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://bronto.ewi.utwente.nl/" + dbuser, dbuser, passwd);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO mochi_project_test4.Person (user_id, username, password) VALUES (?, ?, ?)")) {
            stmt.setInt(1, TEST_USER_ID);
            stmt.setString(2, "test_user_" + TEST_USER_ID);
            stmt.setString(3, "securepassword");
            stmt.executeUpdate();
        }
    }

    @Test
    public void testGetIdealDay_UserExists() throws Exception {
        // Arrange
        insertTestPerson();
        when(mockContext.getProperty("userId")).thenReturn(TEST_USER_ID);
        Response response = idealDayResources.getIdealDay(mockContext);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        IdealDay result = (IdealDay) response.getEntity();
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(8, result.getMax_working_hours());
        assertTrue(result.getWake_up_time().contains("07:00:00"));
    }

    @Test
    public void testRetrieveActivities_UpdateSuccess() throws Exception {
        insertTestPerson();
        when(mockContext.getProperty("userId")).thenReturn(TEST_USER_ID);

        IdealDay updatePayload = new IdealDay();
        updatePayload.setMax_working_hours(6);
        updatePayload.setWake_up_time("09:30:00");
        updatePayload.setBedtime("23:45:00");

        Response response = idealDayResources.retrieveActivities(updatePayload, mockContext);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        IdealDay result = (IdealDay) response.getEntity();
        assertNotNull(result);
        assertEquals(6, result.getMax_working_hours());
        assertEquals(TEST_USER_ID, result.getUserId());
    }

    @Test
    public void testGetIdealDay_UserDoesNotExistInDb() {
        when(mockContext.getProperty("userId")).thenReturn(999999);
        Response response = idealDayResources.getIdealDay(mockContext);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        IdealDay result = (IdealDay) response.getEntity();
        assertEquals(0, result.getUserId());
    }

    @Test
    public void testGetIdealDay_NullUserContext() {
        when(mockContext.getProperty("userId")).thenReturn(null);
        Response response = idealDayResources.getIdealDay(mockContext);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}