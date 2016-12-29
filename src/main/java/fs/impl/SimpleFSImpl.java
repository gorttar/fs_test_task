/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs.impl;

import static fs.FSError.Type.FILE_IS_REGULAR;
import static fs.FileType.DIRECTORY;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import check.CheckHelper;
import data.ByteArray;
import data.either.Either;
import data.Unit;
import fs.FS;
import fs.FSError;
import fs.FileInfo;
import fs.FileType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

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
        checkInvariants();
    }

    private void checkInvariants() {
        assert root.size() <= size;
    }

    private <T> T checkedGet(Supplier<? extends T> payload) {
        return CheckHelper.checkedGet(payload, this::checkInvariants);
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> create(@Nonnull String path, @Nonnull FileType fileType) {
        return checkedGet(
                () -> {
                    requireNonNull(fileType);
                    final List<String> splitPath = splitPath(requireNonNull(path));
                    return findParentNode(splitPath).rFlatMap(parent -> parent.createUnder(splitPath.get(splitPath.size() - 1), fileType));
                });
    }

    @Nonnull
    private Either<FSError, FSNode> findParentNode(@Nonnull List<String> splitPath) {
        return checkedGet(() -> root.findUnder(requireNonNull(splitPath).subList(0, splitPath.size() - 1)));
    }

    @Nonnull
    private static List<String> splitPath(@Nonnull String path) {
        if (!requireNonNull(path).startsWith("/")) {
            throw new IllegalArgumentException(String.format("Path %s is malformed (not starting from '/')", path));
        }
        return asList(path.replaceAll("/+", "/").replaceAll("^/", "").split("/"));
    }

    @Nonnull
    @Override
    public Either<FSError, FileInfo> info(@Nonnull String path) {
        return checkedGet(() -> root.findUnder(splitPath(requireNonNull(path))).rMap(FSNode::info));
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> read(@Nonnull String path) {
        return checkedGet(() -> root.findUnder(splitPath(requireNonNull(path))).rFlatMap(FSNode::content));
    }

    @Nonnull
    @Override
    public Either<FSError, List<FileInfo>> ls(@Nonnull String path) {
        return checkedGet(() -> root.findUnder(splitPath(requireNonNull(path))).rFlatMap(FSNode::ls));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> copy(@Nonnull String sourcePath, @Nonnull String destinationPath) {
        return checkedGet(
                () -> {
                    final List<String> sourceSplitPath = splitPath(requireNonNull(sourcePath));
                    final List<String> destinationSplitPath = splitPath(requireNonNull(destinationPath));
                    return isDestinationSubtree(sourceSplitPath, destinationSplitPath)
                            ? Either.left(
                            new FSError(FSError.Type.DESTINATION_IS_SOURCE_SUBTREE, String.format("%s is subtree of %s", destinationPath, sourcePath)))
                            : (
                            root
                                    .findUnder(sourceSplitPath)
                                    .rFlatMap(
                                            src -> src.size() <= free()
                                                    ? doCopy(src, destinationPath, destinationSplitPath)
                                                    : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space"))));
                });
    }

    private static boolean isDestinationSubtree(List<String> sourceSplitPath, List<String> destinationSplitPath) {
        return destinationSplitPath.size() > sourceSplitPath.size() &&
                destinationSplitPath.subList(0, sourceSplitPath.size()).equals(sourceSplitPath);
    }

    private Either<FSError, Unit> doCopy(@Nonnull FSNode src, @Nonnull String destinationPath, List<String> destinationSplitPath) {
        return checkedGet(
                () -> {
                    requireNonNull(src);
                    return root
                            .findUnder(requireNonNull(destinationSplitPath))
                            .flatMap(
                                    __ -> findParentNode(destinationSplitPath)
                                            .rFlatMap(
                                                    parent -> {
                                                        final Either<FSError, Unit> result;
                                                        if (parent.type() != DIRECTORY) {
                                                            result = Either.left(new FSError(
                                                                    FILE_IS_REGULAR, String.format("Shouldn't copy under regular file %s", parent.path())));
                                                        } else {
                                                            src.copyTo(destinationSplitPath.get(destinationSplitPath.size() - 1), parent);
                                                            result = Either.right(Unit.unit());
                                                        }
                                                        return result;
                                                    }),
                                    dst -> src == dst
                                            ? Either.right(Unit.unit())
                                            : Either.left(new FSError(FSError.Type.FILE_ALREADY_EXISTS, String.format("File %s already exists", destinationPath))));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull String path, @Nonnull byte[] content) {
        return checkedGet(
                () -> {
                    requireNonNull(content);
                    return root
                            .findUnder(splitPath(requireNonNull(path)))
                            .rFlatMap(
                                    node -> content.length <= free() + node.size()
                                            ? node.write(content)
                                            : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space")));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull String path, @Nonnull byte[] content) {
        return checkedGet(
                () -> {
                    requireNonNull(path);
                    requireNonNull(content);
                    return root
                            .findUnder(splitPath(path))
                            .rFlatMap(
                                    node -> content.length <= free()
                                            ? node.append(content)
                                            : Either.left(new FSError(FSError.Type.NO_FREE_SPACE, "There is no free space")));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> delete(@Nonnull String path) {
        return checkedGet(
                () -> {
                    final List<String> splitPath = splitPath(requireNonNull(path));
                    return findParentNode(splitPath).rFlatMap(parent -> parent.deleteUnder(splitPath.get(splitPath.size() - 1)));
                });
    }

    @Override
    public long size() {
        return checkedGet(() -> size);
    }

    @Override
    public long used() {
        return checkedGet(root::size);
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> move(@Nonnull String sourcePath, @Nonnull String destinationPath) {
        return checkedGet(() -> {
            final List<String> sourceSplitPath = splitPath(requireNonNull(sourcePath));
            final List<String> destinationSplitPath = splitPath(requireNonNull(destinationPath));
            return isDestinationSubtree(sourceSplitPath, destinationSplitPath)
                    ? Either.left(new FSError(FSError.Type.DESTINATION_IS_SOURCE_SUBTREE, String.format("%s is subtree of %s", destinationPath, sourcePath)))
                    : root.findUnder(sourceSplitPath).rFlatMap((src) -> doMove(src, destinationPath, destinationSplitPath));
        });
    }

    private Either<FSError, Unit> doMove(FSNode src, String destinationPath, List<String> destinationSplitPath) {
        return checkedGet(
                () -> root
                        .findUnder(destinationSplitPath)
                        .flatMap(
                                __ -> findParentNode(destinationSplitPath)
                                        .rFlatMap(
                                                parent -> {
                                                    final Either<FSError, Unit> result;
                                                    if (parent.type() != DIRECTORY) {
                                                        result = Either.left(new FSError(
                                                                FILE_IS_REGULAR, String.format("Shouldn't move under regular file %s", parent.path())));
                                                    } else {
                                                        src.moveTo(destinationSplitPath.get(destinationSplitPath.size() - 1), parent);
                                                        result = Either.right(Unit.unit());
                                                    }
                                                    return result;
                                                }),
                                dst -> src == dst
                                        ? Either.right(Unit.unit())
                                        : Either.left(new FSError(FSError.Type.FILE_ALREADY_EXISTS, String.format("File %s already exists", destinationPath)))));
    }
}