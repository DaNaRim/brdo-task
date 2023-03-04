package com.danarim.brdotask.config.security.auth;


import com.danarim.brdotask.user.persistence.model.User;

import java.util.Arrays;
import java.util.Objects;

/**
 * Uses for managing auth on client side.
 */
public record AuthResponseEntity(
        String username,
        String[] roles,
        String csrfToken
) {

    /**
     * Generates auth response entity for user. Use this method to generate response for user.
     *
     * @param user     user to generate response for
     * @param csrfToken csrf token to send to client
     *
     * @return auth response entity
     */
    public static AuthResponseEntity generateAuthResponse(User user, String csrfToken) {
        return new AuthResponseEntity(
                user.getUsername(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toArray(String[]::new),
                csrfToken
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthResponseEntity that = (AuthResponseEntity) o;
        return Objects.equals(username, that.username)
                && Arrays.equals(roles, that.roles)
                && Objects.equals(csrfToken, that.csrfToken);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(username, csrfToken);
        result = 31 * result + Arrays.hashCode(roles);
        return result;
    }

    @Override
    public String toString() {
        return "AuthResponseEntity{"
                + "username='" + username + '\''
                + ", roles=" + Arrays.toString(roles)
                + ", csrfToken='" + csrfToken + '\''
                + '}';
    }
}
