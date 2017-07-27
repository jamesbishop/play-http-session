/**
 * This is the session facade interface, and provided the basic template for all
 * session management implementations (persistence and encryption).
 *
 * @author James Bishop
 * @version 1.0
 */
package uk.gov.dvla.f2d.web.session;

import uk.gov.dvla.f2d.web.session.exceptions.SessionNotFoundException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotPersistedException;

interface ISessionFacade
{
    void ping(final String sessionID);

    String getData(final String sessionID)
        throws SessionNotFoundException;

    void setData(final String sessionID, final String data)
        throws SessionNotPersistedException;

    void invalidate(final String sessionID);
}
