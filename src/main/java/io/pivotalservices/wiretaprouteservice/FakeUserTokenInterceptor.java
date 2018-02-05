package io.pivotalservices.wiretaprouteservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.IOException;


/**
 * This class acts as an interceptor for Http Requests and Responses. It logs the content of the
 * Requests and Responses, acting as a basic 'wiretap' on communications coming in and out of
 * the controller.
 * <p>
 * As an interceptor, this class must be wired into the RestTemplate in order to get triggered.
 */
public class FakeUserTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeUserTokenInterceptor.class);

    private static final String X_AUTH_USER_HEADER_NAME = "x-auth-user";
    protected static final String X_AUTH_TOKEN_HEADER_NAME = "x-auth-token";

    public FakeUserTokenInterceptor() {
        LOGGER.info("Initialised the {} class.", this.getClass().getSimpleName());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        LOGGER.info("Checking for {} header in the request.", X_AUTH_TOKEN_HEADER_NAME);

        String token = request.getHeaders().getFirst(X_AUTH_TOKEN_HEADER_NAME);
        if (!StringUtils.isEmpty(token)) {
            LOGGER.info("{} is present, an has a value of: {}", X_AUTH_TOKEN_HEADER_NAME, token);

            LOGGER.info("Attempting to build a new {} header based on the token found ({}).", X_AUTH_USER_HEADER_NAME, token);
            HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
            token = getXAuthUserTokenFromRequest(token);

            LOGGER.info("Adding {} header to the request.", X_AUTH_USER_HEADER_NAME);
            HttpHeaders requestHeaders = wrapper.getHeaders();
            requestHeaders.set(X_AUTH_USER_HEADER_NAME, token);

            LOGGER.info("Removing {} header from the request.", X_AUTH_TOKEN_HEADER_NAME);
            requestHeaders.remove(X_AUTH_TOKEN_HEADER_NAME);

            HttpHeaders responseHeaders = wrapper.getHeaders();
            responseHeaders.set(X_AUTH_USER_HEADER_NAME, token);
            return execution.execute(wrapper, body);
        } else {
            LOGGER.warn("Header: {} was not found. No {} will be added to this request.", X_AUTH_TOKEN_HEADER_NAME, X_AUTH_USER_HEADER_NAME);
            return  execution.execute(request, body);
        }
    }


    private String getXAuthUserTokenFromRequest(String token) {
        LOGGER.trace("Attempting to getXAuthUserTokenFromRequest({})", token);
        String[] bits = StringUtils.tokenizeToStringArray(token, ":");
        LOGGER.trace("Split token into length {} and content: {}", bits.length, StringUtils.arrayToCommaDelimitedString(bits));
        token = XAuthUserTokenBuilder.getToken(bits[0], bits[1], bits[2]);
        LOGGER.info("Built {} header with a value of: {}", X_AUTH_USER_HEADER_NAME, token);
        return token;
    }
}