package org.konceptosociala.netpkt.warehouse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "very_secret_key";
    private static final Algorithm ALGO = Algorithm.HMAC256(SECRET);
    private static final long EXPIRATION_MS = 3600_000;

    public static String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(ALGO);
    }

    public static boolean isValid(String token) {
        try {
            JWTVerifier verifier = JWT.require(ALGO).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
