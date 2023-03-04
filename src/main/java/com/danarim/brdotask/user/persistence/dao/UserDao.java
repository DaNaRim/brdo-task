package com.danarim.brdotask.user.persistence.dao;

import com.danarim.brdotask.user.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Manages the {@link User} in the database.
 */
public interface UserDao extends JpaRepository<User, Long> {

    User findByEmailIgnoreCase(String email);

}
