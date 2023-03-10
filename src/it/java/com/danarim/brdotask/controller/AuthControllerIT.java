package com.danarim.brdotask.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.brdotask.DbUserFiller;
import com.danarim.brdotask.config.WebConfig;
import com.danarim.brdotask.config.security.jwt.JwtTokenDao;
import com.danarim.brdotask.config.security.jwt.JwtUtil;
import com.danarim.brdotask.user.persistence.model.RoleName;
import com.danarim.brdotask.util.CookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;
import javax.servlet.http.Cookie;

import static com.danarim.brdotask.DbUserFiller.USER_USERNAME;
import static com.danarim.brdotask.TestUtils.getAccessTokenCookie;
import static com.danarim.brdotask.TestUtils.getLoginResult;
import static com.danarim.brdotask.TestUtils.getRefreshTokenCookie;
import static com.danarim.brdotask.TestUtils.postExt;
import static com.danarim.brdotask.controller.AuthControllerIT.AuthControllerITUtils.authCookiesDontChange;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private JwtTokenDao jwtTokenDao;

    @Test
    void logout() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/logout")
                                .cookie(accessTokenCookie)
                                .cookie(refreshTokenCookie))
                .andExpect(status().isNoContent())

                .andExpect(cookie().exists(CookieUtil.COOKIE_ACCESS_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))
                .andExpect(cookie().maxAge(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, 0))

                .andExpect(cookie().exists(CookieUtil.COOKIE_REFRESH_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, true))
                .andExpect(cookie().maxAge(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, 0));

        DecodedJWT accessToken = jwtUtil.decode(accessTokenCookie.getValue());
        DecodedJWT refreshToken = jwtUtil.decode(refreshTokenCookie.getValue());
        long accessTokenId = Long.parseLong(accessToken.getId());
        long refreshTokenId = Long.parseLong(refreshToken.getId());

        assertTrue(jwtTokenDao.isTokenBlocked(accessTokenId),
                   "Access token should be blocked after logout");
        assertTrue(jwtTokenDao.isTokenBlocked(refreshTokenId),
                   "Refresh token should be blocked after logout");
    }

    @Test
    void logout_NoCookies() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/logout"))
                .andExpect(status().isNoContent())

                .andExpect(cookie().exists(CookieUtil.COOKIE_ACCESS_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))
                .andExpect(cookie().maxAge(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, 0))

                .andExpect(cookie().exists(CookieUtil.COOKIE_REFRESH_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, true))
                .andExpect(cookie().maxAge(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, 0));
    }

    @Test
    void authRefresh() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(accessTokenCookie)
                                .cookie(refreshTokenCookie))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.username").value(USER_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.csrfToken").exists())

                .andExpect(cookie().exists(CookieUtil.COOKIE_ACCESS_TOKEN_KEY))
                .andExpect(cookie()
                                   .value(CookieUtil.COOKIE_ACCESS_TOKEN_KEY,
                                          not(accessTokenCookie.getValue())))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))
                .andExpect(cookie().secure(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))

                //refresh token cookie doesn't change
                .andExpect(cookie().doesNotExist(CookieUtil.COOKIE_REFRESH_TOKEN_KEY));
    }

    @Test
    void authRefresh_NoToken_Unauthorized() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh"))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authRefresh_ExpiredToken_Unauthorized() throws Exception {
        String accessToken =
                jwtUtil.generateAccessToken(DbUserFiller.testUser, "csrf doesn't matter", -1L);

        Cookie accessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authRefresh_InvalidToken_Unauthorized() throws Exception {
        Cookie invalidRefreshTokenCookie =
                new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, "invalid");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(invalidRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authRefresh_IncorrectToken_Unauthorized() throws Exception {
        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(DbUserFiller.testUser, csrfToken, -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectRefreshTokenCookie =
                new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authRefresh_AccessTokenAsRefresh_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        refreshTokenCookie.setValue(accessTokenCookie.getValue());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authRefresh_BlockedToken_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        String refreshToken = refreshTokenCookie.getValue();

        jwtUtil.blockToken(refreshToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                                .cookie(accessTokenCookie)
                                .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void authGetState() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(getAccessTokenCookie(result)))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.username").value(USER_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.csrfToken").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_NoToken_Unauthorized() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState"))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_ExpiredToken_Unauthorized() throws Exception {
        String accessToken =
                jwtUtil.generateAccessToken(DbUserFiller.testUser, "csrf doesn't matter", -1L);

        Cookie accessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_InvalidToken_Unauthorized() throws Exception {
        Cookie invalidRefreshTokenCookie =
                new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, "invalid");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(invalidRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_IncorrectToken_Unauthorized() throws Exception {
        String accessToken =
                jwtUtil.generateAccessToken(DbUserFiller.testUser, "csrf doesn't matter", -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectRefreshTokenCookie =
                new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_RefreshTokenAsAccess_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        accessTokenCookie.setValue(refreshTokenCookie.getValue());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())
                .andExpect(authCookiesDontChange());
    }

    @Test
    void authGetState_BlockedToken_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);

        String accessToken = accessTokenCookie.getValue();

        jwtUtil.blockToken(accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                                .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    protected static class AuthControllerITUtils {

        protected static ResultMatcher authCookiesDontChange() {
            return result -> {
                cookie().doesNotExist(CookieUtil.COOKIE_ACCESS_TOKEN_KEY).match(result);
                cookie().doesNotExist(CookieUtil.COOKIE_REFRESH_TOKEN_KEY).match(result);
            };
        }

    }

}
