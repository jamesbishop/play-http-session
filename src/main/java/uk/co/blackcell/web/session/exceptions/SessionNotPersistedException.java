package uk.co.blackcell.web.session.exceptions;

public final class SessionNotPersistedException extends Exception
{
    public SessionNotPersistedException(final String message) {
        super(message);
    }
}
