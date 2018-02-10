package io.pivotalservices.tokenservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

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


    @Test
    public void catchAllControllerTest_withNoToken() throws Exception {
        this.mockMvc
                .perform(get("/")
                        .header("X-CF-Forwarded-Url","http://www.google.com")
                        .header("X-CF-Proxy-Metadata", "blah")
                        .header("X-CF-Proxy-Signature", "blah"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void catchAllControllerTest_withToken() throws Exception {
        this.mockMvc
                .perform(get("/")
                        .header("X-CF-Forwarded-Url","http://www.google.com")
                        .header("X-CF-Proxy-Metadata", "blah")
                        .header("X-CF-Proxy-Signature", "blah")
                        .header(X_AUTH_TOKEN_HEADER_NAME, "3:10:YUMA"))
                .andDo(print())
                .andExpect(status().isOk());
    }

}
