/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * immutable wrapper for byte array
 * <p>
 * abstraction function:
 * *    represents immutable array of bytes
 * <p>
 * safety from exposure:
 * *    all fields are private
 * *    {@link #ByteArray(byte[])} and {@link #get()} makes defensive copying of {@link #content} to avoid it's sharing
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class ByteArray {
    // representation:
    @Nonnull
    private final byte[] content;

    /**
     * @param content to be wrapped
     */
    public ByteArray(@Nonnull byte[] content) {
        this.content = requireNonNull(content).clone();
    }

    /**
     * @return copy of wrapped byte array
     */
    @Nonnull
    public byte[] get() {
        return content.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteArray)) {
            return false;
        }
        ByteArray byteArray = (ByteArray) o;
        return Arrays.equals(content, byteArray.content);
    }

    public int length() {
        return content.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "ByteArray(" + Arrays.toString(content) + ')';
    }


}