package uk.co.blackcell.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.blackcell.web.session.exceptions.SessionAuthenticationFailedException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import static uk.co.blackcell.web.session.constants.SessionConstants.*;

public final class SessionAuthenticator
{
    private static final Logger logger = LoggerFactory.getLogger(SessionAuthenticator.class);

    private SessionConfig config = SessionConfig.getInstance();

    private Optional<PrivateKey> key;

    SessionAuthenticator(final String keyStore) {
        logger.debug("constructor() called...");
        initialise(keyStore);
    }

    private void initialise(final String keyStore) throws IllegalArgumentException {
        logger.debug("initialise() called...");
        try {
            File keyStoreFile = new File(keyStore + FORWARD_SLASH + PRIVATE_KEY_FILE);
            byte[] keyBytes = Files.readAllBytes(keyStoreFile.toPath());

            logger.debug("key initialising with {} byte(s)...", keyBytes.length);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);

            logger.debug("key initialised successfully, generating...");

            key = Optional.of(keyFactory.generatePrivate(keySpec));

        } catch(IOException | NoSuchAlgorithmException | InvalidKeySpecException ex) {
            logger.error("A key store was provided, but failed to initialise: ", ex);
            throw new IllegalArgumentException(SESSION_BAD_CONFIGURATION);
        }
    }

    public void authenticate() throws SessionAuthenticationFailedException {
        logger.debug("authenticate() called...");
        try {
            Optional<String> param = Optional.ofNullable(config.getSessionAuthenticationKey());

            if(!param.isPresent() || param.get().isEmpty()) {
                logger.error("Session management was badly configured: [{}]", param);
                throw new SessionAuthenticationFailedException(SESSION_BAD_CONFIGURATION);
            }

            decrypt(param.get());

        } catch(IllegalArgumentException ex) {
            logger.error("Session failed to authenticate properly: ", ex);
            throw new SessionAuthenticationFailedException(SESSION_AUTHENTICATION_ERROR);
        }
    }

    public String getAuthenticationKey() {
        logger.debug("getAuthenticationKey() called...");
        return decrypt(config.getSessionAuthenticationKey());
    }

    private String decrypt(final String passPhrase) {
        logger.debug("decrypt() called...");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key.get());

            return new String(cipher.doFinal(Base64.getDecoder().decode(passPhrase)));

        } catch(NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException ex) {
            throw new IllegalArgumentException(ex);

        } catch(BadPaddingException | InvalidKeyException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
