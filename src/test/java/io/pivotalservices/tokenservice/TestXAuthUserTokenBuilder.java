package io.pivotalservices.tokenservice;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestXAuthUserTokenBuilder {

    private static final Map<String, Object> data = new HashMap<>();
    private static JwsParser jwsParser;
    private JWKSource<SecurityContext> publicKeySet;


    @Before
    public void setup() throws IOException, ParseException {
        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, null);
        data.put(XAuthUserTokenBuilder.EDO_KLID, null);
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, null);
        publicKeySet = XAuthUserTokenBuilder.loadPublicKeySet();
        jwsParser = new JwsParser(new DefaultJWTProcessor<>(), publicKeySet);
    }

    @Test
    public void testSplitString_withValidString(){
        String tokenString = "3:10:YUMA";
        Map<String, Object> tokenMap = XAuthUserTokenBuilder.buildTokenDataMapFromString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(4);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("3");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("10");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("YUMA");
    }

    @Test
    public void testSplitString_WithNullTokenString(){
        String tokenString = null;
        Map<String, Object> tokenMap = XAuthUserTokenBuilder.buildTokenDataMapFromString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(4);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("0");
    }

    @Test
    public void testSplitString_WithEmptyTokenString(){
        String tokenString = "";
        Map<String, Object> tokenMap = XAuthUserTokenBuilder.buildTokenDataMapFromString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(4);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EXPIRY_DATE)).isNotNull();
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EXPIRY_DATE)).isInstanceOf(Date.class);
    }

    @Test
    public void testGenerateSignedToken_withValidTokenString() throws ParseException, BadJOSEException, JOSEException {
        String tokenString = "3:10:YUMA";
        Map<String, Object> data = XAuthUserTokenBuilder.buildTokenDataMapFromString(tokenString);
        assertThat(data.entrySet().size()).isEqualTo(4);

        String myToken = XAuthUserTokenBuilder.generateSignedToken(data);

        JWTClaimsSet jwtClaimsSet = jwsParser.parse(myToken);
        assertThat(jwtClaimsSet.getClaim("authUserLevel")).isEqualTo(data.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL));
        assertThat(jwtClaimsSet.getClaim("edoUserId")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_USER_ID));
        assertThat(jwtClaimsSet.getClaim("edoKlid")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_KLID));
    }

    @Test
    public void testGetToken_withNullAuthLevel() throws ParseException, JOSEException, BadJOSEException {
        data.put(XAuthUserTokenBuilder.EDO_KLID, "dummy-klid");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "dummy-edo-user-id");

        String myToken = XAuthUserTokenBuilder.generateSignedToken(data);

        JWTClaimsSet jwtClaimsSet = jwsParser.parse(myToken);
        assertThat(jwtClaimsSet.getClaim("authUserLevel")).isNull();
        assertThat(jwtClaimsSet.getClaim("edoUserId")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_USER_ID));
        assertThat(jwtClaimsSet.getClaim("edoKlid")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_KLID));
    }

    @Test
    public void testGetToken_withNullEdoKlid() throws ParseException, JOSEException, BadJOSEException {
        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "dummy-auth-user-level");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "dummy-edo-user-id");

        String myToken = XAuthUserTokenBuilder.generateSignedToken(data);

        JWTClaimsSet jwtClaimsSet = jwsParser.parse(myToken);
        assertThat(jwtClaimsSet.getClaim("authUserLevel")).isEqualTo(data.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL));
        assertThat(jwtClaimsSet.getClaim("edoUserId")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_USER_ID));
        assertThat(jwtClaimsSet.getClaim("edoKlid")).isNull();
    }

    @Test
    public void testGetToken_withNullEdoUserId() throws ParseException, JOSEException, BadJOSEException {
        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "dummy-auth-user-level");
        data.put(XAuthUserTokenBuilder.EDO_KLID, "dummy-edo-klid");

        String myToken = XAuthUserTokenBuilder.generateSignedToken(data);

        JWTClaimsSet jwtClaimsSet = jwsParser.parse(myToken);
        assertThat(jwtClaimsSet.getClaim("authUserLevel")).isEqualTo(data.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL));
        assertThat(jwtClaimsSet.getClaim("edoKlid")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_KLID));
        assertThat(jwtClaimsSet.getClaim("edoUserId")).isNull();
    }


}
