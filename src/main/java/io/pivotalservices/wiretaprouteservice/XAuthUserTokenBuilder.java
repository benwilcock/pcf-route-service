package io.pivotalservices.wiretaprouteservice;

import java.nio.charset.StandardCharsets;


public class XAuthUserTokenBuilder {

    public static final String AUTH_TOKEN_FORMAT = "{\"authTicket\": \"23fadf2309aoiijassegg\","
            + " \"authSessionId\": \"15uIoKjx40jhjq7u0HzGUWrukRJokKb4\","
            + " \"authUserId\": \"user-id\","
            + " \"authUserType\": \"CUSTOMER\","
            + " \"authUserLevel\": \"%s\", "
            + " \"siebelCustomerRelationId\": \"000000011111111\","
            + " \"siebelUserRelationId\": \"000000011111111\", "
            + " \"edoKlid\": \"%s\","
            + " \"edoAgreementId\": \"000001143\", "
            + " \"edoUserId\": \"%s\", "
            + " \"sources\": [\"RASS\", \"TA\"]}";


    public static String getToken(String authUserLevel, String edoKlid, String edoUserId) {
        return "joseHeader."
                + java.util.Base64.getEncoder().encodeToString(
                String.format(AUTH_TOKEN_FORMAT, authUserLevel, edoKlid, edoUserId).getBytes(StandardCharsets.UTF_8))
                + ".signingKey";
    }
}
