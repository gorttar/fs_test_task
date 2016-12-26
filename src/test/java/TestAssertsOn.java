/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */

import org.testng.annotations.Test;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-24)
 */
public class TestAssertsOn {
    @Test(expectedExceptions = AssertionError.class)
    public void testAssertsOn() {
        assert false;
    }
}