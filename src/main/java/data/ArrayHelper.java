/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-25)
 */
public final class ArrayHelper {
    private ArrayHelper() {
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... ts) {
        return Arrays.stream(ts).collect(Collectors.toSet());
    }
}