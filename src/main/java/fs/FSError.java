/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

/**
 * type to represent FS related exceptions. Contains no getters because it's fields are immutable, their types
 * are also immutable, so there is no need to hide them behind getters
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-20)
 */
public class FSError extends Exception {
    /**
     * type of error
     */
    public final Type type;

    /**
     * error message
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public final String message;

    @SuppressWarnings("WeakerAccess")
    public FSError(Type type, String message) {
        super(message);
        this.type = type;
        this.message = message;
    }

    /**
     * enumeration of available file system errors
     */
    public enum Type {
        NO_FREE_SPACE, FILE_ALREADY_EXISTS, PATH_NOT_FOUND, FILE_IS_DIRECTORY, FILE_IS_REGULAR, DESTINATION_IS_SOURCE_SUBTREE, FS_CREATION_FAILED
    }
}