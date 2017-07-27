package uk.gov.dvla.f2d.web.session.exceptions;

public final class SessionAuthenticationFailedException extends Exception
{
    public SessionAuthenticationFailedException(final String message) {
        super(message);
    }
}
