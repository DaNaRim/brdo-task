package com.danarim.brdotask.config.security.filters;

import com.danarim.brdotask.DbUserFiller;
import com.danarim.brdotask.config.WebConfig;
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

import javax.servlet.http.Cookie;

import static com.danarim.brdotask.TestUtils.csrfTokenHeader;
import static com.danarim.brdotask.TestUtils.getAccessTokenCookie;
import static com.danarim.brdotask.TestUtils.getExt;
import static com.danarim.brdotask.TestUtils.getExtWithAuth;
import static com.danarim.brdotask.TestUtils.getLoginResult;
import static com.danarim.brdotask.TestUtils.getRefreshTokenCookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class CustomAuthorizationFilterIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void authorization_NoLoginAccess() throws Exception {
        mockMvc.perform(getExt("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub"))
                .andExpect(status().isForbidden());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/adminStub"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorization_LoggedUserAccess() throws Exception {
        mockMvc.perform(
                        getExtWithAuth(WebConfig.API_V1_PREFIX + "/stub", RoleName.ROLE_USER,
                                       mockMvc))
                .andExpect(status().isOk());

        mockMvc.perform(
                        getExtWithAuth(WebConfig.API_V1_PREFIX + "/adminStub", RoleName.ROLE_USER,
                                       mockMvc))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorization_LoggedAdminAccess() throws Exception {
        mockMvc.perform(
                        getExtWithAuth(WebConfig.API_V1_PREFIX + "/stub", RoleName.ROLE_ADMIN,
                                       mockMvc))
                .andExpect(status().isOk());

        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/adminStub", RoleName.ROLE_ADMIN,
                                       mockMvc))
                .andExpect(status().isOk());
    }

    @Test
    void authorization_NoCsrf_Forbidden() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .cookie(getAccessTokenCookie(result)))
                .andExpect(status().isForbidden());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/adminStub")
                                .cookie(getAccessTokenCookie(result)))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorization_ExpiredToken_Unauthorized() throws Exception {
        String accessToken =
                jwtUtil.generateAccessToken(DbUserFiller.testUser, "csrf doesn't matter", -1L);

        Cookie accessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void authorization_InvalidToken_Unauthorized() throws Exception {
        Cookie invalidAccessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, "invalid");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .cookie(invalidAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void authorization_IncorrectToken_Unauthorized() throws Exception {
        String accessToken =
                jwtUtil.generateAccessToken(DbUserFiller.testUser, "doesn`t matter", -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectAccessTokenCookie =
                new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .cookie(incorrectAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void authorization_RefreshTokenAsAccess_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        Cookie accessTokenCookie = getAccessTokenCookie(result);
        Cookie refreshTokenCookie = getRefreshTokenCookie(result);

        accessTokenCookie.setValue(refreshTokenCookie.getValue());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .headers(csrfTokenHeader(result))
                                .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorization_BlockedToken_Unauthorized() throws Exception {
        MvcResult result = getLoginResult(RoleName.ROLE_USER, mockMvc);

        jwtUtil.blockToken(getAccessTokenCookie(result).getValue());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                                .headers(csrfTokenHeader(result))
                                .cookie(getAccessTokenCookie(result)))
                .andExpect(status().isUnauthorized());
    }

}

