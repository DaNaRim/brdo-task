package com.danarim.brdotask.config.security.auth;

import com.danarim.brdotask.user.persistence.dao.UserDao;
import com.danarim.brdotask.user.persistence.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Used to get user from database for authentication and authorization.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Get user from database by email. Email is used as username.
     *
     * @param email username of user
     *
     * @return UserDetails
     *
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userDao.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + email);
        }
        return user;
    }

}
