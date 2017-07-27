/**
 * This is the Redis (non-encrypted) session management implementation. It delegates it's data
 * requests to the redis client implementation. No encryption is supported in this implementation.
 *
 * @author James Bishop
 * @version 1.0
 */
package uk.gov.dvla.f2d.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import uk.gov.dvla.f2d.web.session.exceptions.SessionAuthenticationFailedException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotFoundException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotPersistedException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionTechnicalException;

import java.util.Optional;

import static uk.gov.dvla.f2d.web.session.constants.SessionConstants.SESSION_NOT_FOUND_ERROR;

class RedisSessionManagerNonEncryptedImpl extends AbstractSessionFacade implements ISessionFacade
{
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionManagerNonEncryptedImpl.class);

    RedisSessionManagerNonEncryptedImpl(Optional<String> authenticate) {
        super(authenticate);
        logger.debug("RedisSessionManagerNonEncryptedImpl() constructor...");
    }

    public void ping(final String sessionID) {
        super.ping(sessionID);
    }

    public String getData(final String sessionID) throws SessionNotFoundException {
        logger.debug("getData({})...", sessionID);
        try {
            Jedis connection = getConnection();

            Optional<String> data = Optional.ofNullable(connection.get(sessionID));

            updateTimeoutInterval(sessionID);

            connection.close();

            if (!data.isPresent() || data.get().isEmpty()) {
                logger.debug("No data could be retrieved from session: [{}]...", sessionID);
                throw new SessionNotFoundException(SESSION_NOT_FOUND_ERROR);
            }

            return data.get();

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);
        }
    }

    public void setData(final String sessionID, final String data)throws SessionNotPersistedException {
        logger.debug("setData({})...", sessionID);
        try {
            Jedis connection = getConnection();

            connection.set(sessionID, data);

            updateTimeoutInterval(sessionID);

            connection.close();

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);
        }
    }

    public void invalidate(final String sessionID) {
        logger.debug("invalidate({})...", sessionID);
        try {
            Jedis connection = getConnection();

            connection.del(sessionID);

            connection.close();

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);
        }
    }
}
