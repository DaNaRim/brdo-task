package com.danarim.brdotask.comment.persistence.dao;

import com.danarim.brdotask.comment.persistence.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for {@link Comment} entities.
 */
public interface CommentDao extends JpaRepository<Comment, Long> {

}
