/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package check;

import java.util.function.Supplier;

/**
 * helper to simplify defensive checks of implementation code such as representation invariants
 * *    and other logical constraints
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-28)
 */
public final class CheckHelper {
    private CheckHelper() {
    }

    /**
     * executes payload with post execution side effect from checker's execution
     * * typical use case: assert on invariants and post conditions
     *
     * @param payload supplier to decorate
     * @param checker post execution side effect
     * @param <T>     payload supplied type
     * @return value supplied by payload
     */
    public static <T> T checkedGet(Supplier<? extends T> payload, Runnable checker) {
        final T t = payload.get();
        checker.run();
        return t;
    }
}