package com.danarim.brdotask.failhandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.brdotask.exceptions.BadFieldException;
import com.danarim.brdotask.exceptions.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Collections;
import java.util.List;

/**
 * Handles exceptions thrown by rest controllers.
 * <br>
 * All methods except auth handlers must return {@link ResponseEntity} with list of
 * {@link ErrorResponse} as body.
 * <br>
 * The reason for returning list instead of single object is because frontend always expects list of
 * errors for validation.
 */
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    protected static final String LOG_TEMPLATE = "%s during request: %s : %s";

    private final MessageSource messages;

    public RestExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Handles validation exceptions with global type.
     *
     * @param e       exception caused by global validation error.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadRequestException(BadRequestException e,
                                                                            WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        String message =
                messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.globalError(e.getMessageCode(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation exceptions that are caused by specific fields.
     *
     * @param e       exception caused by field validation.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadFieldException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadFieldException(BadFieldException e,
                                                                          WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        String message =
                messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());

        ErrorResponse errorResponse =
                ErrorResponse.fieldError(e.getMessageCode(), e.getField(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles {@link AccessDeniedException} thrown by Spring Security when user is not authorized
     * to access resource.
     *
     * @param e       exception caused access denied.
     * @param request request where exception occurred.
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<List<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException e,
            WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        String message = messages.getMessage("error.access.denied", null, request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.globalError("error.access.denied", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link TokenExpiredException} thrown by auth refresh endpoint when token is expired.
     *
     * @param e       exception caused by expired token.
     * @param request request where exception occurred.
     *
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by
     *         frontend.
     */
    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<String> handleTokenExpiredException(TokenExpiredException e,
                                                                 WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.expired",
                                          null,
                                          request.getLocale()));
    }

    /**
     * Handles {@link JWTVerificationException} thrown by auth refresh endpoint when token is
     * invalid.
     *
     * @param e       exception caused by invalid jwt token.
     * @param request request where exception occurred.
     *
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by
     *         frontend.
     */
    @ExceptionHandler(JWTVerificationException.class)
    protected ResponseEntity<String> handleJwtVerificationException(JWTVerificationException e,
                                                                    WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.invalid",
                                          null,
                                          request.getLocale()));
    }

    /**
     * Handles server exceptions.
     *
     * @param e       exception caused by server.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<ErrorResponse>> handleInternalException(Exception e,
                                                                          WebRequest request
    ) {
        logger.error("Internal server error during request: " + request.getContextPath(), e);

        String message =
                messages.getMessage("error.server.internal-error", null, request.getLocale());

        ErrorResponse errorResponse =
                ErrorResponse.serverError("error.server.internal-error", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
