package io.pivotalservices.wiretaprouteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


/**
 * This Spring Boot application demonstrates a simple PCF Route Service that implements
 * the 'wiretap' integration pattern. When configured correctly, Requests and Responses
 * can be output as Log entries.
 * <p>
 * Sleuth stream has been added so that log entries contain correlation identifiers and
 * the entries themselves can be analysed as part of a call chain within Zipkin.
 */
@SpringBootApplication
public class RouteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteServiceApplication.class, args);
    }

    /**
     * This method configures a RestTemplate that has the necessary 'Interceptor' for logging.
     * The interceptor being added to the default RestTemplate causes requests and responses to
     * be logged to the logging framework.
     * <p>
     * A custom 'TrustEverythingClientHttpRequestFactory' has been used to allow this service to wiretap all kinds
     * of different services without security becoming too intrusive.
     *
     * @return
     */
    @Bean
    RestOperations restOperations() {

        // The BufferingClientHttpRequestFactory setup here is critical to reading the response without breaking it!
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new TrustEverythingClientHttpRequestFactory()));
        restTemplate.setErrorHandler(new NoErrorsResponseErrorHandler());

        // Add the interceptor that will handle the logging of Requests and Responses
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        interceptors.add(new RequestLoggingInterceptor());
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private static final class NoErrorsResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return false;
        }
    }

    private static final class TrustEverythingClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

        @Override
        protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
            HttpURLConnection connection = super.openConnection(url, proxy);

            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                httpsConnection.setSSLSocketFactory(getSslContext(new TrustEverythingTrustManager()).getSocketFactory());
                httpsConnection.setHostnameVerifier(new TrustEverythingHostNameVerifier());
            }

            return connection;
        }

        private static SSLContext getSslContext(TrustManager trustManager) {
            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{trustManager}, null);
                return sslContext;
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);

            }

        }
    }

    private static final class TrustEverythingHostNameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }

    }

    private static final class TrustEverythingTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

}
