package com.linksnip.urlshortener.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsNotFoundTo404() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleNotFound(new UrlNotFoundException("missing"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void mapsExpiredTo410() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleExpired(new UrlExpiredException("expired"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
    }

    @Test
    void mapsAliasConflictTo409() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleAliasTaken(new AliasAlreadyExistsException("taken"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsReservedAliasTo422() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleReservedAlias(new ReservedAliasException("reserved"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void mapsEmailTakenTo409() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleEmailTaken(new EmailAlreadyRegisteredException("taken"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void mapsBadCredentialsTo401() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleBadCredentials(new BadCredentialsException("bad"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void mapsAccessDeniedTo403() {
        when(request.getRequestURI()).thenReturn("/x");
        ResponseEntity<?> response = handler.handleAccessDenied(new AccessDeniedException("denied"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
