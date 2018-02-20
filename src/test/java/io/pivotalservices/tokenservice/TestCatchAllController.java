package io.pivotalservices.tokenservice;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CatchAllController.class)
public class TestCatchAllController {


    private static final String X_AUTH_USER_HEADER_NAME = "x-auth-user";

    private static final String X_AUTH_TOKEN_HEADER_NAME = "x-auth-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PassthruService passthruService;

    private static JwsParser jwsParser;
    private JWKSource<SecurityContext> publicKeySet;


    @Before
    public void setup() throws IOException, ParseException {
        publicKeySet = XAuthUserTokenBuilder.loadPublicKeySet();
        jwsParser = new JwsParser(new DefaultJWTProcessor<>(), publicKeySet);
    }


    @Test
    public void catchAllControllerTest_withNoToken() throws Exception {

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>("my response body".getBytes(Charset.forName("UTF-8")), new HttpHeaders(), HttpStatus.OK);
        when(passthruService.exchange(any())).thenReturn(responseEntity);


        this.mockMvc
                .perform(get("/")
                        .header("X-CF-Forwarded-Url","http://www.google.com")
                        .header("X-CF-Proxy-Metadata", "blah")
                        .header("X-CF-Proxy-Signature", "blah"))
                .andDo(print())
                .andExpect(status().isOk());

        //capturing varargs:
        ArgumentCaptor<RequestEntity> capturedRequest = ArgumentCaptor.forClass(RequestEntity.class);
        verify(passthruService).exchange(capturedRequest.capture());

        HttpHeaders headers = capturedRequest.getValue().getHeaders();

        assertThat(headers).isNotNull();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers).containsKey("X-CF-Proxy-Metadata");
        assertThat(headers).containsKey("X-CF-Proxy-Signature");
        assertThat(headers).doesNotContainKey("X-CF-Forwarded-Url");
        assertThat(headers).doesNotContainKey(X_AUTH_USER_HEADER_NAME);
    }

    @Test
    public void catchAllControllerTest_withToken() throws Exception {

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>("my response body".getBytes(Charset.forName("UTF-8")), new HttpHeaders(), HttpStatus.OK);
        when(passthruService.exchange(any())).thenReturn(responseEntity);

        String testXAuthTokenString = "3:10:YUMA";

        this.mockMvc
                .perform(get("/")
                        .header("X-CF-Forwarded-Url","http://www.google.com")
                        .header("X-CF-Proxy-Metadata", "blah")
                        .header("X-CF-Proxy-Signature", "blah")
                        .header(X_AUTH_TOKEN_HEADER_NAME, testXAuthTokenString))
                .andDo(print())
                .andExpect(status().isOk());

        //capturing varargs:
        ArgumentCaptor<RequestEntity> capturedRequest = ArgumentCaptor.forClass(RequestEntity.class);
        verify(passthruService).exchange(capturedRequest.capture());

        HttpHeaders headers = capturedRequest.getValue().getHeaders();

        assertThat(headers).isNotNull();
        assertThat(headers.size()).isEqualTo(3);
        assertThat(headers).doesNotContainKey("X-CF-Forwarded-Url");

        assertThat(headers).containsKey("X-CF-Proxy-Metadata");
        assertThat(headers).containsKey("X-CF-Proxy-Signature");
        assertThat(headers).containsKey("x-auth-user");
        assertThat(headers.get("x-auth-user")).isNotNull();

        Map<String, Object> data = XAuthUserTokenBuilder.buildTokenDataMapFromString(testXAuthTokenString);

        String token = XAuthUserTokenBuilder.generateSignedToken(data);
        assertThat(headers.getFirst("x-auth-user")).isEqualTo(token);
        JWTClaimsSet jwtClaimsSet = jwsParser.parse(token);
        assertThat(jwtClaimsSet.getClaim("authUserLevel")).isEqualTo(data.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL));
        assertThat(jwtClaimsSet.getClaim("edoUserId")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_USER_ID));
        assertThat(jwtClaimsSet.getClaim("edoKlid")).isEqualTo(data.get(XAuthUserTokenBuilder.EDO_KLID));
    }

}
