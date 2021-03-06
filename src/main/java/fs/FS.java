/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static fs.FSError.Type.FS_CREATION_FAILED;
import static java.util.Objects.requireNonNull;

import data.ByteArray;
import data.either.Either;
import data.Unit;
import fs.impl.FSConfig;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * file system API interface
 * all file systems should implement it
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-20)
 */
@SuppressWarnings("WeakerAccess")
public interface FS {
    /**
     * @param path     full path to file
     * @param fileType file type
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#FILE_ALREADY_EXISTS} if there is a file at path
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no directory tree to the given path
     * {@link FSError.Type#FILE_IS_REGULAR} if you are trying to create file under regular file
     */
    @Nonnull
    Either<FSError, Unit> create(@Nonnull String path, @Nonnull FileType fileType);

    /**
     * @param path full path to file
     * @return either an instance of {@link FileInfo} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     */
    @Nonnull
    Either<FSError, FileInfo> info(@Nonnull String path);

    /**
     * @param path full path to file
     * @return either byte array filled with file content or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     * {@link FSError.Type#FILE_IS_DIRECTORY} if you are trying to read from directory
     */
    @Nonnull
    Either<FSError, ByteArray> read(@Nonnull String path);

    /**
     * @param path full path to directory
     * @return either array of {@link FileInfo} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     * {@link FSError.Type#FILE_IS_REGULAR} if you are trying to get list of files in regular file
     */
    @Nonnull
    Either<FSError, List<FileInfo>> ls(@Nonnull String path);

    /**
     * copies file or directory with it's subtree to another location
     *
     * @param sourcePath      full path to source file
     * @param destinationPath full path to destination
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at source path
     * {@link FSError.Type#FILE_ALREADY_EXISTS} if there is a file at destination path
     * {@link FSError.Type#NO_FREE_SPACE} if there is no free space in file system
     * {@link FSError.Type#FILE_IS_REGULAR} if you are trying to copy under regular file
     */
    @Nonnull
    Either<FSError, Unit> copy(@Nonnull String sourcePath, @Nonnull String destinationPath);

    /**
     * rewrites file with new content
     *
     * @param path    full path to file
     * @param content new file's content
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     * {@link FSError.Type#FILE_IS_DIRECTORY} if you are trying to write directory
     * {@link FSError.Type#NO_FREE_SPACE} if there is no free space in file system
     */
    @Nonnull
    Either<FSError, Unit> write(@Nonnull String path, @Nonnull byte[] content);

    /**
     * appends content to the end of file
     *
     * @param path    full path to file
     * @param content to append
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     * {@link FSError.Type#FILE_IS_DIRECTORY} if you are trying to write directory
     * {@link FSError.Type#NO_FREE_SPACE} if there is no free space in file system
     */
    @Nonnull
    Either<FSError, Unit> append(@Nonnull String path, @Nonnull byte[] content);

    /**
     * deletes file or directory with it's subtree
     *
     * @param path full path to file
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at path
     */
    @Nonnull
    Either<FSError, Unit> delete(@Nonnull String path);

    /**
     * @return total size of file system in bytes
     */
    long size();

    /**
     * @return size in bytes used by files
     */
    long used();

    /**
     * @return free space in bytes
     */
    default long free() {
        return size() - used();
    }

    /**
     * copies file or directory with it's subtree to another location
     *
     * @param sourcePath      full path to source file
     * @param destinationPath full path to destination
     * @return either {@link Unit#unit()} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#PATH_NOT_FOUND} if there is no file at source path
     * {@link FSError.Type#FILE_ALREADY_EXISTS} if there is a file at destination path
     * {@link FSError.Type#NO_FREE_SPACE} if there is no free space in file system
     * {@link FSError.Type#FILE_IS_REGULAR} if you are trying to copy under regular file
     * <p>
     * This is default implementation based on {@link #copy(String, String)} and {@link #delete(String)}
     * it should be overridden in {@link FS} implementations for optimal resource (RAM, CPU etc) consumption
     */
    @Nonnull
    default Either<FSError, Unit> move(@Nonnull String sourcePath, @Nonnull String destinationPath) {
        return copy(requireNonNull(sourcePath), requireNonNull(destinationPath)).rFlatMap(__ -> delete(sourcePath));
    }

    /**
     * initializes instance of file system
     *
     * @param size of file system in bytes
     * @return either an instance of {@link FS} or an instance of {@link FSError}
     * <p>
     * possible error types:
     * {@link FSError.Type#FS_CREATION_FAILED} if file system creation failed eg negative size is passed
     */
    @Nonnull
    static Either<FSError, FS> init(long size) {
        return size >= 0 ? Either.right(FSConfig.init(size)) : Either.left(new FSError(FS_CREATION_FAILED, "Can't create file system with negative size"));
    }
}