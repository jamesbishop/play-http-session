/**
 * This is the main session management class, which is uses in the absence of any
 * established session caching bundled within the play framework. The session
 * manager abstracts out all data encryption and data persistence/management.
 *
 * @author James Bishop
 * @version 1.0
 */
package uk.gov.dvla.f2d.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotFoundException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotPersistedException;

import java.util.Optional;
import java.util.UUID;

public class SessionManager
{
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private ISessionFacade session;

    public SessionManager() {
        logger.debug("SessionManager() constructor.");

        initialise(Optional.empty());
    }

    public SessionManager(final String keyPath) {
        logger.debug("SessionManager({}) constructor.", keyPath);

        initialise(Optional.of(keyPath));
    }

    private void initialise(Optional<String> keyPath) {
        logger.debug("Initialise({}) method called...", keyPath);

        session = SessionFactory.getFactory().getSession(keyPath);

    }

    public static String generateID() {
        return UUID.randomUUID().toString();
    }

    public void ping(final String sessionID) {
        session.ping(sessionID);
    }

    public void save(final String sessionID, final String data) throws SessionNotPersistedException {
        session.setData(sessionID, data);
    }

    public String find(final String sessionID) throws SessionNotFoundException {
        return session.getData(sessionID);
    }

    public void invalidate(final String sessionID) {
        session.invalidate(sessionID);
    }
}
