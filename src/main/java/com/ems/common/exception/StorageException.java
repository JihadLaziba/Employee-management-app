package com.ems.common.exception;

/** Erreur technique avec MinIO -> HTTP 503, message générique pour l'utilisateur. */
public class StorageException extends RuntimeException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
