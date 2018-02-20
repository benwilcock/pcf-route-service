package io.pivotalservices.tokenservice;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class JwsParser {

    private static final JWSAlgorithm JWS_ALGORITHM = JWSAlgorithm.RS512;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwsParser.class);

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final JWKSource<SecurityContext> jwkSource;

    public JwsParser(ConfigurableJWTProcessor<SecurityContext> processor, JWKSource<SecurityContext> jwkSource) {
        this.jwtProcessor = processor;
        this.jwkSource = jwkSource;
        initializeProcessor();
    }

    public JWTClaimsSet parse(final String authUserHeader) throws ParseException, JOSEException, BadJOSEException {
        final JWTClaimsSet claimsSet = jwtProcessor.process(authUserHeader, null);
        LOGGER.debug("Parsed the (signed) JWT Token to: {}", claimsSet.toJSONObject());
        return claimsSet;
    }

    private void initializeProcessor() {
        final JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWS_ALGORITHM, jwkSource);
        jwtProcessor.setJWSKeySelector(keySelector);

    }
}
