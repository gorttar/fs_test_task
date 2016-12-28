/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static fs.FSError.Type.FILE_ALREADY_EXISTS;
import static fs.FSError.Type.FILE_IS_DIRECTORY;
import static fs.FSError.Type.FILE_IS_REGULAR;
import static fs.FSError.Type.PATH_NOT_FOUND;
import static fs.FileType.DIRECTORY;
import static fs.FileType.REGULAR;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import check.CheckHelper;
import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
final class FSNodeConfig {
    private FSNodeConfig() {
    }

    static FSNode createFileUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new File(requireNonNull(name), requireNonNull(parent));
    }

    static FSNode createDirUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new Directory(requireNonNull(name), requireNonNull(parent));
    }

    static FSNode createRoot() {
        return new Root();
    }
}

abstract class AbstractNode implements FSNode {
    @Nonnull
    private String name;
    @Nullable
    private FSNode parent;

    AbstractNode(@Nonnull String name, @Nullable FSNode parent) {
        this.name = requireNonNull(name);
        this.parent = parent;
        checkInvariants();
    }

    @Nonnull
    @Override
    public Optional<FSNode> getParent() {
        return checkedGet(() -> Optional.ofNullable(this.parent));
    }

    @Nonnull
    @Override
    public String name() {
        return checkedGet(() -> this.name);
    }

    @Override
    public void moveTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        checkedGet(
                () -> {
                    getParent().ifPresent(oldParent -> oldParent.deleteUnder(name()));

                    name = requireNonNull(newName);
                    parent = requireNonNull(newParent);

                    newParent.link(this);
                    return null;
                });
    }

    private void checkInvariants() {
        assert true;
    }

    private <T> T checkedGet(Supplier<? extends T> payload) {
        return CheckHelper.checkedGet(payload, this::checkInvariants);
    }
}

final class Root extends Directory {
    Root() {
        super("", null);
        checkInvariants();
    }

    private void checkInvariants() {
        assert true;
    }

    @SuppressWarnings("UnusedReturnValue")
    private <T> T checkedGet(Supplier<? extends T> payload) {
        return CheckHelper.checkedGet(payload, this::checkInvariants);
    }

    @Override
    public void moveTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        checkedGet(
                () -> {
                    requireNonNull(newName);
                    requireNonNull(newParent);
                    throw new UnsupportedOperationException("Root shouldn't be moved");
                });
    }
}

class Directory extends AbstractNode {
    private final Map<String, FSNode> children = new HashMap<>();

    Directory(@Nonnull String name, @Nullable FSNode parent) {
        super(requireNonNull(name), parent);
        checkInvariants();
    }

    @Nonnull
    @Override
    public Either<FSError, List<FileInfo>> ls() {
        return checkedGet(() -> Either.right(children.values().stream().map(FSNode::info).collect(Collectors.toList())));
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> content() {
        return checkedGet(() -> Either.left(new FSError(FSError.Type.FILE_IS_DIRECTORY, String.format("Shouldn't read from directory %s", path()))));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull byte[] content) {
        return checkedGet(
                () -> {
                    requireNonNull(content);
                    return Either.left(new FSError(FILE_IS_DIRECTORY, String.format("Shouldn't write to directory %s", path())));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull byte[] content) {
        return checkedGet(
                () -> {
                    requireNonNull(content);
                    return Either.left(new FSError(FILE_IS_DIRECTORY, String.format("Shouldn't append to directory %s", path())));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath) {
        return checkedGet(
                () -> {
                    requireNonNull(splitPath);
                    final Either<FSError, FSNode> result;

                    if (splitPath.isEmpty()) {
                        result = Either.right(this);
                    } else {
                        final String childName = splitPath.get(0);

                        result = Optional
                                .ofNullable(children.get(childName))
                                .map(child -> child.findUnder(splitPath.subList(1, splitPath.size())))
                                .orElseGet(() -> Either.left(new FSError(PATH_NOT_FOUND, String.format("Child %s not found under %s", childName, path()))));
                    }

                    return result;
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType) {
        return checkedGet(
                () -> {
                    requireNonNull(fileType);
                    requireNonNull(name);
                    final Either<FSError, Unit> result;

                    if (children.containsKey(name)) {
                        result = Either.left(new FSError(FILE_ALREADY_EXISTS, String.format("File %s already exists", path() + '/' + name)));
                    } else {
                        link(FSNode.createUnder(name, fileType, this));
                        result = Either.right(Unit.unit());
                    }

                    return result;
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> deleteUnder(@Nonnull String name) {
        return checkedGet(
                () -> {
                    requireNonNull(name);
                    final Either<FSError, Unit> result;

                    if (!children.containsKey(name)) {
                        result = Either.left(new FSError(PATH_NOT_FOUND, String.format("Child %s not found under %s", name, path())));
                    } else {
                        children.remove(name);
                        result = Either.right(Unit.unit());
                    }

                    return result;
                });
    }

    @Override
    public void copyTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        checkedGet(
                () -> {
                    requireNonNull(newParent).createUnder(requireNonNull(newName), DIRECTORY);
                    final FSNode copy = newParent.findUnder(singletonList(newName))
                            .elseGetRight(
                                    () -> {
                                        throw new IllegalStateException(String.format("Can't find file %s/%s", newParent.path(), newName));
                                    });
                    children.values().forEach(child -> child.copyTo(child.name(), copy));
                    return copy;
                });
    }

    @Nonnull
    @Override
    public FileType type() {
        return checkedGet(() -> DIRECTORY);
    }

    @Override
    public long size() {
        return checkedGet(
                () -> children
                        .values()
                        .stream()
                        .map(FSNode::size)
                        .reduce(0L, (x, y) -> x + y));
    }

    @Override
    public void link(@Nonnull FSNode child) {
        children.put(requireNonNull(child).name(), child);
    }

    private void checkInvariants() {
        assert true;
    }

    private <T> T checkedGet(Supplier<? extends T> payload) {
        return CheckHelper.checkedGet(payload, this::checkInvariants);
    }
}

final class File extends AbstractNode {
    private final List<ByteArray> chunks = new LinkedList<>();
    private int size = 0;

    File(@Nonnull String name, @Nonnull FSNode parent) {
        super(requireNonNull(name), requireNonNull(parent));
        checkInvariants();
    }

    @Nonnull
    @Override
    public Either<FSError, List<FileInfo>> ls() {
        return checkedGet(() -> Either.left(new FSError(FILE_IS_REGULAR, String.format("Shouldn't list regular file %s", path()))));
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> content() {
        return checkedGet(() -> {
            final byte[] result = new byte[size];

            int offset = 0;
            for (ByteArray chunk : chunks) {
                final byte[] bytes = chunk.get();
                System.arraycopy(bytes, 0, result, offset, bytes.length);
                offset += bytes.length;
            }

            return Either.right(new ByteArray(result));
        });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull byte[] content) {
        return checkedGet(() -> {
            requireNonNull(content);
            chunks.clear();
            size = 0;
            return append(content);
        });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull byte[] content) {
        return checkedGet(() -> {
            chunks.add(new ByteArray(requireNonNull(content)));
            size += content.length;
            return Either.right(Unit.unit());
        });
    }

    @Nonnull
    @Override
    public Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath) {
        return checkedGet(
                () -> requireNonNull(splitPath).isEmpty()
                        ? Either.right(this)
                        : Either.left(new FSError(FILE_IS_REGULAR, String.format("Shouldn't find under regular file %s", path()))));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType) {
        return checkedGet(
                () -> {
                    requireNonNull(name);
                    requireNonNull(fileType);
                    return Either.left(new FSError(FILE_IS_REGULAR, String.format("Shouldn't create under regular file %s", path())));
                });
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> deleteUnder(@Nonnull String name) {
        return checkedGet(() -> {
            requireNonNull(name);
            return Either.left(new FSError(FILE_IS_REGULAR, String.format("Shouldn't delete under regular file %s", path())));
        });
    }

    @Override
    public void copyTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        checkedGet(
                () -> {
                    requireNonNull(newParent).createUnder(requireNonNull(newName), REGULAR);
                    final FSNode copy = newParent.findUnder(singletonList(newName))
                            .elseGetRight(
                                    () -> {
                                        throw new IllegalStateException(String.format("Can't find file %s/%s", newParent.path(), newName));
                                    });
                    content().onRight(byteArray -> copy.write(byteArray.get()));
                    return null;
                });
    }

    @Nonnull
    @Override
    public FileType type() {
        return checkedGet(() -> REGULAR);
    }

    @Override
    public long size() {
        return checkedGet(() -> size);
    }

    @Override
    public void link(@Nonnull FSNode child) {
        checkedGet(
                () -> {
                    requireNonNull(child);
                    throw new UnsupportedOperationException(String.format("Shouldn't link child to regular file %s", path()));
                });
    }

    private void checkInvariants() {
        assert size == chunks.stream().map(ByteArray::length).reduce(0, (x, y) -> x + y);
    }

    private <T> T checkedGet(Supplier<? extends T> payload) {
        return CheckHelper.checkedGet(payload, this::checkInvariants);
    }

}