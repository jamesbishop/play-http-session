package uk.gov.dvla.f2d.web.session.exceptions;

public final class SessionNotPersistedException extends Exception
{
    public SessionNotPersistedException(final String message) {
        super(message);
    }
}
