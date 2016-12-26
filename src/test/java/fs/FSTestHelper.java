/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs;

import static fs.FSError.Type.FILE_ALREADY_EXISTS;
import static fs.FSError.Type.FILE_IS_DIRECTORY;
import static fs.FSError.Type.FILE_IS_REGULAR;
import static fs.FSError.Type.FILE_NOT_FOUND;
import static fs.FSError.Type.NO_FREE_SPACE;
import static fs.FSError.Type.PATH_NOT_FOUND;
import static helpers.TestHelper.addReprToCons;
import static org.testng.Assert.assertEquals;

import helpers.TestHelper;

import java.util.function.Consumer;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-25)
 */
final class ResultCheckers {
    static final Consumer<FSError> ALREADY_EXISTS_CHECKER = provideErrorTypeChecker(FILE_ALREADY_EXISTS);
    static final Consumer<FSError> FILE_NOT_FOUND_CHECKER = provideErrorTypeChecker(FILE_NOT_FOUND);
    static final Consumer<FSError> FILE_IS_REGULAR_CHECKER = provideErrorTypeChecker(FILE_IS_REGULAR);
    static final Consumer<FSError> FILE_IS_DIRECTORY_CHECKER = provideErrorTypeChecker(FILE_IS_DIRECTORY);
    static final Consumer<FSError> PATH_NOT_FOUND_CHECKER = provideErrorTypeChecker(PATH_NOT_FOUND);
    static final Consumer<FSError> NO_FREE_SPACE_CHECKER = provideErrorTypeChecker(NO_FREE_SPACE);

    private ResultCheckers() {
    }

    private static Consumer<FSError> provideErrorTypeChecker(FSError.Type expected) {
        return TestHelper.addReprToCons(
                actual -> assertEquals(actual.type, expected),
                "actual -> assertEquals(actual.type, " + expected + ")");
    }

    static Consumer<FileInfo> provideInfoChecker(String fullName, FileType type, long size) {
        final FileInfo expected = new FileInfo(fullName, type, size);
        return addReprToCons(
                actual -> assertEquals(actual, expected),
                "actual -> assertEquals(actual, " + expected + ")");
    }
}

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-25)
 */
final class TestFileNames {
    static final String TEST_FILE = "/test_file";
    static final String TEST_FILE2 = "/test_file2";
    static final String TEST_DIR = "/test_dir";
    static final String TEST_DIR2 = "/test_dir2";
    static final String INNER_FILE = "/inner_file";
    static final String INNER_DIR = "/inner_dir";
    static final String EXISTING_FILE = "/existing_file";
    static final String EXISTING_DIR = "/existing_dir";
    static final String LARGE = "/large";
    static final String MOVED_FILE = "/moved_file";
    static final String MOVED_DIR = "/moved_dir";
    static final String NOPE = "/nope";
    static final String MOVED_NOPE = "/moved_nope";

    static final String INNER_FILE_IN_TEST_DIR2 = TEST_DIR2 + INNER_FILE;
    static final String INNER_FILE_IN_TEST_DIR = TEST_DIR + INNER_FILE;
    static final String INNER_DIR_IN_TEST_DIR = TEST_DIR + INNER_DIR;
    static final String TEST_DIR_IN_NOPE = NOPE + TEST_DIR;
    static final String TEST_FILE_IN_NOPE = NOPE + TEST_FILE;

    private TestFileNames() {
    }
}
