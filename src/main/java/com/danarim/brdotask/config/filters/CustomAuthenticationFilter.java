package com.danarim.brdotask.config.filters;

import com.danarim.brdotask.config.security.auth.AuthResponseEntity;
import com.danarim.brdotask.config.security.auth.CustomAuthenticationProvider;
import com.danarim.brdotask.config.security.jwt.JwtUtil;
import com.danarim.brdotask.failhandler.CustomAuthFailureHandler;
import com.danarim.brdotask.user.persistence.model.User;
import com.danarim.brdotask.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Filter to authenticate user.
 * <br>
 * Authentication handles by {@link CustomAuthenticationProvider}
 * <br>
 * Exception handles by {@link CustomAuthFailureHandler}
 */
@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Constructor to inject dependencies.
     *
     * @param authenticationManager manager to authenticate user
     * @param jwtUtil               util to work with jwt tokens
     */
    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      JwtUtil jwtUtil
    ) {
        super.setAuthenticationManager(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Process the authentication from request body.
     *
     * @throws AuthenticationCredentialsNotFoundException if the authentication body is missing or
     *                                                    invalid.
     * @throws AuthenticationException                    if authentication failed.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response
    ) {
        AuthenticationBody authBody;
        try {
            authBody = new ObjectMapper().readValue(request.getInputStream(),
                                                    AuthenticationBody.class);
        } catch (IOException e) {
            throw new AuthenticationCredentialsNotFoundException("invalid AuthenticationBody", e);
        }
        final String username = authBody.username();
        final String password = authBody.password();

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * If authentication is successful, generate a JWT tokens, set them in cookies and return
     * AuthResponse.
     *
     * @throws IOException if an error occurs while writing the response.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult
    ) throws IOException {
        User user = (User) authResult.getPrincipal();

        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(user, csrfToken);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        Cookie accessTokenCookie = CookieUtil.createAccessTokenCookie(accessToken);
        Cookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(refreshToken);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        AuthResponseEntity authResponse = AuthResponseEntity.generateAuthResponse(user, csrfToken);

        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    /**
     * Authentication request body.
     *
     * @param username username (email)
     * @param password password
     */
    protected record AuthenticationBody(
            String username,
            String password
    ) {

    }

}
