package fs;

import static data.ArrayHelper.asSet;
import static fs.FSError.Type.FILE_ALREADY_EXISTS;
import static fs.FSError.Type.FILE_IS_DIRECTORY;
import static fs.FSError.Type.FILE_IS_REGULAR;
import static fs.FSError.Type.FILE_NOT_FOUND;
import static fs.FSError.Type.NO_FREE_SPACE;
import static fs.FSError.Type.PATH_NOT_FOUND;
import static fs.FileType.DIRECTORY;
import static fs.FileType.REGULAR;
import static helpers.TestHelper.addReprToCons;
import static helpers.TestHelper.nop;
import static helpers.TestHelper.provideFail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import data.ByteArray;
import data.Unit;
import helpers.TestHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class FSTest {
    private static final Consumer<FSError> ALREADY_EXISTS_CHECKER = provideErrorTypeChecker(FILE_ALREADY_EXISTS);
    private static final Consumer<FSError> FILE_NOT_FOUND_CHECKER = provideErrorTypeChecker(FILE_NOT_FOUND);
    private static final Consumer<FSError> FILE_IS_REGULAR_CHECKER = provideErrorTypeChecker(FILE_IS_REGULAR);
    private static final Consumer<FSError> FILE_IS_DIRECTORY_CHECKER = provideErrorTypeChecker(FILE_IS_DIRECTORY);
    private static final Consumer<FSError> PATH_NOT_FOUND_CHECKER = provideErrorTypeChecker(PATH_NOT_FOUND);
    private static final Consumer<FSError> NO_FREE_SPACE_CHECKER = provideErrorTypeChecker(NO_FREE_SPACE);
    private static final int FS_SIZE = 128;

    // file and dir names
    private static final String TEST_FILE = "/test_file";
    private static final String TEST_FILE2 = "/test_file2";
    private static final String TEST_DIR = "/test_dir";
    private static final String TEST_DIR2 = "/test_dir2";
    private static final String INNER_FILE = "/inner_file";
    private static final String INNER_DIR = "/inner_dir";
    private static final String EXISTING_FILE = "/existing_file";
    private static final String EXISTING_DIR = "/existing_dir";
    private static final String LARGE = "/large";
    private static final String MOVED_FILE = "/moved_file";
    private static final String MOVED_DIR = "/moved_dir";
    private static final String NOPE = "/nope";
    private static final String MOVED_NOPE = "/moved_nope";

    private FS testFs;

    private static Consumer<FSError> provideErrorTypeChecker(FSError.Type expected) {
        return TestHelper.addReprToCons(
                actual -> assertEquals(actual.type, expected),
                "actual -> assertEquals(actual.type, " + expected + ")");
    }

    private static Consumer<FileInfo> provideInfoChecker(String fullName, FileType type, long size) {
        final FileInfo expected = new FileInfo(fullName, type, size);
        return addReprToCons(
                actual -> assertEquals(actual, expected),
                "actual -> assertEquals(actual, " + expected + ")");
    }

    private void setUp() {
        testFs = FS.init(FS_SIZE).both(
                __ -> {
                    throw new AssertionError("File system initialisation failed");
                },
                x -> x);
    }

    @DataProvider(name = "testCreate")
    private Object[][] data4testCreate() {
        setUp();

        testFs.create(EXISTING_FILE, REGULAR);
        testFs.create(EXISTING_DIR, DIRECTORY);

        final String nope = NOPE;
        return new Object[][]{
                // success
                {EXISTING_DIR + TEST_FILE, REGULAR, provideFail("Should create file"), nop()},
                {EXISTING_DIR + TEST_DIR, DIRECTORY, provideFail("Should create directory"), nop()},
                // file already exists
                {
                        EXISTING_FILE,
                        REGULAR,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't create file over existing file")},
                {
                        EXISTING_FILE,
                        DIRECTORY,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't create directory over existing file")},
                {
                        EXISTING_DIR,
                        REGULAR,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't create file over existing directory")},
                {
                        EXISTING_DIR,
                        DIRECTORY,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't create directory over existing directory")},
                // path not found
                {
                        nope + TEST_FILE,
                        REGULAR,
                        PATH_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't create file located at non existing path")},
                {
                        nope + TEST_DIR,
                        DIRECTORY,
                        PATH_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't create directory located at non existing path")},
        };
    }

    @Test(dataProvider = "testCreate")
    public void testCreate(String path, FileType type, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.create(path, type).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testInfo")
    private Object[][] data4testInfo() {
        setUp();

        testFs.create(TEST_FILE, REGULAR);
        testFs.create(TEST_FILE2, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.write(TEST_FILE, new byte[]{1, 2});

        return new Object[][]{
                {
                        TEST_FILE,
                        provideFail("Should get info from existing file"),
                        provideInfoChecker(TEST_FILE, REGULAR, 0)},
                {
                        TEST_FILE2,
                        provideFail("Should get info from existing file"),
                        provideInfoChecker(TEST_FILE2, REGULAR, 2)},
                {
                        TEST_DIR,
                        provideFail("Should get info from existing directory"),
                        provideInfoChecker(TEST_DIR, DIRECTORY, 0)},
                {
                        NOPE,
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't get info from non existent file")},
        };
    }

    @Test(dataProvider = "testInfo")
    public void testInfo(String path, Consumer<FSError> leftChecker, Consumer<FileInfo> rightChecker) throws Exception {
        testFs.info(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testRead")
    private Object[][] data4testRead() {
        setUp();

        testFs.create(TEST_FILE, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.write(TEST_FILE, new byte[]{1, 2});

        return new Object[][]{
                {
                        TEST_FILE,
                        provideFail("Should read existing file"),
                        TestHelper.<ByteArray>addReprToCons(
                                actual -> assertEquals(actual, new ByteArray(new byte[]{1, 2})),
                                "actual -> assertEquals(actual, new ByteArray(new byte[]{1, 2}))")},
                {NOPE, FILE_NOT_FOUND_CHECKER, provideFail("Shouldn't read from non existent file")},
                {TEST_DIR, FILE_IS_DIRECTORY_CHECKER, provideFail("Shouldn't read from directory")},
        };
    }

    @Test(dataProvider = "testRead")
    public void testRead(String path, Consumer<FSError> leftChecker, Consumer<ByteArray> rightChecker) throws Exception {
        testFs.read(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testLs")
    private Object[][] data4testLs() {
        setUp();

        testFs.create(TEST_DIR, DIRECTORY);
        testFs.create(TEST_DIR + INNER_FILE, REGULAR);
        testFs.create(TEST_DIR + INNER_DIR, DIRECTORY);

        return new Object[][]{
                // existing directory
                {
                        TEST_DIR,
                        provideFail("Should list directory content"),
                        addReprToCons(
                                actual -> assertEquals(
                                        asSet(actual),
                                        asSet(
                                                new FileInfo(TEST_DIR + INNER_FILE, REGULAR, 0),
                                                new FileInfo(TEST_DIR + INNER_DIR, DIRECTORY, 0))),
                                "actual -> assertEquals(" +
                                        "asSet(actual)," +
                                        "asSet(" +
                                        "new FileInfo(\"" + TEST_DIR + INNER_FILE + "\", REGULAR, 0)," +
                                        "new FileInfo(\"" + TEST_DIR + INNER_DIR + "\", DIRECTORY, 0)))")},
                // non existing directory
                {
                        NOPE,
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't list non existing directory")},
                // regular file
                {
                        TEST_DIR + INNER_FILE,
                        FILE_IS_REGULAR_CHECKER,
                        provideFail("Shouldn't list regular file")},

        };
    }

    @Test(dataProvider = "testLs")
    public void testLs(String path, Consumer<FSError> leftChecker, Consumer<FileInfo[]> rightChecker) throws Exception {
        testFs.ls(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testCopy")
    private Object[][] data4testCopy() {
        setUp();

        testFs.create(TEST_DIR, DIRECTORY);
        testFs.create(TEST_DIR + INNER_FILE, REGULAR);
        testFs.write(TEST_DIR + INNER_FILE, new byte[]{1, 2});
        testFs.create(LARGE, REGULAR);
        testFs.write(LARGE, new byte[FS_SIZE / 2 + 1]);

        return new Object[][]{
                {
                        TEST_DIR + INNER_FILE, TEST_FILE,
                        provideFail("Should copy existent file"),
                        provideFileChecker(TEST_FILE, new FileInfo(TEST_FILE, REGULAR, 2), new byte[]{1, 2})},
                {
                        TEST_DIR + INNER_FILE, TEST_DIR + INNER_FILE,
                        provideFail("Should copy existent file"),
                        provideFileChecker(
                                TEST_DIR + INNER_FILE,
                                new FileInfo(TEST_DIR + INNER_FILE, REGULAR, 2),
                                new byte[]{1, 2})},
                {
                        TEST_DIR, TEST_DIR2,
                        provideFail("Should copy existent directory"),
                        provideDirChecker(
                                TEST_DIR2,
                                new FileInfo(TEST_DIR2, DIRECTORY, 0),
                                new FileInfo(TEST_DIR2 + INNER_FILE, REGULAR, 2))},
                {
                        TEST_DIR, TEST_DIR,
                        provideFail("Should copy existent directory"),
                        provideDirChecker(
                                TEST_DIR,
                                new FileInfo(TEST_DIR, DIRECTORY, 0),
                                new FileInfo(TEST_DIR + INNER_FILE, REGULAR, 2))},
                {
                        NOPE, TEST_FILE,
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't copy non existent file")},
                {
                        TEST_DIR + INNER_FILE, LARGE,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't copy into existent file")},
                {
                        LARGE, TEST_FILE,
                        NO_FREE_SPACE_CHECKER,
                        provideFail("Shouldn't copy file larger than half of total file system size")},
        };
    }

    @Test(dataProvider = "testCopy")
    public void testCopy(String sourcePath,
                         String destinationPath,
                         Consumer<FSError> leftChecker,
                         Consumer<Unit> rightChecker) throws Exception {
        testFs.copy(sourcePath, destinationPath).onBoth(leftChecker, rightChecker);
    }

    private Consumer<Unit> provideDirChecker(String path, FileInfo expectedInfo, FileInfo... expectedContent) {
        return addReprToCons(
                __ -> checkDir(path, expectedInfo, expectedContent),
                "__-> checkDir(" + path + ", " + expectedInfo + ", " + Arrays.toString(expectedContent) + ")");
    }

    private void checkDir(String path, FileInfo expectedInfo, FileInfo[] expectedContent) {
        assertEquals(
                testFs.info(path).elseGetRight(
                        () -> {
                            throw new AssertionError("Should get info from existing directory");
                        }),
                expectedInfo);
        assertEquals(
                testFs.ls(path).elseGetRight(
                        () -> {
                            throw new AssertionError("Should list existing directory");
                        }),
                expectedContent);
    }

    private Consumer<Unit> provideFileChecker(String path, FileInfo expectedInfo, byte[] expectedContent) {
        return addReprToCons(
                __ -> checkFile(path, expectedInfo, expectedContent),
                "__ -> checkFile(" + path + ", " + expectedInfo + ", " + Arrays.toString(expectedContent) + ")");
    }

    private void checkFile(String path, FileInfo expectedInfo, byte[] expectedContent) {
        assertEquals(
                testFs.info(path).elseGetRight(
                        () -> {
                            throw new AssertionError("Should get info from existing file");
                        }),
                expectedInfo);
        assertEquals(
                testFs.read(path).elseGetRight(
                        () -> {
                            throw new AssertionError("Should read existing file");
                        }).get(),
                expectedContent);
    }

    @DataProvider(name = "testWrite")
    private Object[][] data4testWrite() {
        setUp();
        testFs.create(TEST_FILE, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.write(TEST_FILE, new byte[]{1, 2});
        return new Object[][]{
                {
                        TEST_FILE, new ByteArray(new byte[]{3, 4, 5}),
                        provideFail("Should write to existing file"),
                        TestHelper.<Unit>addReprToCons(
                                __ -> testFs.read(TEST_FILE).onRight(actual -> assertEquals(actual.get(),
                                        new byte[]{3, 4, 5})),
                                "__ -> testFs.read(\"/test_file\").onRight(actual -> assertEquals(actual.get()," +
                                        "new byte[]{3, 4, 5}))")},
                {
                        NOPE, new ByteArray(new byte[]{1, 2, 3}),
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't write to non existent file")},
                {
                        TEST_DIR, new ByteArray(new byte[]{1, 2, 3}),
                        FILE_IS_DIRECTORY_CHECKER,
                        provideFail("Shouldn't write to directory")},
                {
                        TEST_FILE, new ByteArray(new byte[FS_SIZE + 1]),
                        NO_FREE_SPACE_CHECKER,
                        provideFail("Shouldn't write content larger than file system")},
        };
    }

    @Test(dataProvider = "testWrite")
    public void testWrite(String path, ByteArray content, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.write(path, content.get()).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testAppend")
    private Object[][] data4testAppend() {
        setUp();
        testFs.create(TEST_FILE, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.write(TEST_FILE, new byte[]{1, 2});
        return new Object[][]{
                {
                        TEST_FILE, new ByteArray(new byte[]{3}),
                        provideFail("Should append to existing file"),
                        TestHelper.<Unit>addReprToCons(
                                __ -> testFs.read(TEST_FILE).onRight(actual -> assertEquals(actual.get(),
                                        new byte[]{1, 2, 3})),
                                "__ -> testFs.read(\"/test_file\").onRight(actual -> assertEquals(actual.get()," +
                                        "new byte[]{1, 2, 3}))")},
                {
                        NOPE, new ByteArray(new byte[]{1, 2, 3}),
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't append to non existent file")},
                {
                        TEST_DIR, new ByteArray(new byte[]{1, 2, 3}),
                        FILE_IS_DIRECTORY_CHECKER,
                        provideFail("Shouldn't append to directory")},
                {
                        TEST_FILE, new ByteArray(new byte[FS_SIZE + 1]),
                        NO_FREE_SPACE_CHECKER,
                        provideFail("Shouldn't append content larger than file system")},
        };
    }

    @Test(dataProvider = "testAppend")
    public void testAppend(String path, ByteArray content, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.append(path, content.get()).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testDelete")
    private Object[][] data4testDelete() {
        testFs.create(TEST_FILE, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.create(TEST_DIR +
                INNER_FILE, REGULAR);
        return new Object[][]{
                {TEST_FILE, provideFail("Should delete file"), provideDeleteChecker(TEST_FILE)},
                {TEST_DIR, provideFail("Should delete directory"), provideDeleteChecker(TEST_DIR)},
                {NOPE, FILE_NOT_FOUND_CHECKER, provideFail("Non existent file shouldn't be deleted")},
        };
    }

    private Consumer<Unit> provideDeleteChecker(String path) {
        return addReprToCons(
                __ -> testFs.info(path).onRight(___ -> fail("File " + path + "should be deleted")),
                "__ -> testFs.info(" + path + ").onRight(___ -> fail(\"File \" + " + path + " + \"should be deleted\"))");
    }

    @Test(dataProvider = "testDelete")
    public void testDelete(String path, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker) throws Exception {
        testFs.delete(path).onBoth(leftChecker, rightChecker);
    }

    @Test
    public void testSize() throws Exception {
        setUp();
        assertEquals(testFs.size(), FS_SIZE);
    }

    @Test
    public void testUsed() throws Exception {
        setUp();
        final String testFile = TEST_FILE;
        testFs.create(testFile, REGULAR);
        final int fileMinimalSize = FS_SIZE / 2;
        testFs.write(testFile, new byte[fileMinimalSize]);
        assertTrue(testFs.used() >= fileMinimalSize);
    }

    @DataProvider(name = "testMove")
    private Object[][] data4testMove() {
        setUp();

        testFs.create(TEST_FILE, REGULAR);
        testFs.create(EXISTING_FILE, REGULAR);
        testFs.create(TEST_DIR, DIRECTORY);
        testFs.create(TEST_DIR + INNER_FILE, REGULAR);
        testFs.write(TEST_DIR + INNER_FILE, new byte[]{1, 2});
        testFs.create(TEST_DIR2, DIRECTORY);
        testFs.create(TEST_DIR2 + INNER_FILE, REGULAR);
        testFs.write(TEST_DIR2 + INNER_FILE, new byte[]{1, 2});

        return new Object[][]{
                {
                        TEST_DIR + INNER_FILE, MOVED_FILE,
                        provideFail("Should move existent file"),
                        provideFileChecker(MOVED_FILE, new FileInfo(TEST_FILE, REGULAR, 2), new byte[]{1, 2})},
                {
                        TEST_DIR2, MOVED_DIR,
                        provideFail("Should move existent directory"),
                        provideDirChecker(
                                MOVED_DIR,
                                new FileInfo(MOVED_DIR, DIRECTORY, 0),
                                new FileInfo(MOVED_DIR + INNER_FILE, REGULAR, 2))},
                {
                        NOPE, MOVED_NOPE,
                        FILE_NOT_FOUND_CHECKER,
                        provideFail("Shouldn't move non existent file")},
                {
                        TEST_FILE, EXISTING_FILE,
                        ALREADY_EXISTS_CHECKER,
                        provideFail("Shouldn't move into existent file")},
        };
    }

    @Test(dataProvider = "testMove")
    public void testMove(String sourcePath,
                         String destinationPath,
                         Consumer<FSError> leftChecker,
                         Consumer<Unit> rightChecker) throws Exception {
        testFs.move(sourcePath, destinationPath).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testInit")
    private Object[][] data4testInit() {
        return new Object[][]{
                {0L, provideFail("Should init file system with zero size"), nop()},
                {1L, provideFail("Should init file system with positive size"), nop()},
                {Long.MAX_VALUE, provideFail("Should init file system with positive size"), nop()},
                {-1L, nop(), provideFail("Shouldn't init file system with negative size")},
        };
    }

    @Test(dataProvider = "testInit")
    public void testInit(long size, Consumer<FSError> leftChecker, Consumer<FS> rightChecker) throws Exception {
        FS.init(size).onBoth(leftChecker, rightChecker);
    }

}