/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-17)
 */
final class Left<L, R> implements Either<L, R> {
    private final L l;

    Left(L l) {
        this.l = l;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <L2, R2> Either<L2, R2> flatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper,
                                           @Nonnull Function<? super R, ? extends Either<L, R2>> rMapper) {
        return (Either<L2, R2>) lMapper.apply(l);
    }

    @Override
    public R getRight() throws UnwrapException {
        throw new UnwrapException("Left instances doesn't contain right value");
    }

    @Override
    public L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier) {
        return l;
    }

    @Override
    public L getLeft() throws UnwrapException {
        return l;
    }

    @Override
    public R elseGetRight(@Nonnull Supplier<? extends R> rSupplier) {
        return rSupplier.get();
    }

    @Override
    public <V> V both(@Nonnull Function<? super L, ? extends V> lMapper, @Nonnull Function<? super R, ? extends V> rMapper) {
        return lMapper.apply(l);
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Left)) return false;
        Left<?, ?> other = (Left<?, ?>) o;
        return Objects.equals(l, other.l);
    }

    @Override
    public int hashCode() {
        return Objects.hash(l);
    }

    @Override
    public String toString() {
        return "Left(" + l + ')';
    }
}

final class Right<L, R> implements Either<L, R> {
    private final R r;

    Right(R r) {
        this.r = r;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <L2, R2> Either<L2, R2> flatMap(@Nonnull Function<? super L, ? extends Either<L2, R>> lMapper, @Nonnull Function<? super R, ? extends Either<L, R2>> rMapper) {
        return (Either<L2, R2>) rMapper.apply(r);
    }

    @Override
    public R getRight() throws UnwrapException {
        return r;
    }

    @Override
    public L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier) {
        return lSupplier.get();
    }

    @Override
    public L getLeft() throws UnwrapException {
        throw new UnwrapException("Right instances doesn't contain left value");
    }

    @Override
    public R elseGetRight(@Nonnull Supplier<? extends R> rSupplier) {
        return r;
    }

    @Override
    public <V> V both(@Nonnull Function<? super L, ? extends V> lMapper, @Nonnull Function<? super R, ? extends V> rMapper) {
        return rMapper.apply(r);
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Right)) return false;
        Right<?, ?> other = (Right<?, ?>) o;
        return Objects.equals(r, other.r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(r);
    }

    @Override
    public String toString() {
        return "Right(" + r + ')';
    }
}