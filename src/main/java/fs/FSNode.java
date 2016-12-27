/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * interface of file system node
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
interface FSNode extends Cloneable {
    @Nonnull
    Optional<FSNode> parent();

    @Nonnull
    Either<FSError, Map<String, FSNode>> children();

    @Nonnull
    Either<FSError, ByteArray> content();

    @Nonnull
    Either<FSError, Unit> addChild(@Nonnull FSNode child);

    @Nonnull
    Either<FSError, Unit> removeChild(@Nonnull FSNode child);

    @Nonnull
    Either<FSError, Unit> write(@Nonnull byte[] content);

    @Nonnull
    Either<FSError, Unit> append(@Nonnull byte[] content);

    void rename(@Nonnull String name);

    @Nonnull
    Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath);

    @Nonnull
    Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType);

    @Nonnull
    Either<FSError, Unit> deleteUnder(@Nonnull String name);

    @Nonnull
    FSNode copyTo(@Nonnull String newName, @Nonnull FSNode newParent);

    @Nonnull
    String name();

    @Nonnull
    FileType type();

    long size();

    @Nonnull
    default FileInfo info() {
        return new FileInfo(path(), type(), size());
    }

    default boolean isRoot() {
        return !parent().isPresent();
    }

    default String path() {
        return path(name());
    }

    default String path(String acc) {
        return parent()
                .map(parentNode -> parentNode.path(parentNode.name() + '/' + acc))
                .orElse('/' + acc);
    }

    @Nonnull
    static FSNode createUnder(@Nonnull String name, @Nonnull FileType fileType, @Nonnull FSNode parent) {
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
}