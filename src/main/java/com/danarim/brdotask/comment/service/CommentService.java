package com.danarim.brdotask.comment.service;

import com.danarim.brdotask.comment.persistence.model.Comment;

import java.util.List;

/**
 * Service for {@link Comment} entities.
 */
public interface CommentService {

    List<Comment> getAllComments();
}
