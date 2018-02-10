package io.pivotalservices.tokenservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TestXAuthUserTokenBuilder {

    private static final Map<String, String> data = new HashMap<>();


    @Before
    public void setup(){
        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, null);
        data.put(XAuthUserTokenBuilder.EDO_KLID, null);
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, null);
    }

    @Test
    public void testGetToken_withValidStrings(){
        //Arrange
        String correctToken = "joseHeader.eyJhdXRoVGlja2V0IjogIjIzZmFkZjIzMDlhb2lpamFzc2VnZyIsICJhdXRoU2Vzc2lvbklkIjogIjE1dUlvS2p4NDBqaGpxN3UwSHpHVVdydWtSSm9rS2I0IiwgImF1dGhVc2VySWQiOiAidXNlci1pZCIsICJhdXRoVXNlclR5cGUiOiAiQ1VTVE9NRVIiLCAiYXV0aFVzZXJMZXZlbCI6ICJkdW1teS1hdXRoLXVzZXItbGV2ZWwiLCAgInNpZWJlbEN1c3RvbWVyUmVsYXRpb25JZCI6ICIwMDAwMDAwMTExMTExMTEiLCAic2llYmVsVXNlclJlbGF0aW9uSWQiOiAiMDAwMDAwMDExMTExMTExIiwgICJlZG9LbGlkIjogImR1bW15LWtsaWQiLCAiZWRvQWdyZWVtZW50SWQiOiAiMDAwMDAxMTQzIiwgICJlZG9Vc2VySWQiOiAiZHVtbXktZWRvLXVzZXItaWQiLCAgInNvdXJjZXMiOiBbIlJBU1MiLCAiVEEiXX0=.signingKey";

        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "dummy-auth-user-level");
        data.put(XAuthUserTokenBuilder.EDO_KLID, "dummy-klid");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "dummy-edo-user-id");

        //Act
        String token = XAuthUserTokenBuilder.getToken(data);

        //Assert
        assertThat(token).isEqualTo(correctToken);
    }

    @Test
    public void testGetToken_withNullAuthLevel(){
        //Arrange
        String correctToken = "joseHeader.eyJhdXRoVGlja2V0IjogIjIzZmFkZjIzMDlhb2lpamFzc2VnZyIsICJhdXRoU2Vzc2lvbklkIjogIjE1dUlvS2p4NDBqaGpxN3UwSHpHVVdydWtSSm9rS2I0IiwgImF1dGhVc2VySWQiOiAidXNlci1pZCIsICJhdXRoVXNlclR5cGUiOiAiQ1VTVE9NRVIiLCAiYXV0aFVzZXJMZXZlbCI6ICIiLCAgInNpZWJlbEN1c3RvbWVyUmVsYXRpb25JZCI6ICIwMDAwMDAwMTExMTExMTEiLCAic2llYmVsVXNlclJlbGF0aW9uSWQiOiAiMDAwMDAwMDExMTExMTExIiwgICJlZG9LbGlkIjogImR1bW15LWtsaWQiLCAiZWRvQWdyZWVtZW50SWQiOiAiMDAwMDAxMTQzIiwgICJlZG9Vc2VySWQiOiAiZHVtbXktZWRvLXVzZXItaWQiLCAgInNvdXJjZXMiOiBbIlJBU1MiLCAiVEEiXX0=.signingKey";

        data.put(XAuthUserTokenBuilder.EDO_KLID, "dummy-klid");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "dummy-edo-user-id");

        //Act
        String token = XAuthUserTokenBuilder.getToken(data);

        //Assert
        assertThat(token).isEqualTo(correctToken);
    }

    @Test
    public void testGetToken_withNullEdoKlid(){
        //Arrange
        String correctToken = "joseHeader.eyJhdXRoVGlja2V0IjogIjIzZmFkZjIzMDlhb2lpamFzc2VnZyIsICJhdXRoU2Vzc2lvbklkIjogIjE1dUlvS2p4NDBqaGpxN3UwSHpHVVdydWtSSm9rS2I0IiwgImF1dGhVc2VySWQiOiAidXNlci1pZCIsICJhdXRoVXNlclR5cGUiOiAiQ1VTVE9NRVIiLCAiYXV0aFVzZXJMZXZlbCI6ICJkdW1teS1hdXRoLXVzZXItbGV2ZWwiLCAgInNpZWJlbEN1c3RvbWVyUmVsYXRpb25JZCI6ICIwMDAwMDAwMTExMTExMTEiLCAic2llYmVsVXNlclJlbGF0aW9uSWQiOiAiMDAwMDAwMDExMTExMTExIiwgICJlZG9LbGlkIjogIiIsICJlZG9BZ3JlZW1lbnRJZCI6ICIwMDAwMDExNDMiLCAgImVkb1VzZXJJZCI6ICJkdW1teS1lZG8tdXNlci1pZCIsICAic291cmNlcyI6IFsiUkFTUyIsICJUQSJdfQ==.signingKey";

        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "dummy-auth-user-level");
        data.put(XAuthUserTokenBuilder.EDO_USER_ID, "dummy-edo-user-id");

        //Act
        String token = XAuthUserTokenBuilder.getToken(data);

        //Assert
        assertThat(token).isEqualTo(correctToken);
    }

    @Test
    public void testGetToken_withNullEdoUserId(){
        //Arrange
        String correctToken = "joseHeader.eyJhdXRoVGlja2V0IjogIjIzZmFkZjIzMDlhb2lpamFzc2VnZyIsICJhdXRoU2Vzc2lvbklkIjogIjE1dUlvS2p4NDBqaGpxN3UwSHpHVVdydWtSSm9rS2I0IiwgImF1dGhVc2VySWQiOiAidXNlci1pZCIsICJhdXRoVXNlclR5cGUiOiAiQ1VTVE9NRVIiLCAiYXV0aFVzZXJMZXZlbCI6ICJkdW1teS1hdXRoLXVzZXItbGV2ZWwiLCAgInNpZWJlbEN1c3RvbWVyUmVsYXRpb25JZCI6ICIwMDAwMDAwMTExMTExMTEiLCAic2llYmVsVXNlclJlbGF0aW9uSWQiOiAiMDAwMDAwMDExMTExMTExIiwgICJlZG9LbGlkIjogImR1bW15LWtsaWQiLCAiZWRvQWdyZWVtZW50SWQiOiAiMDAwMDAxMTQzIiwgICJlZG9Vc2VySWQiOiAiIiwgICJzb3VyY2VzIjogWyJSQVNTIiwgIlRBIl19.signingKey";

        data.put(XAuthUserTokenBuilder.AUTH_USER_LEVEL, "dummy-auth-user-level");
        data.put(XAuthUserTokenBuilder.EDO_KLID, "dummy-klid");

        //Act
        String token = XAuthUserTokenBuilder.getToken(data);

        //Assert
        assertThat(token).isEqualTo(correctToken);
    }

    @Test
    public void catchAllControllerTest_splitString(){
        String tokenString = "3:10:YUMA";
        Map<String, String> tokenMap = XAuthUserTokenBuilder.splitTokenString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(3);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("3");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("10");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("YUMA");
    }

    @Test
    public void catchAllControllerTest_splitStringWithNull(){
        String tokenString = null;
        Map<String, String> tokenMap = XAuthUserTokenBuilder.splitTokenString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(3);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("0");
    }

    @Test
    public void catchAllControllerTest_splitStringWithEmptyString(){
        String tokenString = "";
        Map<String, String> tokenMap = XAuthUserTokenBuilder.splitTokenString(tokenString);
        assertThat(tokenMap.entrySet().size()).isEqualTo(3);
        assertThat(tokenMap.get(XAuthUserTokenBuilder.AUTH_USER_LEVEL)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_KLID)).isEqualTo("0");
        assertThat(tokenMap.get(XAuthUserTokenBuilder.EDO_USER_ID)).isEqualTo("0");
    }
}
