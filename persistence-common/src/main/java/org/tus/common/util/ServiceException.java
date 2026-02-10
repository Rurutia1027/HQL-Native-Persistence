package org.tus.common.util;

/**
 * Service exception for persistence-common module.
 * Minimal implementation.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
