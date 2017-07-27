/**
 * This is the Redis (Encrypted) session management implementation. It uses industry standard
 * encryption, with a default key length of 32 byte(s) and initialisation vector of 16 byte(s).
 * Data encoding is supported for plain text encryption, with data padding.
 *
 * @author James Bishop
 * @version 1.0
 */
package uk.gov.dvla.f2d.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import uk.gov.dvla.f2d.web.session.exceptions.SessionAuthenticationFailedException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotFoundException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionNotPersistedException;
import uk.gov.dvla.f2d.web.session.exceptions.SessionTechnicalException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

import static uk.gov.dvla.f2d.web.session.constants.SessionConstants.*;

class RedisSessionManagerEncryptedImpl extends AbstractSessionFacade implements ISessionFacade
{
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionManagerEncryptedImpl.class);

    private static final String SECRET_KEY_ALGORITHM        = "AES";
    private static final String CIPHER_IMPLEMENTATION       = "AES/CBC/PKCS5PADDING";

    private static final String SUPPORTED_ENCODING          = "UTF-8";

    private static final String INITIALISATION_VECTOR;
    private static final String ENCRYPTED_SECRET_KEY;

    static {
        logger.debug("<Static> initialising vectors and keys...");

        // Define our 16-byte initialisation vector data.
        byte[] vectorInitialisationByteArray = {
                0x32, 0x43, 0x18, 0x0A, 0x07, 0x64, 0x75, 0x0B,
                0x27, 0x65, 0x51, 0x72, 0x13, 0x0F, 0x39, 0x19
        };

        // Load and population our initialisation vector.
        INITIALISATION_VECTOR = new String(vectorInitialisationByteArray);

        // Retrieve the session encryption keys from config.
        ENCRYPTED_SECRET_KEY = SessionConfig.getInstance().getSessionEncryptionKey();
    }

    RedisSessionManagerEncryptedImpl(Optional<String> authenticate) {
        super(authenticate);
        logger.debug("RedisSessionManagerEncryptedImpl constructor.");
    }

    public void ping(final String sessionID) {
        super.ping(sessionID);
    }

    public String getData(final String sessionID) throws SessionNotFoundException {
        logger.debug("getData({})...", sessionID);
        try {
            Jedis connection = getConnection();

            Optional<String> encrypted = Optional.ofNullable(connection.get(sessionID));

            updateTimeoutInterval(sessionID);

            connection.close();

            if (!encrypted.isPresent() || encrypted.get().isEmpty()) {
                logger.debug("No data could be retrieved from session: [{}]...", sessionID);
                throw new SessionNotFoundException(SESSION_NOT_FOUND_ERROR);
            }

            return decrypt(encrypted.get());

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);

        } catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException ex) {
            logger.error("A technical error was raised: " + ex.toString());
            throw new SessionTechnicalException(ex);

        } catch(InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            logger.error("A technical error was raised: " + ex.toString());
            throw new SessionTechnicalException(ex);

        } catch(IOException ex) {
            logger.error("A technical error was raised: " + ex.toString());
            throw new SessionTechnicalException(ex);
        }
    }

    public void setData(final String sessionID, final String data) throws SessionNotPersistedException {
        logger.debug("setData({})...", sessionID);

        try {
            Jedis connection = getConnection();

            String encrypted = encrypt(data);

            String response = connection.set(sessionID, encrypted);
            logger.debug("Session response: [{}]", response);

            if(!response.equalsIgnoreCase(SESSION_RESPONSE_CODE)) {
                logger.warn("Session response was not successful, warning raised...");
                throw new SessionNotPersistedException(SESSION_FAILED_TO_PERSIST);
            }

            updateTimeoutInterval(sessionID);

            connection.close();

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);

        } catch(UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            logger.warn("Data could not be persisted to the current store: [{}]", sessionID);
            throw new SessionTechnicalException(ex);

        } catch(InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException ex) {
            logger.warn("Data could not be persisted to the current store: [{}]", sessionID);
            throw new SessionTechnicalException(ex);

        } catch(BadPaddingException | InvalidKeySpecException ex) {
            logger.warn("Data could not be persisted to the current store: [{}]", sessionID);
            throw new SessionTechnicalException(ex);
        }
    }

    public void invalidate(final String sessionID) {
        logger.debug("invalidate({})...", sessionID);
        try {
            Jedis connection = getConnection();

            Long response = connection.del(sessionID);
            logger.debug("response: ({})...", response);

            connection.close();

        } catch(SessionAuthenticationFailedException ex) {
            logger.error("Session failed to authenticate properly: " + ex.toString());
            throw new SessionTechnicalException(ex);
        }
    }

    private String encrypt(final String data)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeySpecException {

        logger.debug("encrypt(String)...");

        IvParameterSpec ivSpec = new IvParameterSpec(INITIALISATION_VECTOR.getBytes(SUPPORTED_ENCODING));

        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(CIPHER_IMPLEMENTATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes());

        return new BASE64Encoder().encode(encryptedData);
    }

    private String decrypt(final String encrypted)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeySpecException {

        logger.debug("decrypt(String)...");

        IvParameterSpec ivSpec = new IvParameterSpec(INITIALISATION_VECTOR.getBytes(SUPPORTED_ENCODING));

        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(CIPHER_IMPLEMENTATION);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] decodedData = new BASE64Decoder().decodeBuffer(encrypted);

        byte[] decryptedData = cipher.doFinal(decodedData);

        return new String(decryptedData);
    }

    private Key generateKey() throws UnsupportedEncodingException {
        logger.debug("generateKey()...");

        return new SecretKeySpec(ENCRYPTED_SECRET_KEY.getBytes(SUPPORTED_ENCODING), SECRET_KEY_ALGORITHM);
    }
}
