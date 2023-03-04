package com.danarim.brdotask.exceptions;

import java.io.Serial;

/**
 * Used when an internal server error occurs.
 */
public class InternalServerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2239953445948010676L;

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
