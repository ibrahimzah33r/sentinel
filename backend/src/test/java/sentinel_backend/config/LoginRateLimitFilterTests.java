package sentinel_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTests {

    @Test
    void shouldAllowLoginRequestsWithinLimit()
            throws Exception {

        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/auth/login");

        request.setRemoteAddr("192.168.1.10");

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(
                request,
                response,
                chain);

        assertEquals(
                200,
                response.getStatus());
    }

    @Test
    void shouldRejectLoginRequestsAboveLimit()
            throws Exception {

        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        for (int attempt = 0; attempt < 10; attempt++) {

            MockHttpServletRequest request = new MockHttpServletRequest(
                    "POST",
                    "/api/auth/login");

            request.setRemoteAddr("192.168.1.20");

            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(
                    request,
                    response,
                    mock(MockFilterChain.class));
        }

        MockHttpServletRequest blockedRequest = new MockHttpServletRequest(
                "POST",
                "/api/auth/login");

        blockedRequest.setRemoteAddr(
                "192.168.1.20");

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        filter.doFilter(
                blockedRequest,
                blockedResponse,
                mock(MockFilterChain.class));

        assertEquals(
                429,
                blockedResponse.getStatus());
    }

    @Test
    void shouldRateLimitIpsSeparately()
            throws Exception {

        LoginRateLimitFilter filter = new LoginRateLimitFilter();

        for (int attempt = 0; attempt < 10; attempt++) {

            MockHttpServletRequest request = new MockHttpServletRequest(
                    "POST",
                    "/api/auth/login");

            request.setRemoteAddr("192.168.1.30");

            filter.doFilter(
                    request,
                    new MockHttpServletResponse(),
                    mock(MockFilterChain.class));
        }

        MockHttpServletRequest otherIpRequest = new MockHttpServletRequest(
                "POST",
                "/api/auth/login");

        otherIpRequest.setRemoteAddr(
                "192.168.1.31");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                otherIpRequest,
                response,
                mock(MockFilterChain.class));

        assertEquals(
                200,
                response.getStatus());
    }
}