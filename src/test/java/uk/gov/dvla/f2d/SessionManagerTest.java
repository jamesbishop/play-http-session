package uk.gov.dvla.f2d;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.gov.dvla.f2d.web.session.SessionManager;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for Session Management component.
 */
public class SessionManagerTest extends TestCase
{
    /**
     * Create the test case
     * @param testName name of the test case
     */
    public SessionManagerTest(String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( SessionManagerTest.class );
    }

    /**
     * Allow us to turn of mock tests and use real data (development only)
     * @return A SessionManager for inspecting and calling delegate methods.
     */
    private SessionManager instantiate() {
        return mock(SessionManager.class);
    }

    /**
     * Unit test to ensure the a valid session identifier was generated. The session
     * identifier is a 36-character unique identifier based on the UUID pattern.
     */
    public void testSessionIdentifierGenerated() {
        final String mySession = SessionManager.generateID();

        assertNotNull(mySession);
        assertEquals(mySession.length(), 36);
    }

    /**
     * This test checks to see whether the persistent store is running.
     */
    public void testSessionStoreIsAlive() {
        final String mySession = SessionManager.generateID();

        SessionManager manager = instantiate();
        manager.ping(mySession);
    }

    /**
     * This test is designed to prove our session persistence framework using our
     * custom session manager. We have created two separate instances of the
     * <code>SessionManager</code> class, to prove caching and state are managed
     * across instances. The default behaviour is illustrated here.
     */
    public void testDefaultSessionManagement() throws Exception {
        // Define the data that we want to save
        final String mySession = SessionManager.generateID();
        final String myAppData = "This is my app data and I want to feel safe :-(";

        SessionManager manager = instantiate();

        // Persist our session data.
        manager.save(mySession, myAppData);

        // Retrieve our session data.
        when(manager.find(mySession)).thenReturn(myAppData);

        // Retrieve the data from the session.
        String myData = manager.find(mySession);

        assertNotNull(myData);
        assertEquals(myAppData.length(), myData.length());

        // Check that what we persisted, is what we found.
        assertEquals(myAppData, myData);
    }

    /**
     * This test is designed to prove our session persistence framework using our
     * custom session manager. We have created two separate instances of the
     * <code>SessionManager</code> class, to prove caching and state are managed
     * across instances. The null data (expired sessions) are illustrated here.
     */
    public void testGetDataUsingNullValues() throws Exception {
        // Define the data that we want to save
        final String mySession = SessionManager.generateID();
        final String myAppData = null;

        // Persist our session data.
        SessionManager manager = instantiate();
        manager.save(mySession, myAppData);

        // Retrieve our session data.
        when(manager.find(mySession)).thenReturn(myAppData);

        String myData = manager.find(mySession);

        // Check that what we persisted, is what we found.
        assertNull(myData);
    }

    /**
     * Test to ensure that a session is disposed of properly once it's been invalidated.
     */
    public void testSessionInvalidatedSuccessfully() throws Exception {
        // Define the data that we want to save
        final String mySession = SessionManager.generateID();
        final String myAppData = "This is my app data and I want to feel safe :-(";

        // Persist our session data.
        SessionManager manager = instantiate();
        manager.save(mySession, myAppData);

        // Now invalidate our session.
        manager.invalidate(mySession);

        // Retrieve our session data.
        when(manager.find(mySession)).thenReturn("");

        // Now attempt to lookup our session data.
        String myFoundData = manager.find(mySession);

        assertNotNull(myFoundData);
    }
}
