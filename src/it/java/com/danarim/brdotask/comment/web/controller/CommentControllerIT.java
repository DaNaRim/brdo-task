package com.danarim.brdotask.comment.web.controller;

import com.danarim.brdotask.comment.service.CommentService;
import com.danarim.brdotask.config.WebConfig;
import com.danarim.brdotask.failhandler.RestExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.brdotask.TestUtils.getExt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@ContextConfiguration(classes = {CommentController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void pingGetAllComments() throws Exception {
        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/comments"))
                .andExpect(status().isOk());
    }

}
