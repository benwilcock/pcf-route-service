package io.pivotalservices.wiretaprouteservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * This class acts as an interceptor for Http Requests and Responses. It logs the content of the
 * Requests and Responses, acting as a basic 'wiretap' on communications coming in and out of
 * the controller.
 * <p>
 * As an interceptor, this class must be wired into the RestTemplate in order to get triggered.
 */
public class RequestLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private static int MAX_RESULT_SIZE = 524288;

    public RequestLoggingInterceptor() {
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }


    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        logger.info("REQUEST -> URI: {} METHOD: {} BODY: {}", request.getURI(), request.getMethod(), body);
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        String body = getBodyString(response);
        logger.info("RESPONSE -> S.CODE: {} S.TEXT: {} BODY: {}", response.getStatusCode(), response.getStatusText(), body);
    }

    private String getBodyString(ClientHttpResponse response) {
        try {
            //if (response != null && response.getBody() != null && isReadableResponse(response)) {
            if (response != null && response.getBody() != null) {
                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    //inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }

                return inputStringBuilder.toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}