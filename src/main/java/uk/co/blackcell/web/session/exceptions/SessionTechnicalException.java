package uk.co.blackcell.web.session.exceptions;

public final class SessionTechnicalException extends RuntimeException
{
    public SessionTechnicalException(Throwable ex) {
        super(ex);
    }

    public SessionTechnicalException(String message) {
        super(message);
    }
}
