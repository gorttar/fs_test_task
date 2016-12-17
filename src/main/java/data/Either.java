/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * type for storing either left or right values
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-17)
 */
public interface Either<L, R> {
    /**
     * tries to get stored right value from this
     *
     * @return stored right value
     * @throws UnwrapException in case there is no right value in this
     */
    R getRight() throws UnwrapException;

    /**
     * gets stored stored right value from this or value supplied by supplier
     * in case there is no right value in this
     *
     * @param rSupplier supplier for default value
     * @return either stored right value or default value from supplier
     */
    R elseGetRight(@Nonnull Supplier<? extends R> rSupplier);

    /**
     * tries to get stored left value from this
     *
     * @return stored left value
     * @throws UnwrapException in case there is no left value in this
     */
    L getLeft() throws UnwrapException;

    /**
     * gets stored stored left value from this or value supplied by supplier
     * in case there is no left value in this
     *
     * @param lSupplier supplier for default value
     * @return either stored left value or default value from supplier
     */
    L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier);

    /**
     * maps either left or right mapper to this without lifting result back to either
     *
     * @param lMapper mapper used for left values
     * @param rMapper mapper user for right values
     * @param <V>     result type of both matters
     * @return result of applying either left or right mapper to stored value
     */
    <V> V both(@Nonnull Function<? super L, ? extends V> lMapper,
               @Nonnull Function<? super R, ? extends V> rMapper);

    /**
     * @return true if this stores left value
     */
    boolean isLeft();

    /**
     * @return true if this stores right value
     */
    boolean isRight();

    /**
     * maps either left or right mapper to this flattening result of such mapping
     *
     * @param lMapper mapper used for left value
     * @param rMapper mapper user for right value
     * @param <L2>    new left type parameter
     * @param <R2>    new right type parameter
     * @return result of applying either left or right mapper to stored value
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    default <L2, R2> Either<L2, R2> flatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper,
                                            @Nonnull Function<? super R, ? extends Either<L, R2>> rMapper) {
        return (Either<L2, R2>) both(lMapper, rMapper);
    }

    /**
     * consumes either left or right value by accepting appropriate consumer
     *
     * @param lConsumer consumer for left value
     * @param rConsumer consumer for right value
     */
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

    /**
     * maps on left value only
     * right value remains intact
     *
     * @param lMapper mapper for left value
     * @param <L2>    new left type parameter
     * @return result of mapping
     */
    @Nonnull
    default <L2> Either<L2, R> lMap(@Nonnull Function<? super L, ? extends L2> lMapper) {
        return map(lMapper, x -> x);
    }

    /**
     * maps on left value only flattening result of such mapping
     * right value remains intact
     *
     * @param lMapper mapper for left value
     * @param <L2>    new left type parameter
     * @return result of mapping
     */
    @Nonnull
    default <L2> Either<L2, R> lFlatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper) {
        return flatMap(lMapper, __ -> this);
    }

    /**
     * maps on right value only
     * left value remains intact
     *
     * @param rMapper mapper for right value
     * @param <R2>    new right type parameter
     * @return result of mapping
     */
    @Nonnull
    default <R2> Either<L, R2> rMap(@Nonnull Function<? super R, ? extends R2> rMapper) {
        return map(x -> x, rMapper);
    }

    /**
     * maps on right value only flattening result of such mapping
     * left value remains intact
     *
     * @param rMapper mapper for right value
     * @param <R2>    new right type parameter
     * @return result of mapping
     */
    @Nonnull
    default <R2> Either<L, R2> rFlatMap(@Nonnull Function<? super R, ? extends Either<L, R2>> rMapper) {
        return flatMap(__ -> this, rMapper);
    }

    /**
     * maps either left or right mapper to this
     *
     * @param lMapper mapper used for left value
     * @param rMapper mapper user for right value
     * @param <L2>    new left type parameter
     * @param <R2>    new right type parameter
     * @return result of mapping
     */
    @Nonnull
    default <L2, R2> Either<L2, R2> map(@Nonnull Function<? super L, ? extends L2> lMapper,
                                        @Nonnull Function<? super R, ? extends R2> rMapper) {
        return flatMap(l -> left(lMapper.apply(l)), r -> right(rMapper.apply(r)));
    }

    /**
     * gets stored stored left value from this or value provided as parameter
     * in case there is no left value in this
     *
     * @param l default value
     * @return either stored left value or default value
     */
    default L getLeft(L l) {
        return elseGetLeft(() -> l);
    }

    /**
     * gets stored stored right value from this or value provided as parameter
     * in case there is no right value in this
     *
     * @param r default value
     * @return either stored left value or default value
     */
    default R getRight(R r) {
        return elseGetRight(() -> r);
    }

    /**
     * consumes left value only
     *
     * @param lConsumer to accept left value
     */
    default void onLeft(@Nonnull Consumer<? super L> lConsumer) {
        onBoth(lConsumer, Either::nop);
    }

    /**
     * consumes right value only
     *
     * @param rConsumer to accept right value
     */
    default void onRight(@Nonnull Consumer<? super R> rConsumer) {
        onBoth(Either::nop, rConsumer);
    }

    /**
     * creates instance of either from right value
     *
     * @param r   right value
     * @param <L> type of left value
     * @param <R> type of right value
     * @return created instance
     */
    @Nonnull
    static <L, R> Either<L, R> right(R r) {
        return new Right<>(r);
    }

    /**
     * creates instance of either from left value
     *
     * @param l   left value
     * @param <L> type of left value
     * @param <R> type of right value
     * @return created instance
     */
    @Nonnull
    static <L, R> Either<L, R> left(L l) {
        return new Left<>(l);
    }

    final class UnwrapException extends Exception {
        UnwrapException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("unused")
    static <T> void nop(T __) {
    }
}