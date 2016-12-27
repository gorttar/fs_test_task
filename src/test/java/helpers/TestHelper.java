/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package helpers;

import static java.util.Objects.requireNonNull;
import static org.testng.Assert.fail;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-25)
 */
public final class TestHelper {
    /**
     * the only instance of no operations consumer
     */
    private static final Consumer<?> NOP = addReprToCons(
            __ -> {
            }, "__ -> {}");

    private TestHelper() {
    }

    /**
     * @param <T> consumer's type parameter
     * @return no operations consumer casted to appropriate type
     */
    public static <T> Consumer<T> nop() {
        @SuppressWarnings("unchecked")
        final Consumer<T> result = (Consumer<T>) NOP;
        return result;
    }

    /**
     * {@link Consumer} decorator which adds provided repr to source consumer
     *
     * @param source to decorate
     * @param repr   what decorated consumer's toString should return
     * @param <T>    consumer's type argument
     * @return decorated consumer
     */
    public static <T> Consumer<T> addReprToCons(@Nonnull Consumer<? super T> source,
                                                @Nonnull String repr) {
        requireNonNull(source);
        requireNonNull(repr);
        return new Consumer<T>() {
            @Override
            public void accept(T t) {
                source.accept(t);
            }

            @Override
            public String toString() {
                return repr;
            }
        };
    }

    /**
     * provides consumer which throws {@link AssertionError} with given message on invocation
     *
     * @param message error description
     * @return consumer described above
     */
    public static <T> Consumer<T> provideFail(String message) {
        return addReprToCons(arg -> fail(message + "\targ:" + arg), "arg -> fail(" + message + " + \"\\targ:\" + arg)");
    }
}