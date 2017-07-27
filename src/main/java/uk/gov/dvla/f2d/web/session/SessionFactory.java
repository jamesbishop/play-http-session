/**
 * This is the session factory class, which instantiates the preferred type of
 * implementation we require for our data caching and encryption.
 *
 * @author James Bishop
 * @version 1.0
 */
package uk.gov.dvla.f2d.web.session;

import java.util.Optional;

class SessionFactory
{
    private SessionFactory() {
        super();
    }

    static synchronized SessionFactory getFactory() {
        return new SessionFactory();
    }

    private Boolean isSessionEncrypted() {
        return SessionConfig.getInstance().isSessionEncryptionEnabled();
    }

    private ISessionFacade getEncryptedSession(Optional<String> keyPath) {
        return new RedisSessionManagerEncryptedImpl(keyPath);
    }

    private ISessionFacade getUnencryptedSession(Optional<String> keyPath) {
        return new RedisSessionManagerNonEncryptedImpl(keyPath);
    }

    private ISessionFacade getRedisSession(Optional<String> keyPath) {
        return (isSessionEncrypted()) ? getEncryptedSession(keyPath) : getUnencryptedSession(keyPath);
    }

    public ISessionFacade getSession(Optional<String> keyPath) {
        return getRedisSession(keyPath);
    }
}
