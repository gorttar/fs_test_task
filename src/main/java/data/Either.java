/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-17)
 */
public interface Either<L, R> {
    @Nonnull
    <L2, R2> Either<L2, R2> flatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper,
                                    @Nonnull Function<? super R, ? extends Either<L, R2>> rMapper);

    R getRight() throws UnwrapException;

    R elseGetRight(@Nonnull Supplier<? extends R> rSupplier);

    L getLeft() throws UnwrapException;

    L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier);

    <V> V both(@Nonnull Function<? super L, ? extends V> lMapper,
               @Nonnull Function<? super R, ? extends V> rMapper);

    boolean isLeft();

    boolean isRight();

    default void onBoth(@Nonnull Consumer<? super L> lConsumer,
                        @Nonnull Consumer<? super R> rConsumer) {
        map(
                l -> {
                    lConsumer.accept(l);
                    return null;
                },
                r -> {
                    rConsumer.accept(r);
                    return null;
                });
    }

    @Nonnull
    default <L2> Either<L2, R> lMap(@Nonnull Function<? super L, ? extends L2> lMapper) {
        return map(lMapper, x -> x);
    }

    @Nonnull
    default <L2> Either<L2, R> lFlatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper) {
        return flatMap(lMapper, __ -> this);
    }

    @Nonnull
    default <R2> Either<L, R2> rMap(@Nonnull Function<? super R, ? extends R2> rMapper) {
        return map(x -> x, rMapper);
    }

    @Nonnull
    default <R2> Either<L, R2> rFlatMap(@Nonnull Function<? super R, ? extends Either<L, R2>> rMapper) {
        return flatMap(__ -> this, rMapper);
    }

    @Nonnull
    default <L2, R2> Either<L2, R2> map(@Nonnull Function<? super L, ? extends L2> lMapper,
                                        @Nonnull Function<? super R, ? extends R2> rMapper) {
        return flatMap(l -> left(lMapper.apply(l)), r -> right(rMapper.apply(r)));
    }

    default L getLeft(L l) {
        return elseGetLeft(() -> l);
    }

    default R getRight(R r) {
        return elseGetRight(() -> r);
    }

    default void onLeft(@Nonnull Consumer<? super L> lConsumer) {
        onBoth(lConsumer, Either::nop);
    }

    default void onRight(@Nonnull Consumer<? super R> rConsumer) {
        onBoth(Either::nop, rConsumer);
    }

    @Nonnull
    static <L, R> Either<L, R> right(R r) {
        return new Right<>(r);
    }

    @Nonnull
    static <L, R> Either<L, R> left(L l) {
        return new Left<>(l);
    }

    final class UnwrapException extends Exception {
        UnwrapException(String msg) {
            super(msg);
        }
    }

    static <T> void nop(T __) {
    }
}