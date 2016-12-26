/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

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

    SimpleFSImpl(long size) {
        this.size = size;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> create(@Nonnull String path, @Nonnull FileType fileType) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, FileInfo> info(@Nonnull String path) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, ByteArray> read(@Nonnull String path) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, List<FileInfo>> ls(@Nonnull String path) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> copy(@Nonnull String sourcePath, @Nonnull String destinationPath) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> write(@Nonnull String path, @Nonnull byte[] content) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> append(@Nonnull String path, @Nonnull byte[] content) {
        return null;
    }

    @Nonnull
    @Override
    public Either<FSError, Unit> delete(@Nonnull String path) {
        return null;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public long used() {
        return 0;
    }
}