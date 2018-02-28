package io.pivotalservices.tokenservice;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class XAuthUserTokenBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XAuthUserTokenBuilder.class);
    private static final JWSAlgorithm JWS_ALGORITHM = JWSAlgorithm.RS512;
    public static final String AUTH_USER_LEVEL = "AUTH_USER_LEVEL";
    public static final String EDO_KLID = "EDO_KLID";
    public static final String EDO_USER_ID = "EDO_USER_ID";
    public static final String EXPIRY_DATE = "EXPIRY_DATE";
    protected static final String DEFAULT_PRIVATE_KEY = "f66e26b3-fbd0-4c0e-8113-d0c7f913978c";

    public static String generateSignedToken(Map<String, Object> data) {
        return generateSignedToken(data, DEFAULT_PRIVATE_KEY, JWS_ALGORITHM);
    }

    private static String generateSignedToken(Map<String, Object> data, String keyId, JWSAlgorithm algorithm) {

        LOGGER.debug("Generating a (signed) JWT token for the EdoUserId: {}", data.get(XAuthUserTokenBuilder.EDO_USER_ID));

        try {
            final byte[] keyBytes = Base64.getDecoder().decode(
                    FileUtil.readAsString(
                            new File(XAuthUserTokenBuilder.class.getClassLoader().getResource("jwk-private-key").getFile())
                    )
            );

            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

            JWSSigner signer = new RSASSASigner(privateKey);

            final SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(algorithm)
                    .keyID(keyId)
                    .build(), getClaimSet(data));

            signedJWT.sign(signer);
            String signedToken = signedJWT.serialize();

            LOGGER.debug("Generated the (signed) JWT token: {}", signedToken);
            return signedToken;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JWTClaimsSet getClaimSet(Map<String, Object> data) {
        Date expirationDate = (Date) data.get(EXPIRY_DATE);

        if (null == expirationDate) {
            expirationDate = Date.from(ZonedDateTime.now().plusSeconds(180).toInstant());
        }

        return new JWTClaimsSet.Builder()
                .issueTime(Date.from(expirationDate.toInstant().minusSeconds(300)))
                .expirationTime(expirationDate)
                .claim("authUserId", "user-id")
                .claim("authUserType", "CUSTOMER")
                .claim("authUserLevel", (String) data.get(AUTH_USER_LEVEL))
                .claim("authTicket", "23fadf2309aoiijassegg")
                .claim("siebelCustomerRelationId", "000000011111111")
                .claim("siebelUserRelationId", "000000011111111")
                .claim("edoKlid", (String) data.get(EDO_KLID))
                .claim("edoAgreementId", "000001143")
                .claim("edoUserId", (String) data.get(EDO_USER_ID))
                .claim("sources", new String[]{"RASS", "TA"})
                .build();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildTokenDataMapFromString(String token) {
        String[] bits = StringUtils.commaDelimitedListToStringArray("0,0,0");
        if (!StringUtils.isEmpty(token)) {
            bits = StringUtils.tokenizeToStringArray(token, ":");
        }

        if (bits.length != 3) {
            throw new IllegalArgumentException("The token was malformed.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put(AUTH_USER_LEVEL, bits[0]);
        data.put(EDO_KLID, bits[1]);
        data.put(EDO_USER_ID, bits[2]);
        data.put(EXPIRY_DATE, Date.from(ZonedDateTime.now().plusSeconds(180).toInstant()));

        return data;
    }


    protected static JWKSource<SecurityContext> loadPublicKeySet() throws IOException, ParseException {
        JWKSource<SecurityContext> javaWebKeySet = new ImmutableJWKSet<>(JWKSet.load(new ClassPathResource("jwk-local-key-set.json").getFile()));
        return javaWebKeySet;
    }


}
