package com.danarim.brdotask;

import com.danarim.brdotask.user.persistence.dao.RoleDao;
import com.danarim.brdotask.user.persistence.dao.UserDao;
import com.danarim.brdotask.user.persistence.model.Role;
import com.danarim.brdotask.user.persistence.model.RoleName;
import com.danarim.brdotask.user.persistence.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Used to fill database with users and helping to test authentication.
 */
@TestConfiguration
public class DbUserFiller {

    public static final String USER_NOT_ACTIVATED_USERNAME = "user_not_activated";

    public static final String USER_USERNAME = "user";
    public static final String USER_PASSWORD = "userPassword";

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "adminPassword";

    public static final String AUTH_JSON_TEMPLATE = "{\"username\": \"%s\",\"password\": \"%s\"}";
    public static final String AUTH_JSON_USER =
            String.format(AUTH_JSON_TEMPLATE, USER_USERNAME, USER_PASSWORD);
    public static final String AUTH_JSON_ADMIN =
            String.format(AUTH_JSON_TEMPLATE, ADMIN_USERNAME, ADMIN_PASSWORD);

    private static final Log logger = LogFactory.getLog(DbUserFiller.class);

    public static User testUser;

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Fills the database with test users.
     */
    @EventListener(ApplicationStartedEvent.class)
    @Order(1)
    public void prepareDbWithUsersForTests() {
        logger.info("Filling the database with test users");

        saveNotVerificatedUser();
        saveVerificatedUser();
        saveAdmin();

        logger.info("Database filled with test users");
    }

    private void saveVerificatedUser() {
        String userPassword = passwordEncoder.encode(USER_PASSWORD);
        Role userRoles = roleDao.findByRoleName(RoleName.ROLE_USER);

        User user = new User(
                USER_USERNAME,
                userPassword,
                Set.of(userRoles)
        );
        user.setEmailVerified(true);

        this.testUser = userDao.save(user);
    }

    private void saveNotVerificatedUser() {
        String userPassword = passwordEncoder.encode(USER_PASSWORD);
        Role userRole = roleDao.findByRoleName(RoleName.ROLE_USER);

        User user = new User(
                USER_NOT_ACTIVATED_USERNAME,
                userPassword,
                Set.of(userRole)
        );
        userDao.save(user);
    }

    private void saveAdmin() {
        String adminPassword = passwordEncoder.encode(ADMIN_PASSWORD);

        Role userRole = roleDao.findByRoleName(RoleName.ROLE_USER);
        Role adminRole = roleDao.findByRoleName(RoleName.ROLE_ADMIN);

        User admin = new User(
                ADMIN_USERNAME,
                adminPassword,
                Set.of(userRole, adminRole)
        );
        admin.setEmailVerified(true);

        userDao.save(admin);
    }

}
