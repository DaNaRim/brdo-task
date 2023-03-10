package com.danarim.brdotask.util;

import com.danarim.brdotask.config.security.jwt.JwtUtil;
import org.springframework.web.util.WebUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility class to work with cookies.
 */
public final class CookieUtil {

    public static final String COOKIE_ACCESS_TOKEN_KEY = "access_token";
    public static final String COOKIE_REFRESH_TOKEN_KEY = "refresh_token";

    private CookieUtil() {
    }

    /**
     * Get the cookie value from the request.
     *
     * @param request    http request
     * @param cookieName cookie name
     *
     * @return cookie value or null if cookie not found
     */
    public static String getCookieValueByRequest(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .orElse(null);
    }

    /*
      Access token Cookie
     */

    /**
     * Creates a new access token cookie. Expiration same as the token
     *
     * @param accessToken access token value
     *
     * @return cookie with access token
     */
    public static Cookie createAccessTokenCookie(String accessToken) {
        Cookie cookie = new Cookie(COOKIE_ACCESS_TOKEN_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(
                JwtUtil.ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));
        return cookie;
    }

    /**
     * Gets the access token from the request.
     *
     * @param request http request
     *
     * @return access token or null cookie not found
     */
    public static String getAccessTokenValueByRequest(HttpServletRequest request) {
        return getCookieValueByRequest(request, COOKIE_ACCESS_TOKEN_KEY);
    }

    /**
     * Creates a new access token cookie with null value and expiration 0. To delete the cookie you
     * need to add it to the response.
     *
     * @return cookie with null value and expiration 0
     */
    public static Cookie deleteAccessTokenCookie() {
        Cookie cookie = new Cookie(COOKIE_ACCESS_TOKEN_KEY, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    /*
      Refresh token Cookie
     */

    /**
     * Creates a new refresh token cookie. Expiration same as the token
     *
     * @param refreshToken refresh token value
     *
     * @return cookie with refresh token
     */
    public static Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN_KEY, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(
                JwtUtil.REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));
        return cookie;
    }

    /**
     * Gets the refresh token from the request.
     *
     * @param request http request
     *
     * @return refresh token or null cookie not found
     */
    public static String getRefreshTokenValueByRequest(HttpServletRequest request) {
        return getCookieValueByRequest(request, COOKIE_REFRESH_TOKEN_KEY);
    }

    /**
     * Creates a new refresh token cookie with null value and expiration 0. To delete the cookie you
     * need to add it to the response.
     *
     * @return cookie with null value and expiration 0
     */
    public static Cookie deleteRefreshTokenCookie() {
        Cookie cookie = new Cookie(COOKIE_REFRESH_TOKEN_KEY, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

}
