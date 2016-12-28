/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package check;

import java.util.function.Supplier;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-28)
 */
public final class CheckHelper {
    private CheckHelper() {
    }

    public static <T> T checkedGet(Supplier<? extends T> payload, Runnable checker) {
        final T t = payload.get();
        checker.run();
        return t;
    }
}