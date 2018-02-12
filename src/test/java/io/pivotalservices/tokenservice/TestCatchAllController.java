package io.pivotalservices.tokenservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
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
        assertThat(headers).doesNotContainKey("x-auth-user");
    }

    @Test
    public void catchAllControllerTest_withToken() throws Exception {

        ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>("my response body".getBytes(Charset.forName("UTF-8")), new HttpHeaders(), HttpStatus.OK);
        when(passthruService.exchange(any())).thenReturn(responseEntity);


        this.mockMvc
                .perform(get("/")
                        .header("X-CF-Forwarded-Url","http://www.google.com")
                        .header("X-CF-Proxy-Metadata", "blah")
                        .header("X-CF-Proxy-Signature", "blah")
                        .header(X_AUTH_TOKEN_HEADER_NAME, "3:10:YUMA"))
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

        Map<String, String> data = new HashMap<>();
        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "3");
        data.put(XAuthUserTokenBuilder.EDO_KLID, "10");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "YUMA");

        String token = XAuthUserTokenBuilder.getToken(data);
        assertThat(headers.get("x-auth-user")).isNotNull();
        assertThat(headers.getFirst("x-auth-user")).isEqualTo(token);
    }

}
