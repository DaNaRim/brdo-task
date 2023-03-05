package com.danarim.brdotask.comment.service;

import com.danarim.brdotask.comment.persistence.dao.CommentDao;
import com.danarim.brdotask.comment.persistence.model.Comment;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//Integration test because it uses the external API
@SpringBootTest
class CommentServiceImplIT {

    private final CommentDao commentDao = mock(CommentDao.class);

    private final CommentServiceImpl commentService = new CommentServiceImpl(commentDao);

    @Test
    void getAllComments_emptyDb_fetching() {
        when(commentDao.findAll()).thenReturn(new ArrayList<>());

        List<Comment> comments = commentService.getAllComments();

        assertNotNull(comments, "Comments should not be null");
    }

}
