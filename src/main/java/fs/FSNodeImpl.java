/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package fs;

import data.ByteArray;
import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Andrey Antipov (andrey.antipov@cxense.com) (2016-12-26 21:50)
 */
final class FSNodeConfig {
    private FSNodeConfig() {
    }

    static FSNode createFileUnder(@Nonnull String name, @Nonnull FSNode parent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static FSNode createDirUnder(@Nonnull String name, @Nonnull FSNode parent) {
        throw new UnsupportedOperationException("Not implemented yet");
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
    }

    @Nonnull
    @Override
    public Optional<FSNode> parent() {
        return Optional.ofNullable(parent);
    }

    @Nonnull
    @Override
    public String name() {
        return name;
    }
}

final class Directory extends AbstractNode {
    private final Map<String, FSNode> children = new HashMap<>();

    Directory(@Nonnull String name, @Nullable FSNode parent) {
        super(name, parent);
    }

    @Nonnull
    @Override
    public Either<FSError, Map<String, FSNode>> children() {
        return Either.right(Collections.unmodifiableMap(children));
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> content() {
        return Either.left(new FSError(FSError.Type.FILE_IS_DIRECTORY, path() + " is directory"));
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> addChild(@Nonnull FSNode child) {
        final Either<FSError, Unit> result;

        final String childName = child.name();
        if (children.containsKey(childName)) {
            result = Either.left(new FSError(FSError.Type.FILE_ALREADY_EXISTS, children.get(childName).path() + " already exists"));
        } else {
            children.put(childName, child);
            result = Either.right(Unit.unit());
        }

        return result;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> removeChild(@Nonnull FSNode child) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull String name, @Nonnull byte[] content) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull String name, @Nonnull byte[] content) {
        return null;
    }

    @Override
    public void rename(@Nonnull String name) {

    }

    @Override
    public FSNode copyUnder(@Nonnull String newName, @Nonnull FSNode parent) {
        return null;
    }

    @Nonnull
    @Override
    public FileType type() {
        return null;
    }

    @Override
    public long size() {
        return 0;
    }
}