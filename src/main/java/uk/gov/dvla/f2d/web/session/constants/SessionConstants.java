package uk.gov.dvla.f2d.web.session.constants;

public final class SessionConstants
{
    public static final String ALGORITHM                        = "RSA";

    public static final String PRIVATE_KEY_FILE                 = "sm-auth-private.key";
    public static final String PUBLIC_KEY_FILE                  = "sm-auth-public.key";

    public static final String SESSION_FAILED_TO_PERSIST        = "Failed to save session to persisted store.";
    public static final String SESSION_NOT_FOUND_ERROR          = "Session could not be found in persistent store.";
    public static final String SESSION_AUTHENTICATION_ERROR     = "Failed to authenticate using supplied credentials.";
    public static final String SESSION_BAD_CONFIGURATION        = "Session management was badly configured.";

    public static final String SESSION_RESPONSE_CODE            = "OK";

    public static final String FORWARD_SLASH                    = "/";
}
