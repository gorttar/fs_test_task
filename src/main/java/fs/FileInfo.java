/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class FileInfo {
    @Nonnull
    public final String fullName;

    @Nonnull
    public final FileType type;

    public final long size;

    public FileInfo(@Nonnull String fullName, @Nonnull FileType type, long size) {
        this.fullName = fullName;
        this.type = type;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo)) return false;
        FileInfo fileInfo = (FileInfo) o;
        return size == fileInfo.size &&
                Objects.equals(fullName, fileInfo.fullName) &&
                type == fileInfo.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, type, size);
    }

    @Override
    public String toString() {
        return "FileInfo(\"" + fullName + "\"," + type + ',' + size + ')';
    }
}