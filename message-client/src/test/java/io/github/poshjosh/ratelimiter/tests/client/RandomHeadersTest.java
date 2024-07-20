package io.github.poshjosh.ratelimiter.tests.client;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomHeadersTest {

    @ParameterizedTest
    @CsvSource({
            "0,false,1,true,randomize_givenBot_shouldNotBeAuthorized()",
            "1,true,0,false,randomize_givenAuthorizedUser_shouldNotBeABot()",
            "1,true,1,false,randomize_givenAuthorizedUserAndBot_authorizedUserTakesPrecedence()",
            "0,false,0,false,randomize_givenNeitherAuthorizedUserNotBot_shouldBeNeither()"
    })
    void randomize_givenAuthorizedUserAndBot(
            final float probabilityOfBeingLoggedIn,
            final boolean expectedToBeAuthorized,
            final float probabilityOfBeingABot,
            final boolean expectedToBeBot,
            final String testMethod) {
        RandomHeaders.reset();
        final HttpHeaders headers = new HttpHeaders();
        RandomHeaders.randomize(headers, probabilityOfBeingLoggedIn, probabilityOfBeingABot);
        final String location = this.getClass().getSimpleName() + "#" + testMethod;
        assertEquals(expectedToBeAuthorized, isAuthorized(headers), expectedToBeAuthorized ?
                "Expected to be authorized but was not. \n@" + location :
                "Expected not to be authorized but was. \n@" + location);
        assertEquals(expectedToBeBot, isBot(headers), expectedToBeBot ?
                "Expected to be a bot but was not. \n@" + location :
                "Expected not to be a bot but was. \n@" + location);
    }

    private boolean isBot(HttpHeaders headers) {
        final List<String> userAgentList = headers.get(RandomHeaders.USER_AGENT_HEADER);
        if (userAgentList == null) {
            return false;
        }
        final String userAgent = userAgentList.get(0);
        return RandomHeaders.isRegisteredBot(userAgent);
    }

    private boolean isAuthorized(HttpHeaders headers) {
        final List<String> authorizationList = headers.get(RandomHeaders.AUTHORIZATION_HEADER);
        if (authorizationList == null) {
            return false;
        }
        final String authorization = authorizationList.get(0);
        if (authorization == null) {
            return false;
        }
        return authorization.startsWith("Basic ");
    }
}