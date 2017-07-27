package uk.co.blackcell.web.session.exceptions;

public final class SessionAuthenticationFailedException extends Exception
{
    public SessionAuthenticationFailedException(final String message) {
        super(message);
    }
}
