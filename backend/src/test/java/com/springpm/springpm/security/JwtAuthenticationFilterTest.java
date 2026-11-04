package com.springpm.springpm.security;

import com.springpm.springpm.util.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Request threads are pooled, so the user set for one request must never be seen by the next.
 */
class JwtAuthenticationFilterTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final JwtTokenProvider tokens = mock(JwtTokenProvider.class);
    private final CustomUserDetailsService users = mock(CustomUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens, users);

    @AfterEach
    void reset() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest withToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/workspaces");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void acceptToken(String token) {
        when(tokens.validateAccessToken(token)).thenReturn(true);
        when(tokens.getEmailFromToken(token)).thenReturn("a@example.com");
        when(tokens.getUserIdFromToken(token)).thenReturn(USER.toString());
        CustomUserDetails details = mock(CustomUserDetails.class);
        doReturn(List.of()).when(details).getAuthorities();
        when(users.loadUserByUsername("a@example.com")).thenReturn(details);
    }

    @Test
    void userIsSetDuringTheRequestAndClearedAfter() throws Exception {
        acceptToken("good");
        AtomicReference<Optional<UUID>> seen = new AtomicReference<>();
        FilterChain chain = (req, res) -> seen.set(TenantContext.getCurrentUserId());

        filter.doFilter(withToken("good"), new MockHttpServletResponse(), chain);

        assertThat(seen.get()).contains(USER);
        assertThat(TenantContext.getCurrentUserId()).isEmpty();
    }

    @Test
    void contextIsClearedEvenWhenTheRequestFails() {
        acceptToken("good");
        FilterChain chain = (req, res) -> { throw new ServletException("boom"); };

        assertThatThrownBy(() -> filter.doFilter(withToken("good"), new MockHttpServletResponse(), chain))
                .isInstanceOf(ServletException.class);
        assertThat(TenantContext.getCurrentUserId()).isEmpty();
    }

    @Test
    void aLeftoverUserOnThePooledThreadIsNotSeenByTheNextRequest() throws Exception {
        // As if an earlier request on this thread never cleaned up
        TenantContext.setCurrentUserId(UUID.randomUUID());
        TenantContext.setCurrentWorkspaceId(UUID.randomUUID());
        AtomicReference<Boolean> sawAnyone = new AtomicReference<>();
        FilterChain chain = (req, res) -> sawAnyone.set(
                TenantContext.getCurrentUserId().isPresent() || TenantContext.getCurrentWorkspaceId().isPresent());

        filter.doFilter(new MockHttpServletRequest("GET", "/workspaces"), new MockHttpServletResponse(), chain);

        assertThat(sawAnyone.get()).isFalse();
    }

    @Test
    void aTokenThatIsNotAnAccessTokenSetsNoUser() throws Exception {
        when(tokens.validateAccessToken("refresh-like")).thenReturn(false);
        AtomicReference<Optional<UUID>> seen = new AtomicReference<>();
        FilterChain chain = (req, res) -> seen.set(TenantContext.getCurrentUserId());

        filter.doFilter(withToken("refresh-like"), new MockHttpServletResponse(), chain);

        assertThat(seen.get()).isEmpty();
        verify(users, never()).loadUserByUsername(anyString());
    }
}
