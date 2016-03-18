package edu.kit.ipd.crowdcontrol.objectservice.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This class is responsible to generate/parse the JWT answer/response.
 * @author LeanderK
 * @version 1.0
 */
public class JWTHelper {
    private static final Logger logger = LoggerFactory.getLogger(JWTHelper.class);
    private final String secret;

    /**
     * creates the JWT-Helper
     * @param secret the base64 encoded jwtsecret to use
     */
    public JWTHelper(String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("jwtsecret must be set");
        }
        this.secret = secret;
    }

    /**
     * creates the JWT
     * @param workerId the workerID
     * @return the JWT string
     */
    public String generateJWT(int workerId) {
        Objects.requireNonNull(workerId);
        logger.debug("encoding JWT for workerID", workerId);
        return Jwts.builder()
                .setSubject(String.valueOf(workerId))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * returns the workerID form the JWT
     * @param jwt the jwt to parse
     * @return the workerID
     */
    public int getWorkerID(String jwt) {
        String subject = Jwts.parser().setSigningKey(secret).parseClaimsJws(jwt).getBody().getSubject();
        if (subject == null) {
            logger.error("subject is null");
            throw new IllegalArgumentException("subject is missing");
        }
        try {
            return Integer.parseInt(subject);
        } catch (NumberFormatException e) {
            logger.error(String.format("subject %s is not a number", subject));
            throw new IllegalArgumentException(String.format("subject %s is not a number", subject));
        }
    }
}
