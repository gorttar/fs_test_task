/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static fs.FSError.Type.FILE_ALREADY_EXISTS;
import static fs.FSError.Type.FILE_IS_DIRECTORY;
import static fs.FSError.Type.FILE_IS_REGULAR;
import static fs.FSError.Type.FILE_NOT_FOUND;
import static fs.FSError.Type.PATH_NOT_FOUND;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
final class FSNodeConfig {
    private FSNodeConfig() {
    }

    static FSNode createFileUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new File(name, parent);
    }

    static FSNode createDirUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new Directory(name, parent);
    }

    static FSNode createRoot() {
        return new Root();
    }
}

abstract class AbstractNode implements FSNode {
    @Nonnull
    private final String name;
    @Nullable
    private final FSNode parent;

    AbstractNode(@Nonnull String name, @Nullable FSNode parent) {
        this.name = name;
        this.parent = parent;
        checkInvariants();
    }

    @Nonnull
    @Override
    public Optional<FSNode> parent() {
        checkInvariants();
        return Optional.ofNullable(parent);
    }

    @Nonnull
    @Override
    public String name() {
        checkInvariants();
        return name;
    }

    private void checkInvariants() {
    }
}

final class Root extends Directory {
    Root() {
        super("", null);
        checkInvariants();
    }

    @Override
    public void rename(@Nonnull String name) {
        checkInvariants();
        throw new UnsupportedOperationException("Root node shouldn't be renamed");
    }

    private void checkInvariants() {
    }
}

class Directory extends AbstractNode {
    private final Map<String, FSNode> children = new HashMap<>();

    Directory(@Nonnull String name, @Nullable FSNode parent) {
        super(name, parent);
        checkInvariants();
    }

    @Nonnull
    @Override
    public Either<FSError, Map<String, FSNode>> children() {
        checkInvariants();
        return Either.right(Collections.unmodifiableMap(children));
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> content() {
        checkInvariants();
        return Either.left(new FSError(FSError.Type.FILE_IS_DIRECTORY, String.format("Shouldn't read from directory %s", path())));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> addChild(@Nonnull FSNode child) {
        final Either<FSError, Unit> result;

        final String childName = child.name();
        if (children.containsKey(childName)) {
            result = Either.left(new FSError(FILE_ALREADY_EXISTS, children.get(childName).path() + " already exists"));
        } else {
            children.put(childName, child);
            result = Either.right(Unit.unit());
        }

        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> removeChild(@Nonnull FSNode child) {
        checkInvariants();
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull byte[] content) {
        checkInvariants();
        return Either.left(new FSError(FILE_IS_DIRECTORY, String.format("Shouldn't write to directory %s", path())));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull byte[] content) {
        checkInvariants();
        return Either.left(new FSError(FILE_IS_DIRECTORY, String.format("Shouldn't append to directory %s", path())));
    }

    @Override
    public void rename(@Nonnull String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath) {
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

        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType) {
        final Either<FSError, Unit> result;

        if (children.containsKey(name)) {
            result = Either.left(new FSError(FILE_ALREADY_EXISTS, String.format("File %s already exists", path() + '/' + name)));
        } else {
            final FSNode child = FSNode.createUnder(name, fileType, this);
            children.put(name, child);
            result = Either.right(Unit.unit());
        }

        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> deleteUnder(@Nonnull String name) {
        final Either<FSError, Unit> result;

        if (!children.containsKey(name)) {
            result = Either.left(new FSError(FILE_NOT_FOUND, String.format("Child %s not found under %s", name, path())));
        } else {
            children.remove(name);
            result = Either.right(Unit.unit());
        }

        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public FSNode copyTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public FileType type() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long size() {
        final Long result = children
                .values()
                .stream()
                .map(FSNode::size)
                .reduce(0L, (x, y) -> x + y);
        checkInvariants();
        return result;
    }

    private void checkInvariants() {
    }
}

final class File extends AbstractNode {
    private final List<ByteArray> chunks = new LinkedList<>();
    private int size = 0;

    File(@Nonnull String name, @Nullable FSNode parent) {
        super(name, parent);
        checkInvariants();
    }

    private void checkInvariants() {
        assert size == chunks.stream().map(ByteArray::length).reduce(0, (x, y) -> x + y);
    }

    @Nonnull
    @Override
    public Either<FSError, Map<String, FSNode>> children() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> content() {
        final byte[] result = new byte[size];

        int offset = 0;
        for (ByteArray chunk : chunks) {
            final byte[] bytes = chunk.get();
            System.arraycopy(bytes, 0, result, offset, bytes.length);
            offset += bytes.length;
        }

        checkInvariants();
        return Either.right(new ByteArray(result));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> addChild(@Nonnull FSNode child) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> removeChild(@Nonnull FSNode child) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull byte[] content) {
        chunks.clear();
        size = 0;
        final Either<FSError, Unit> result = append(content);
        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull byte[] content) {
        chunks.add(new ByteArray(content));
        size += content.length;
        checkInvariants();
        return Either.right(Unit.unit());
    }

    @Override
    public void rename(@Nonnull String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, FSNode> findUnder(@Nonnull List<String> splitPath) {
        final Either<FSError, FSNode> result = splitPath.isEmpty()
                ? Either.right(this)
                : Either.left(new FSError(FILE_IS_REGULAR, String.format("Shouldn't find under regular file %s", path())));
        checkInvariants();
        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> createUnder(@Nonnull String name, @Nonnull FileType fileType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> deleteUnder(@Nonnull String name) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public FSNode copyTo(@Nonnull String newName, @Nonnull FSNode newParent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Nonnull
    @Override
    public FileType type() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long size() {
        return size;
    }
}