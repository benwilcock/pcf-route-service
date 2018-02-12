/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotalservices.tokenservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The application needs at least one controller in order for the Requests and Responses to be intercepted.
 * This controller is mapped to all endpoints, and so gets triggered no matter what the incoming request as long as
 * it contains the desired headers. It is possible to add further logging here.
 */
@RestController
final class CatchAllController {

    private static final String FORWARDED_URL = "X-CF-Forwarded-Url";

    private static final String PROXY_METADATA = "X-CF-Proxy-Metadata";

    private static final String PROXY_SIGNATURE = "X-CF-Proxy-Signature";

    private static final String X_AUTH_USER_HEADER_NAME = "x-auth-user";

    private static final String X_AUTH_TOKEN_HEADER_NAME = "x-auth-token";

    private final static Logger LOGGER = LoggerFactory.getLogger(CatchAllController.class);

    private final PassthruService passthruService;

    @Autowired
    public CatchAllController(PassthruService passthruService) {
        this.passthruService = passthruService;
    }


    @RequestMapping(headers = {FORWARDED_URL, PROXY_METADATA, PROXY_SIGNATURE})
    public ResponseEntity<?> service(RequestEntity<byte[]> incoming) {
        this.LOGGER.info("Incoming Request: {}", incoming);
        RequestEntity<?> outgoing = getOutgoingRequest(incoming);
        this.LOGGER.info("Outgoing Request: {}", outgoing);

        return passthruService.exchange(outgoing);
    }

    protected static RequestEntity<?> getOutgoingRequest(RequestEntity<?> incoming) {

        LOGGER.debug("Making a copy of the headers for the outgoing request.");
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(incoming.getHeaders());

        LOGGER.info("Checking for the presence of the X-Auth-Token header in the request.");

        String token = incoming.getHeaders().getFirst(X_AUTH_TOKEN_HEADER_NAME);
        if (!StringUtils.isEmpty(token)) {

            LOGGER.info("Found an X-Auth-Token in the headers ({})", token);
            LOGGER.info("Generating a new X-Auth-User header from the X-Auth-Token's value");
            Map<String, String> data = XAuthUserTokenBuilder.splitTokenString(token);
            token = XAuthUserTokenBuilder.getToken(data);
            LOGGER.debug("Generated a new X-Auth-User header with a value of {}", token);

            List<String> mylist = new ArrayList<>();
            mylist.add(token);
            headers.put(X_AUTH_USER_HEADER_NAME, mylist);
            LOGGER.info("The new X-Auth-User header has now been added to the request headers");

            headers.remove(X_AUTH_TOKEN_HEADER_NAME);
            LOGGER.info("Removed the X-Auth-Token header, we don't need it any more.");
        }


        URI uri = headers.remove(FORWARDED_URL).stream()
                .findFirst()
                .map(URI::create)
                .orElseThrow(() -> new IllegalStateException(String.format("No %s header present", FORWARDED_URL)));

        return new RequestEntity<>(incoming.getBody(), headers, incoming.getMethod(), uri);
    }
}
