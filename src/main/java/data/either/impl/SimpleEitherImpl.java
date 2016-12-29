/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data.either.impl;

import static java.util.Objects.requireNonNull;

import data.either.Either;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * simple {@link Either} implementation
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-29)
 */
final class SimpleEitherImpl {
    private SimpleEitherImpl() {
    }

    /**
     * left value implementation
     */
    static final class Left<L, R> implements Either<L, R> {
        private final L l;

        Left(L l) {
            this.l = l;
        }

        @Override
        public R getRight() throws UnwrapException {
            throw new UnwrapException("Left instances doesn't contain right value");
        }

        @Override
        public L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier) {
            requireNonNull(lSupplier);
            return l;
        }

        @Override
        public L getLeft() throws UnwrapException {
            return l;
        }

        @Override
        public R elseGetRight(@Nonnull Supplier<? extends R> rSupplier) {
            return requireNonNull(rSupplier).get();
        }

        @Override
        public <V> V both(@Nonnull Function<? super L, ? extends V> lMapper, @Nonnull Function<? super R, ? extends V> rMapper) {
            requireNonNull(rMapper);
            return requireNonNull(lMapper).apply(l);
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
            if (this == o) {
                return true;
            }
            if (!(o instanceof Left)) {
                return false;
            }
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

    /**
     * right value implementation
     */
    static final class Right<L, R> implements Either<L, R> {
        private final R r;

        Right(R r) {
            this.r = r;
        }

        @Override
        public R getRight() throws UnwrapException {
            return r;
        }

        @Override
        public L elseGetLeft(@Nonnull Supplier<? extends L> lSupplier) {
            return requireNonNull(lSupplier).get();
        }

        @Override
        public L getLeft() throws UnwrapException {
            throw new UnwrapException("Right instances doesn't contain left value");
        }

        @Override
        public R elseGetRight(@Nonnull Supplier<? extends R> rSupplier) {
            requireNonNull(rSupplier);
            return r;
        }

        @Override
        public <V> V both(@Nonnull Function<? super L, ? extends V> lMapper, @Nonnull Function<? super R, ? extends V> rMapper) {
            requireNonNull(lMapper);
            return requireNonNull(rMapper).apply(r);
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
            if (this == o) {
                return true;
            }
            if (!(o instanceof Right)) {
                return false;
            }
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
}

