/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static java.util.Arrays.asList;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import java.util.List;

final class FSConfig {
    private FSConfig() {
    }

    static FS init(long size) {
        return new SimpleFSImpl(size);
    }
}

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
final class SimpleFSImpl implements FS {
    private final long size;

    private final FSNode root = FSNode.createRoot();

    SimpleFSImpl(long size) {
        if (size < 0) {
            throw new IllegalStateException("Can't create file system with negative size");
        }
        this.size = size;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> create(@Nonnull String path, @Nonnull FileType fileType) {
        final List<String> splitPath = splitPath(path);
        return findParentNode(splitPath).rFlatMap(parent -> parent.createUnder(splitPath.get(splitPath.size() - 1), fileType));
    }

    @Nonnull
    private Either<FSError, FSNode> findParentNode(@Nonnull List<String> splitPath) {
        return root.findUnder(splitPath.subList(0, splitPath.size() - 1));
    }

    @Nonnull
    private static List<String> splitPath(@Nonnull String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(String.format("Path %s is malformed (not starting from '/')", path));
        }
        return asList(path.replaceAll("/+", "/").replaceAll("^/", "").split("/"));
    }

    @Nonnull
    @Override
    public Either<FSError, FileInfo> info(@Nonnull String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> read(@Nonnull String path) {
        return root.findUnder(splitPath(path)).rFlatMap(FSNode::content);
    }

    @Nonnull
    @Override
    public Either<FSError, List<FileInfo>> ls(@Nonnull String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> copy(@Nonnull String sourcePath, @Nonnull String destinationPath) {
        return root.findUnder(splitPath(sourcePath)).rFlatMap(
                src -> src.size() <= free()
                        ? doCopy(src, destinationPath)
                        : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space")));
    }

    private Either<FSError, Unit> doCopy(FSNode src, String destinationPath) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull String path, @Nonnull byte[] content) {
        return root.findUnder(splitPath(path)).rFlatMap(
                node -> content.length <= free() + node.size()
                        ? node.write(content)
                        : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space")));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull String path, @Nonnull byte[] content) {
        return root.findUnder(splitPath(path)).rFlatMap(
                node -> content.length <= free()
                        ? node.append(content)
                        : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space")));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> delete(@Nonnull String path) {
        final List<String> splitPath = splitPath(path);
        return findParentNode(splitPath).rFlatMap(parent -> parent.deleteUnder(splitPath.get(splitPath.size() - 1)));
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public long used() {
        return root.size();
    }
}