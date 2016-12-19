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
    public final String message;

    public FSError(Type type, String message) {
        super(message);
        this.type = type;
        this.message = message;
    }

    public enum Type {
        NO_FREE_SPACE, FILE_ALREADY_EXISTS, NO_FILE_FOUND, PATH_NOT_FOUND
    }
}