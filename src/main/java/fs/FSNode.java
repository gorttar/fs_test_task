/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static java.util.Objects.requireNonNull;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * interface of file system node
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
interface FSNode {
    @Nonnull
    Optional<FSNode> getParent();

    @Nonnull
    Either<FSError, List<FileInfo>> ls();

    @Nonnull
    Either<FSError, ByteArray> content();

    @Nonnull
    Either<FSError, Unit> write(@Nonnull byte[] content);

    @Nonnull
    Either<FSError, Unit> append(@Nonnull byte[] content);

    @Nonnull
    Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath);

    @Nonnull
    Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType);

    @Nonnull
    Either<FSError, Unit> deleteUnder(@Nonnull String name);

    void copyTo(@Nonnull String newName, @Nonnull FSNode newParent);

    void moveTo(@Nonnull String newName, @Nonnull FSNode newParent);

    @Nonnull
    String name();

    @Nonnull
    FileType type();

    long size();

    @Nonnull
    default FileInfo info() {
        return new FileInfo(path(), type(), size());
    }

    default String path() {
        return path(name());
    }

    default String path(String acc) {
        return getParent()
                .map(parentNode -> parentNode.path(parentNode.name() + '/' + acc))
                .orElse(acc);
    }

    @Nonnull
    static FSNode createUnder(@Nonnull String name, @Nonnull FileType fileType, @Nonnull FSNode parent) {
        requireNonNull(name);
        requireNonNull(fileType);
        requireNonNull(parent);
        final FSNode result;
        switch (fileType) {
            case DIRECTORY:
                result = FSNodeConfig.createDirUnder(name, parent);
                break;
            case REGULAR:
                result = FSNodeConfig.createFileUnder(name, parent);
                break;
            default:
                throw new UnsupportedOperationException(fileType + " is not supported");
        }
        return result;
    }

    @Nonnull
    static FSNode createRoot() {
        return FSNodeConfig.createRoot();
    }

    void link(@Nonnull FSNode child);
}