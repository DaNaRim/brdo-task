package com.danarim.brdotask.comment.web.controller;

import com.danarim.brdotask.comment.persistence.model.Comment;
import com.danarim.brdotask.comment.service.CommentService;
import com.danarim.brdotask.config.WebConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for {@link Comment} related requests.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

}
