/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data.either.impl;

import data.either.Either;

import javax.annotation.Nonnull;

/**
 * {@link Either} config intended to separate interface from actual implementation
 * * implementor should modify it in order to switch between implementations
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-29)
 */
public final class EitherConfig {
    private EitherConfig() {
    }

    @Nonnull
    public static <L, R> Either<L, R> right(R r) {
        return new SimpleEitherImpl.Right<>(r);
    }

    @Nonnull
    public static <L, R> Either<L, R> left(L l) {
        return new SimpleEitherImpl.Left<>(l);
    }
}