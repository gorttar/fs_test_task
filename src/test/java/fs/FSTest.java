package fs;

import static fs.FileType.DIRECTORY;
import static fs.FileType.REGULAR;
import static fs.ResultCheckers.ALREADY_EXISTS_CHECKER;
import static fs.ResultCheckers.FILE_IS_DIRECTORY_CHECKER;
import static fs.ResultCheckers.FILE_IS_REGULAR_CHECKER;
import static fs.ResultCheckers.FILE_NOT_FOUND_CHECKER;
import static fs.ResultCheckers.NO_FREE_SPACE_CHECKER;
import static fs.ResultCheckers.PATH_NOT_FOUND_CHECKER;
import static fs.ResultCheckers.provideInfoChecker;
import static fs.TestFileNames.EXISTING_DIR;
import static fs.TestFileNames.EXISTING_FILE;
import static fs.TestFileNames.INNER_DIR_IN_TEST_DIR;
import static fs.TestFileNames.INNER_FILE;
import static fs.TestFileNames.INNER_FILE_IN_TEST_DIR;
import static fs.TestFileNames.INNER_FILE_IN_TEST_DIR2;
import static fs.TestFileNames.LARGE;
import static fs.TestFileNames.MOVED_DIR;
import static fs.TestFileNames.MOVED_FILE;
import static fs.TestFileNames.MOVED_NOPE;
import static fs.TestFileNames.NOPE;
import static fs.TestFileNames.TEST_DIR;
import static fs.TestFileNames.TEST_DIR2;
import static fs.TestFileNames.TEST_DIR_IN_NOPE;
import static fs.TestFileNames.TEST_FILE;
import static fs.TestFileNames.TEST_FILE2;
import static fs.TestFileNames.TEST_FILE_IN_NOPE;
import static helpers.TestHelper.addReprToCons;
import static helpers.TestHelper.nop;
import static helpers.TestHelper.provideFail;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import data.ByteArray;
import data.Unit;
import helpers.TestHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class FSTest {
    private static final int FS_SIZE = 128;

    private FS testFs;

    private final Checks checks = new Checks();

    private void setUp() {
        testFs = FS.init(FS_SIZE).both(
                e -> {
                    throw new AssertionError("File system initialisation failed", e);
                },
                x -> x);
    }

    @DataProvider(name = "testCreate")
    private Iterator<Object[]> data4testCreate() {
        return Stream
                .of(
                        new Object[][]{
                                // success
                                {TestFileNames.TEST_FILE_IN_EXISTING_DIR, REGULAR, provideFail("Should create file"), nop()},
                                {TestFileNames.TEST_DIR_IN_EXISTING_DIR, DIRECTORY, provideFail("Should create directory"), nop()},
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
                                        TEST_FILE_IN_NOPE,
                                        REGULAR,
                                        PATH_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't create file located at non existing path")},
                                {
                                        TEST_DIR_IN_NOPE,
                                        DIRECTORY,
                                        PATH_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't create directory located at non existing path")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(EXISTING_FILE, REGULAR);
                            testFs.create(EXISTING_DIR, DIRECTORY);
                        })
                .iterator();
    }

    @Test(dataProvider = "testCreate")
    public void testCreate(String path, FileType type, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.create(path, type).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testInfo")
    private Iterator<Object[]> data4testInfo() {
        return Stream
                .of(
                        new Object[][]{
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
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(TEST_FILE2, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.write(TEST_FILE, new byte[]{1, 2});
                        })
                .iterator();
    }

    @Test(dataProvider = "testInfo")
    public void testInfo(String path, Consumer<FSError> leftChecker, Consumer<FileInfo> rightChecker) throws Exception {
        testFs.info(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testRead")
    private Iterator<Object[]> data4testRead() {
        return Stream
                .of(
                        new Object[][]{
                                {
                                        TEST_FILE,
                                        provideFail("Should read existing file"),
                                        TestHelper.<ByteArray>addReprToCons(
                                                actual -> assertEquals(actual, new ByteArray(new byte[]{1, 2})),
                                                "actual -> assertEquals(actual, new ByteArray(new byte[]{1, 2}))")},
                                {NOPE, PATH_NOT_FOUND_CHECKER, provideFail("Shouldn't read from non existent file")},
                                {TEST_DIR, FILE_IS_DIRECTORY_CHECKER, provideFail("Shouldn't read from directory")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.write(TEST_FILE, new byte[]{1, 2});
                        })
                .iterator();
    }

    @Test(dataProvider = "testRead")
    public void testRead(String path, Consumer<FSError> leftChecker, Consumer<ByteArray> rightChecker) throws Exception {
        testFs.read(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testLs")
    private Iterator<Object[]> data4testLs() {
        return Stream
                .of(
                        new Object[][]{
                                // existing directory
                                {
                                        TEST_DIR,
                                        provideFail("Should list directory content"),
                                        addReprToCons(
                                                actual -> assertEquals(
                                                        actual,
                                                        asList(
                                                                new FileInfo(INNER_FILE_IN_TEST_DIR, REGULAR, 0),
                                                                new FileInfo(INNER_DIR_IN_TEST_DIR, DIRECTORY, 0))),
                                                "actual -> assertEquals(" +
                                                        "actual," +
                                                        "asList(" +
                                                        "new FileInfo(INNER_FILE_IN_TEST_DIR, REGULAR, 0)," +
                                                        "new FileInfo(INNER_DIR_IN_TEST_DIR, DIRECTORY, 0)))")},
                                // non existing directory
                                {
                                        NOPE,
                                        FILE_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't list non existing directory")},
                                // regular file
                                {
                                        INNER_FILE_IN_TEST_DIR,
                                        FILE_IS_REGULAR_CHECKER,
                                        provideFail("Shouldn't list regular file")},

                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.create(INNER_FILE_IN_TEST_DIR, REGULAR);
                            testFs.create(INNER_DIR_IN_TEST_DIR, DIRECTORY);
                        })
                .iterator();
    }

    @Test(dataProvider = "testLs")
    public void testLs(String path, Consumer<FSError> leftChecker, Consumer<List<FileInfo>> rightChecker) throws Exception {
        testFs.ls(path).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testCopy")
    private Iterator<Object[]> data4testCopy() {
        return Stream
                .of(
                        new Object[][]{
                                {
                                        INNER_FILE_IN_TEST_DIR, TEST_FILE,
                                        provideFail("Should copy existent file"),
                                        checks.provideFileChecker(TEST_FILE, new FileInfo(TEST_FILE, REGULAR, 2), new byte[]{1, 2})},
                                {
                                        INNER_FILE_IN_TEST_DIR, INNER_FILE_IN_TEST_DIR,
                                        provideFail("Should copy existent file"),
                                        checks.provideFileChecker(
                                                INNER_FILE_IN_TEST_DIR,
                                                new FileInfo(INNER_FILE_IN_TEST_DIR, REGULAR, 2),
                                                new byte[]{1, 2})},
                                {
                                        TEST_DIR, TEST_DIR2,
                                        provideFail("Should copy existent directory"),
                                        checks.provideDirChecker(
                                                TEST_DIR2,
                                                new FileInfo(TEST_DIR2, DIRECTORY, 0),
                                                new FileInfo(INNER_FILE_IN_TEST_DIR2, REGULAR, 2))},
                                {
                                        TEST_DIR, TEST_DIR,
                                        provideFail("Should copy existent directory"),
                                        checks.provideDirChecker(
                                                TEST_DIR,
                                                new FileInfo(TEST_DIR, DIRECTORY, 0),
                                                new FileInfo(INNER_FILE_IN_TEST_DIR, REGULAR, 2))},
                                {
                                        NOPE, TEST_FILE,
                                        FILE_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't copy non existent file")},
                                {
                                        INNER_FILE_IN_TEST_DIR, LARGE,
                                        ALREADY_EXISTS_CHECKER,
                                        provideFail("Shouldn't copy into existent file")},
                                {
                                        LARGE, TEST_FILE,
                                        NO_FREE_SPACE_CHECKER,
                                        provideFail("Shouldn't copy file larger than half of total file system size")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.create(INNER_FILE_IN_TEST_DIR, REGULAR);
                            testFs.write(INNER_FILE_IN_TEST_DIR, new byte[]{1, 2});
                            testFs.create(LARGE, REGULAR);
                            testFs.write(LARGE, new byte[FS_SIZE / 2 + 1]);
                        })
                .iterator();
    }

    @Test(dataProvider = "testCopy")
    public void testCopy(String sourcePath,
                         String destinationPath,
                         Consumer<FSError> leftChecker,
                         Consumer<Unit> rightChecker) throws Exception {
        testFs.copy(sourcePath, destinationPath).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testWrite")
    private Iterator<Object[]> data4testWrite() {
        return Stream
                .of(
                        new Object[][]{
                                // write small content
                                {
                                        TEST_FILE, new ByteArray(new byte[]{3, 4, 5}),
                                        provideFail("Should write to existing file"),
                                        TestHelper.<Unit>addReprToCons(
                                                __ -> testFs.read(TEST_FILE).onRight(actual -> assertEquals(actual.get(),
                                                        new byte[]{3, 4, 5})),
                                                "__ -> testFs.read(\"/test_file\").onRight(actual -> assertEquals(actual.get()," +
                                                        "new byte[]{3, 4, 5}))")},
                                // write large content
                                {
                                        TEST_FILE, new ByteArray(new byte[(FS_SIZE * 3) / 4]),
                                        provideFail("Should write to existing file"),
                                        TestHelper.<Unit>addReprToCons(
                                                __ -> testFs.read(TEST_FILE).onRight(actual -> assertEquals(actual.get(),
                                                        new byte[(FS_SIZE * 3) / 4])),
                                                "__ -> testFs.read(\"/test_file\").onRight(actual -> assertEquals(actual.get()," +
                                                        "new byte[(FS_SIZE * 3) / 4]))")},
                                {
                                        NOPE, new ByteArray(new byte[]{1, 2, 3}),
                                        PATH_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't write to non existent file")},
                                {
                                        TEST_DIR, new ByteArray(new byte[]{1, 2, 3}),
                                        FILE_IS_DIRECTORY_CHECKER,
                                        provideFail("Shouldn't write to directory")},
                                {
                                        TEST_FILE, new ByteArray(new byte[FS_SIZE + 1]),
                                        NO_FREE_SPACE_CHECKER,
                                        provideFail("Shouldn't write content larger than file system")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.write(TEST_FILE, new byte[(FS_SIZE * 3) / 4]);
                        })
                .iterator();
    }

    @Test(dataProvider = "testWrite")
    public void testWrite(String path, ByteArray content, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.write(path, content.get()).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testAppend")
    private Iterator<Object[]> data4testAppend() {
        return Stream
                .of(
                        new Object[][]{
                                {
                                        TEST_FILE, new ByteArray(new byte[]{3}),
                                        provideFail("Should append to existing file"),
                                        TestHelper.<Unit>addReprToCons(
                                                __1 -> testFs.read(TEST_FILE).onRight(actual -> assertEquals(actual.get(),
                                                        new byte[]{1, 2, 3})),
                                                "__ -> testFs.read(\"/test_file\").onRight(actual -> assertEquals(actual.get()," +
                                                        "new byte[]{1, 2, 3}))")},
                                {
                                        NOPE, new ByteArray(new byte[]{1, 2, 3}),
                                        PATH_NOT_FOUND_CHECKER,
                                        provideFail("Shouldn't append to non existent file")},
                                {
                                        TEST_DIR, new ByteArray(new byte[]{1, 2, 3}),
                                        FILE_IS_DIRECTORY_CHECKER,
                                        provideFail("Shouldn't append to directory")},
                                {
                                        TEST_FILE, new ByteArray(new byte[FS_SIZE + 1]),
                                        NO_FREE_SPACE_CHECKER,
                                        provideFail("Shouldn't append content larger than file system")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.write(TEST_FILE, new byte[]{1, 2});
                        })
                .iterator();
    }

    @Test(dataProvider = "testAppend")
    public void testAppend(String path, ByteArray content, Consumer<FSError> leftChecker, Consumer<Unit> rightChecker)
            throws Exception {
        testFs.append(path, content.get()).onBoth(leftChecker, rightChecker);
    }

    @DataProvider(name = "testDelete")
    private Iterator<Object[]> data4testDelete() {
        return Stream
                .of(
                        new Object[][]{
                                {TEST_FILE, provideFail("Should delete file"), checks.provideDeleteChecker(TEST_FILE)},
                                {TEST_DIR, provideFail("Should delete directory"), checks.provideDeleteChecker(TEST_DIR)},
                                {NOPE, FILE_NOT_FOUND_CHECKER, provideFail("Non existent file shouldn't be deleted")},
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.create(INNER_FILE_IN_TEST_DIR, REGULAR);
                        })
                .iterator();
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
        final long used = testFs.used();
        assertTrue(used >= fileMinimalSize);
        assertTrue(used <= FS_SIZE);
    }

    @Test
    public void testFree() throws Exception {
        setUp();
        final String testFile = TEST_FILE;
        testFs.create(testFile, REGULAR);
        final int fileMinimalSize = FS_SIZE / 2;
        testFs.write(testFile, new byte[fileMinimalSize]);
        assertEquals(testFs.used() + testFs.free(), testFs.size());
        assertTrue(testFs.free() <= fileMinimalSize);
    }

    @DataProvider(name = "testMove")
    private Iterator<Object[]> data4testMove() {
        return Stream
                .of(
                        new Object[][]{
                                {
                                        INNER_FILE_IN_TEST_DIR, MOVED_FILE,
                                        provideFail("Should move existent file"),
                                        checks.provideFileChecker(MOVED_FILE, new FileInfo(TEST_FILE, REGULAR, 2), new byte[]{1, 2})},
                                {
                                        TEST_DIR2, MOVED_DIR,
                                        provideFail("Should move existent directory"),
                                        checks.provideDirChecker(
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
                        })
                .peek(
                        // setup for run
                        __ -> {
                            setUp();
                            testFs.create(TEST_FILE, REGULAR);
                            testFs.create(EXISTING_FILE, REGULAR);
                            testFs.create(TEST_DIR, DIRECTORY);
                            testFs.create(INNER_FILE_IN_TEST_DIR, REGULAR);
                            testFs.write(INNER_FILE_IN_TEST_DIR, new byte[]{1, 2});
                            testFs.create(TEST_DIR2, DIRECTORY);
                            testFs.create(INNER_FILE_IN_TEST_DIR2, REGULAR);
                            testFs.write(INNER_FILE_IN_TEST_DIR2, new byte[]{1, 2});
                        })
                .iterator();
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

    /**
     * different checks on file system
     */
    private final class Checks {
        private Checks() {
        }

        private Consumer<Unit> provideDeleteChecker(String path) {
            return addReprToCons(
                    __ -> testFs.info(path).onRight(___ -> fail("File " + path + "should be deleted")),
                    "__ -> testFs.info(" + path + ").onRight(___ -> fail(\"File \" + " + path + " + \"should be deleted\"))");
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

        private Consumer<Unit> provideDirChecker(String path, FileInfo expectedInfo, FileInfo... expectedContent) {
            return addReprToCons(
                    __ -> checkDir(path, expectedInfo, asList(expectedContent)),
                    "__-> checkDir(" + path + ", " + expectedInfo + ", " + Arrays.toString(expectedContent) + ")");
        }

        private void checkDir(String path, FileInfo expectedInfo, List<FileInfo> expectedContent) {
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
    }
}