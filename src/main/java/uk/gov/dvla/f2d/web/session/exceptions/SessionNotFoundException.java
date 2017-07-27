package uk.gov.dvla.f2d.web.session.exceptions;

public final class SessionNotFoundException extends Exception
{
    public SessionNotFoundException(final String message) {
        super(message);
    }
}
