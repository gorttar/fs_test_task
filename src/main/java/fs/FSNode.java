/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package fs;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 * interface of file system node
 *
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2016-12-26 20:59)
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
    Either<FSError, Unit> write(@Nonnull String name, @Nonnull byte[] content);

    @Nonnull
    Either<FSError, Unit> append(@Nonnull String name, @Nonnull byte[] content);

    void rename(@Nonnull String name);

    FSNode copyUnder(@Nonnull String newName, @Nonnull FSNode parent);

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
        return path("");
    }

    default String path(String acc) {
        return parent()
                .map(parentNode -> parentNode.path(name() + acc))
                .orElse(acc);
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
}