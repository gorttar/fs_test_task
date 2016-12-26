package data;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class ByteArrayTest {
    @Test
    public void testGet() throws Exception {
        final byte[] src = {1};
        final ByteArray testObject = new ByteArray(src);
        final byte[] dst = testObject.get();
        assertEquals(dst, new byte[]{1}, "Failure on unwrapping array");

        src[0] = 0;
        assertEquals(testObject.get(), new byte[]{1}, "testObject is mutated by source changes");

        dst[0] = 2;
        assertEquals(testObject.get(), new byte[]{1}, "testObject is mutated by destination changes");
    }

}