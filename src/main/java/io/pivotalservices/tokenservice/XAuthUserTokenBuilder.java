package io.pivotalservices.tokenservice;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class XAuthUserTokenBuilder {

    private static final String EMPTY_STRING = "";
    public static final String AUTH_USER_LEVEL = "AUTH_USER_LEVEL";
    public static final String EDO_KLID = "EDO_KLID";
    public static final String EDO_USER_ID = "EDO_USER_ID";

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


    public static String getToken(Map<String, String> data) {

        return "joseHeader."
                + java.util.Base64.getEncoder().encodeToString(
                String.format(AUTH_TOKEN_FORMAT,
                        Objects.toString(data.get(AUTH_USER_LEVEL), EMPTY_STRING),
                        Objects.toString(data.get(EDO_KLID), EMPTY_STRING),
                        Objects.toString(data.get(EDO_USER_ID), EMPTY_STRING))
                        .getBytes(StandardCharsets.UTF_8))
                + ".signingKey";
    }

    public static String getPlaintextToken(Map<String, String> data) {

        return "joseHeader." +
                String.format(AUTH_TOKEN_FORMAT,
                        Objects.toString(data.get(AUTH_USER_LEVEL), EMPTY_STRING),
                        Objects.toString(data.get(EDO_KLID), EMPTY_STRING),
                        Objects.toString(data.get(EDO_USER_ID), EMPTY_STRING))
                + ".signingKey";
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, String> splitTokenString(String token){
        String[] bits = StringUtils.commaDelimitedListToStringArray("0,0,0");
        if(!StringUtils.isEmpty(token)){
            bits  = StringUtils.tokenizeToStringArray(token, ":");
        }

        if(bits.length != 3){
            throw new IllegalArgumentException("The token was malfomed.");
        }

        Map<String,String> data = new HashMap<>();
        data.put(AUTH_USER_LEVEL, bits[0]);
        data.put(EDO_KLID, bits[1]);
        data.put(EDO_USER_ID, bits[2]);

        return data;
    }


}
