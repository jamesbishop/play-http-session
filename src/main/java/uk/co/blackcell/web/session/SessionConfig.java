package uk.co.blackcell.web.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

class SessionConfig
{
    private static final String INTERNAL_SESSION_CONFIG         = "session.properties";
    private static final String EXTERNAL_SESSION_CONFIG         = "session.config.file";

    private static final String SESSION_ENCRYPTION_KEY          = "session.encryption.key";
    private static final String SESSION_AUTHENTICATION_KEY      = "session.authentication.key";

    private static final String SESSION_TIMEOUT_SECONDS         = "session.timeout.seconds";

    private static final String REDIS_HOST_CONFIG_KEY           = "redis.server.host";
    private static final String REDIS_PORT_CONFIG_KEY           = "redis.server.port";
    private static final String REDIS_SESSION_ENCRYPTION_KEY    = "redis.session.encryption";

    private static final String REDIS_SENTINEL_ENABLED_KEY      = "redis.sentinel.enabled";
    private static final String REDIS_SENTINEL_MASTER_KEY       = "redis.sentinel.master";
    private static final String REDIS_SENTINEL_LIST_KEY         = "redis.sentinel.list";

    private static final String REDIS_HOST_DEFAULT              = "localhost";
    private static final String REDIS_PORT_DEFAULT              = "6379";
    private static final String REDIS_ENCRYPTION_DEFAULT        = "true";

    private static final String REDIS_ENABLED_DEFAULT           = "false";
    private static final String REDIS_MASTER_DEFAULT            = "mymaster";
    private static final String REDIS_LIST_DEFAULT              = "127.0.0.1:26379";

    private static final String REDIS_LIST_SEPARATOR            = ",";

    private static final Logger logger = LoggerFactory.getLogger(SessionConfig.class);

    private static final SessionConfig INSTANCE = new SessionConfig();

    private Properties properties;

    private SessionConfig() {
        logger.debug("SessionConfig constructor.");
        try {
            initialise();

        } catch(IOException ex) {
            logger.error(ex.toString());
            throw new RuntimeException(ex);
        }
    }

    static synchronized SessionConfig getInstance() {
        return INSTANCE;
    }

    private void initialise() throws IOException {
        logger.debug("initialise()...");

        String resourceToLoad = System.getProperty(EXTERNAL_SESSION_CONFIG, null);

        InputStream configStream;
        if(resourceToLoad != null) {
            configStream = new FileInputStream(resourceToLoad);
        } else {
            configStream = getClass().getClassLoader().getResource(INTERNAL_SESSION_CONFIG).openStream();
        }

        properties = new Properties();
        properties.load(configStream);
    }

    public String getRedisServerHost() {
        return properties.getProperty(REDIS_HOST_CONFIG_KEY, REDIS_HOST_DEFAULT);
    }

    public int getRedisServerPort() {
        return Integer.parseInt(properties.getProperty(REDIS_PORT_CONFIG_KEY, REDIS_PORT_DEFAULT));
    }

    public boolean isSessionEncryptionEnabled() {
        return Boolean.parseBoolean(properties.getProperty(REDIS_SESSION_ENCRYPTION_KEY, REDIS_ENCRYPTION_DEFAULT));
    }

    public int getSessionTimeoutInSeconds() {
        return Integer.parseInt(properties.getProperty(SESSION_TIMEOUT_SECONDS));
    }

    public String getSessionEncryptionKey() {
        return properties.getProperty(SESSION_ENCRYPTION_KEY);
    }

    public String getSessionAuthenticationKey() {
        return properties.getProperty(SESSION_AUTHENTICATION_KEY);
    }

    boolean isSentinelEnabled() {
        return Boolean.parseBoolean(properties.getProperty(REDIS_SENTINEL_ENABLED_KEY, REDIS_ENABLED_DEFAULT));
    }

    String getMaster() {
        return properties.getProperty(REDIS_SENTINEL_MASTER_KEY, REDIS_MASTER_DEFAULT);
    }

    Set<String> getSentinelList() {
        Set<String> list = new TreeSet<>();
        String sentinels = properties.getProperty(REDIS_SENTINEL_LIST_KEY, REDIS_LIST_DEFAULT);
        for(String instance : sentinels.split(REDIS_LIST_SEPARATOR)) {
            list.add(instance);
        }
        return list;
    }

}
