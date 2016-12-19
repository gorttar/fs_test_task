/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import data.Either;
import data.Unit;

import javax.annotation.Nonnull;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-20)
 */
public interface FS {
    /**
     * creates file
     *
     * @param path     full path to created file
     * @param fileType file type
     * @return either containing {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#FILE_ALREADY_EXISTS} if there is only file at given path
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no directory tree to the given path
     */
    Either<FSError, Unit> create(@Nonnull String path, @Nonnull FileType fileType);

    Either<FSError, Stream<Byte>> read(@Nonnull String path);

    Either<FSError, Unit> copy(@Nonnull String sourcePath, String destinationPath);

    Either<FSError, Unit> rewrite(@Nonnull Supplier<byte[]> contentSupplier);

    Either<FSError, Unit> append(@Nonnull Supplier<byte[]> contentSupplier);

    Either<FSError, Unit> delete(@Nonnull String path);

    default Either<FSError, Unit> move(@Nonnull String sourcePath, String destinationPath) {
        return copy(sourcePath, destinationPath).rFlatMap(__ -> delete(sourcePath));
    }
}