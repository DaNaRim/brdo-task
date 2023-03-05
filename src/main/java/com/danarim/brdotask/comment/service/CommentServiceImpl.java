package com.danarim.brdotask.comment.service;

import com.danarim.brdotask.comment.persistence.dao.CommentDao;
import com.danarim.brdotask.comment.persistence.model.Comment;
import com.danarim.brdotask.exceptions.InternalServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

/**
 * Service for {@link Comment} entities.
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private static final String DATA_API_URL = "https://dummyjson.com/comments?limit=100";

    private final CommentDao commentDao;

    public CommentServiceImpl(CommentDao commentDao) {
        this.commentDao = commentDao;
    }

    /**
     * Fetches all comments from the database. If the database is empty, it fetches comments from
     * the data API and saves them to the database.
     *
     * @return List of comments
     */
    @Override
    public List<Comment> getAllComments() {
        List<Comment> comments = commentDao.findAll();

        if (!comments.isEmpty()) {
            return comments;
        }
        return initializeComments();
    }

    /**
     * Initializes comments by fetching them from the data API and saving them to the database.
     *
     * @return List of fetched comments
     */
    private List<Comment> initializeComments() {
        RestTemplate restTemplate = new RestTemplate();
        String data = restTemplate.getForObject(DATA_API_URL, String.class);

        List<Comment> comments = getCommentsFromJson(data);

        if (comments == null) {
            throw new InternalServerException("Failed to initialize comments");
        }
        processComments(comments);

        return commentDao.saveAll(comments);
    }

    /**
     * Adds the current date as UpdatedAt to each comment and capitalizes the first letter of the
     * username.
     *
     * @param comments fetched comments
     */
    private static void processComments(List<Comment> comments) {
        for (Comment comment : comments) {
            comment.setUpdatedAt(new Date());
            comment.setUsername(
                    comment.getUsername()
                            .substring(0, 1).toUpperCase()
                            + comment.getUsername().substring(1)
            );
        }
    }

    /**
     * Parses the JSON string and returns a list of comments.
     *
     * @param json JSON string containing comments
     *
     * @return List of comments
     * @throws InternalServerException if exception occurs while parsing JSON
     */
    private static List<Comment> getCommentsFromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode comments = root.get("comments");

            for (JsonNode comment : comments) {
                ((ObjectNode) comment).put("username",
                                           comment.get("user").get("username").asText());
                ((ObjectNode) comment).remove("user");
            }
            return List.of(mapper.treeToValue(comments, Comment[].class));
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to parse comments", e);
        }
    }

}
