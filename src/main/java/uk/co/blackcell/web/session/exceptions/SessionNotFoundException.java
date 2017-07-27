package uk.co.blackcell.web.session.exceptions;

public final class SessionNotFoundException extends Exception
{
    public SessionNotFoundException(final String message) {
        super(message);
    }
}
