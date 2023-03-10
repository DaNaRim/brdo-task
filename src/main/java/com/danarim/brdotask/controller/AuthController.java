package com.danarim.brdotask.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.brdotask.config.WebConfig;
import com.danarim.brdotask.config.security.auth.AuthResponseEntity;
import com.danarim.brdotask.config.security.jwt.JwtUtil;
import com.danarim.brdotask.user.persistence.model.User;
import com.danarim.brdotask.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for managing authentication state.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX)
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthController(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Remove the authentication cookies from client and block the tokens.
     *
     * @param request  http request
     * @param response http response
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = CookieUtil.getAccessTokenValueByRequest(request);
        String refreshToken = CookieUtil.getRefreshTokenValueByRequest(request);

        if (accessToken != null) {
            jwtUtil.blockToken(accessToken);
        }
        if (refreshToken != null) {
            jwtUtil.blockToken(refreshToken);
        }
        response.addCookie(CookieUtil.deleteAccessTokenCookie());
        response.addCookie(CookieUtil.deleteRefreshTokenCookie());
    }

    /**
     * Uses when user refreshes the page or opens the app.
     * <br>
     * Process the authentication access token cookie and return auth user state to client.
     *
     * @return auth user state
     *
     * @throws JWTVerificationException if the access token is invalid, wrong type, expired or user
     *                                  not found
     */
    @PostMapping("auth/getState")
    public ResponseEntity<AuthResponseEntity> getAuthState(HttpServletRequest request) {

        String accessToken = CookieUtil.getAccessTokenValueByRequest(request);
        DecodedJWT decodedJwt = jwtUtil.decode(accessToken);

        String email = decodedJwt.getSubject();
        String tokenType = decodedJwt.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();
        String tokenId = decodedJwt.getId();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_ACCESS)) {
            throw new JWTVerificationException("Invalid token type");
        }
        if (jwtUtil.isTokenBlocked(Long.parseLong(tokenId))) {
            throw new JWTVerificationException("Token is blocked");
        }
        User user;
        try {
            user = (User) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new JWTVerificationException("User not found", e);
        }
        String csrfToken = decodedJwt.getClaim(JwtUtil.CLAIM_CSRF_TOKEN).asString();

        return ResponseEntity.ok(AuthResponseEntity.generateAuthResponse(user, csrfToken));
    }

    /**
     * Uses when access token is expired.
     * <br>
     * Process the authentication refresh token cookie, update the JWT access token and return auth
     * user state to client
     *
     * @return auth user state
     *
     * @throws JWTVerificationException if token is invalid, wrong type, expired or user not found
     */
    @PostMapping("auth/refresh")
    public ResponseEntity<AuthResponseEntity> refresh(HttpServletRequest request,
                                                      HttpServletResponse response
    ) {

        String refreshToken = CookieUtil.getRefreshTokenValueByRequest(request);
        DecodedJWT decodedJwt = jwtUtil.decode(refreshToken);

        String email = decodedJwt.getSubject();
        String tokenType = decodedJwt.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();
        String tokenId = decodedJwt.getId();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_REFRESH)) {
            throw new JWTVerificationException("Invalid token type");
        }
        if (jwtUtil.isTokenBlocked(Long.parseLong(tokenId))) {
            throw new JWTVerificationException("Token is blocked");
        }
        User user;
        try {
            user = (User) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new JWTVerificationException("User not found", e);
        }
        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(user, csrfToken);

        Cookie accessTokenCookie = CookieUtil.createAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(AuthResponseEntity.generateAuthResponse(user, csrfToken));
    }

}
