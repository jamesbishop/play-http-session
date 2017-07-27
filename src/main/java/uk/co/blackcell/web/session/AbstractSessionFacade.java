package uk.co.blackcell.web.session;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Pool;
import uk.co.blackcell.web.session.exceptions.SessionAuthenticationFailedException;
import uk.co.blackcell.web.session.exceptions.SessionTechnicalException;

import java.util.Optional;

abstract class AbstractSessionFacade
{
    protected static final String PING_RESPONSE_CODE          = "PONG";

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionFacade.class);

    private static Pool pool;

    private SessionConfig config = SessionConfig.getInstance();

    private Optional<String> keyPath;

    AbstractSessionFacade(Optional<String> keyPath) {
        logger.debug("AbstractSessionFacade constructor.");

        initialise(keyPath);
    }

    private void initialise(Optional<String> keyPath) {
        logger.debug("Perform abstract initialisation of connection pooling...");

        this.keyPath = keyPath;

        if(pool == null) {
            logger.debug("Redis Sentinel enabled set to {}.", config.isSentinelEnabled());

            if (config.isSentinelEnabled()) {
                GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
                pool = new JedisSentinelPool(config.getMaster(), config.getSentinelList(), poolConfig);

                HostAndPort hostAndPort = ((JedisSentinelPool)pool).getCurrentHostMaster();

                String host = hostAndPort.getHost();
                int port = hostAndPort.getPort();

                logger.debug("Sentinel Master on Host:{}, Port:{}...", host, port);

            } else {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(10000);
                poolConfig.setMaxIdle(10);
                poolConfig.setMinIdle(1);
                poolConfig.setMaxWaitMillis(30000);

                String host = config.getRedisServerHost();
                int port = config.getRedisServerPort();

                pool = new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, null);
            }
        }
    }

    Jedis getConnection() throws SessionAuthenticationFailedException {
        logger.debug("Obtain connection from {}...", pool.getClass().getName());
        Jedis resource = (Jedis)pool.getResource();

        if (keyPath.isPresent() && !keyPath.get().isEmpty()) {
            SessionAuthenticator session = new SessionAuthenticator(keyPath.get());
            session.authenticate();

            resource.auth(session.getAuthenticationKey());
        }

        return resource;
    }

    void ping(final String sessionID)
        throws SessionTechnicalException {

        logger.debug("ping({})...", sessionID);
        try {
            Jedis connection = getConnection();

            String response = connection.ping();

            connection.close();

            if(!(response.equalsIgnoreCase(PING_RESPONSE_CODE))) {
                throw new SessionTechnicalException("Redis Server (returned): "+response);
            }

        } catch(Exception ex) {
            throw new SessionTechnicalException(ex);
        }
    }

    long updateTimeoutInterval(final String sessionID)
        throws SessionTechnicalException {

        logger.debug("updateTimeoutInterval({})...", sessionID);
        try {
            Jedis connection = getConnection();

            long result = connection.expire(sessionID, config.getSessionTimeoutInSeconds());

            connection.close();

            return result;

        } catch(Exception ex) {
            throw new SessionTechnicalException(ex);
        }
    }
}
