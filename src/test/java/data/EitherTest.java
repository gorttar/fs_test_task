package data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-17)
 */
public class EitherTest {
    private final static Function<Integer, Integer> F = new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return 2 * x;
        }

        @Override
        public String toString() {
            return "x -> 2 * x";
        }
    };
    private final static Function<Integer, Integer> G = new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return x + 3;
        }

        @Override
        public String toString() {
            return "x -> x + 3";
        }
    };

    private static Supplier<String> supplierFor(String supplied) {
        return new Supplier<String>() {
            @Override
            public String get() {
                return supplied;
            }

            @Override
            public String toString() {
                return String.format("() -> \"%s\"", supplied);
            }
        };
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testAssert() {
        assert false;
    }

    @DataProvider(name = "testElseGetLeft")
    Object[][] data4testElseGetLeft() {
        return new Object[][]{
                {Either.right("right"), supplierFor("supplied left"), "supplied left"},
                {Either.left("left"), supplierFor("supplied left"), "left"},
        };
    }

    @Test(dataProvider = "testElseGetLeft")
    public void testElseGetLeft(Either<String, String> either,
                                Supplier<String> lSupplier,
                                String expected) throws Exception {
        assertEquals(either.elseGetLeft(lSupplier), expected);
    }

    @DataProvider(name = "testElseGetRight")
    Object[][] data4testElseGetRight() {
        return new Object[][]{
                {Either.right("right"), supplierFor("supplied right"), "right"},
                {Either.left("left"), supplierFor("supplied right"), "supplied right"},
        };
    }

    @Test(dataProvider = "testElseGetRight")
    public void testElseGetRight(Either<String, String> either,
                                 Supplier<String> rSupplier,
                                 String expected) throws Exception {
        assertEquals(either.elseGetRight(rSupplier), expected);
    }

    @DataProvider(name = "testLMap")
    Object[][] data4testLMap() {
        return new Object[][]{
                {F, G, Either.right(1), Either.right(1)},
                {F, G, Either.right(2), Either.right(2)},
                {F, G, Either.left(1), Either.left(5)},
                {F, G, Either.left(2), Either.left(7)},
        };
    }

    @Test(dataProvider = "testLMap")
    public void testLMap(Function<Integer, Integer> f,
                         Function<Integer, Integer> g,
                         Either<Integer, Integer> arg,
                         Either<Integer, Integer> expected) throws Exception {
        final Either<Integer, Integer> actual = arg.lMap(f).lMap(g);
        assertEquals(actual, expected);
        assertEquals(arg.lMap(x -> x), arg); // first functor law
        assertEquals(actual, arg.lMap(f.andThen(g))); // second functor law
    }

    @DataProvider(name = "testLFlatMap")
    Object[][] data4testLFlatMap() {
        return new Object[][]{
                {1, Either.right(5)},
                {2, Either.right(7)},
        };
    }

    @Test(dataProvider = "testLFlatMap")
    public void testLFlatMap(int arg, Either<Integer, Integer> expected) throws Exception {
        final Function<Integer, Either<Integer, Integer>> f = x -> Either.left(2 * x);
        final Function<Integer, Either<Integer, Integer>> g = x -> Either.right(x + 3);

        assertEquals(Either.<Integer, Integer>left(arg).lFlatMap(f).lFlatMap(g), expected);

        // left is neutral element of lFlatMap
        assertEquals(Either.<Integer, Integer>left(arg).lFlatMap(f), f.apply(arg));
        assertEquals(Either.<Integer, Integer>left(arg).lFlatMap(g), g.apply(arg));
        // lFlatMap is associative
        assertEquals(
                Either.<Integer, Integer>left(arg).lFlatMap(f).lFlatMap(g),
                Either.<Integer, Integer>left(arg).lFlatMap(x -> f.apply(x).lFlatMap(g)));
        assertEquals(
                Either.<Integer, Integer>right(arg).lFlatMap(f).lFlatMap(g),
                Either.<Integer, Integer>right(arg).lFlatMap(x -> f.apply(x).lFlatMap(g)));
        assertEquals(
                Either.<Integer, Integer>left(arg).lFlatMap(g).lFlatMap(f),
                Either.<Integer, Integer>left(arg).lFlatMap(x -> g.apply(x).lFlatMap(f)));
        assertEquals(
                Either.<Integer, Integer>right(arg).lFlatMap(g).lFlatMap(f),
                Either.<Integer, Integer>right(arg).lFlatMap(x -> g.apply(x).lFlatMap(f)));
    }

    @DataProvider(name = "testRMap")
    Object[][] data4testRMap() {
        return new Object[][]{
                {F, G, Either.right(1), Either.right(5)},
                {F, G, Either.right(2), Either.right(7)},
                {F, G, Either.left(1), Either.left(1)},
                {F, G, Either.left(2), Either.left(2)},
        };
    }

    @Test(dataProvider = "testRMap")
    public void testRMap(Function<Integer, Integer> f,
                         Function<Integer, Integer> g,
                         Either<Integer, Integer> arg,
                         Either<Integer, Integer> expected) throws Exception {
        final Either<Integer, Integer> actual = arg.rMap(f).rMap(g);
        assertEquals(actual, expected);
        assertEquals(arg.rMap(x -> x), arg); // first functor law
        assertEquals(actual, arg.rMap(f.andThen(g))); // second functor law
    }

    @DataProvider(name = "testRFlatMap")
    Object[][] data4testRFlatMap() {
        return new Object[][]{
                {1, Either.left(5)},
                {2, Either.left(7)},
        };
    }

    @Test(dataProvider = "testRFlatMap")
    public void testRFlatMap(int arg, Either<Integer, Integer> expected) throws Exception {
        final Function<Integer, Either<Integer, Integer>> f = x -> Either.right(2 * x);
        final Function<Integer, Either<Integer, Integer>> g = x -> Either.left(x + 3);

        assertEquals(Either.<Integer, Integer>right(arg).rFlatMap(f).rFlatMap(g), expected);

        // right is neutral element of rFlatMap
        assertEquals(Either.<Integer, Integer>right(arg).rFlatMap(f), f.apply(arg));
        assertEquals(Either.<Integer, Integer>right(arg).rFlatMap(g), g.apply(arg));
        // rFlatMap is associative
        assertEquals(
                Either.<Integer, Integer>left(arg).rFlatMap(f).rFlatMap(g),
                Either.<Integer, Integer>left(arg).rFlatMap(x -> f.apply(x).rFlatMap(g)));
        assertEquals(
                Either.<Integer, Integer>right(arg).rFlatMap(f).rFlatMap(g),
                Either.<Integer, Integer>right(arg).rFlatMap(x -> f.apply(x).rFlatMap(g)));
        assertEquals(
                Either.<Integer, Integer>left(arg).rFlatMap(g).rFlatMap(f),
                Either.<Integer, Integer>left(arg).rFlatMap(x -> g.apply(x).rFlatMap(f)));
        assertEquals(
                Either.<Integer, Integer>right(arg).rFlatMap(g).rFlatMap(f),
                Either.<Integer, Integer>right(arg).rFlatMap(x -> g.apply(x).rFlatMap(f)));
    }

    @DataProvider(name = "testMap")
    Object[][] data4testMap() {
        return new Object[][]{
                {F, G, Either.right(1), Either.right(8)},
                {F, G, Either.right(2), Either.right(10)},
                {F, G, Either.left(1), Either.left(5)},
                {F, G, Either.left(2), Either.left(7)},
                {G, F, Either.right(1), Either.right(5)},
                {G, F, Either.right(2), Either.right(7)},
                {G, F, Either.left(1), Either.left(8)},
                {G, F, Either.left(2), Either.left(10)},
        };
    }

    @Test(dataProvider = "testMap")
    public void testMap(Function<Integer, Integer> f,
                        Function<Integer, Integer> g,
                        Either<Integer, Integer> arg,
                        Either<Integer, Integer> expected) throws Exception {
        final Either<Integer, Integer> actual = arg.map(f, g).map(g, f);
        assertEquals(actual, expected);
        assertEquals(arg.map(x -> x, x -> x), arg); // first functor law
        assertEquals(actual, arg.map(f.andThen(g), g.andThen(f))); // second functor law
    }

    @DataProvider(name = "testFlatMap")
    Object[][] data4testFlatMap() {
        return new Object[][]{
                {Either.left(1), Either.right(5)},
                {Either.left(2), Either.right(7)},
                {Either.right(1), Either.left(8)},
                {Either.right(2), Either.left(10)},
        };
    }

    @Test(dataProvider = "testFlatMap")
    public void testFlatMap(Either<Integer, Integer> arg,
                            Either<Integer, Integer> expected) throws Exception {
        final Function<Integer, Either<Integer, Integer>> f = x -> Either.left(2 * x);
        final Function<Integer, Either<Integer, Integer>> g = x -> Either.right(x + 3);

        final Either<Integer, Integer> actual = arg.flatMap(f, g).flatMap(g, f);
        assertEquals(actual, expected);

        // flatMap is associative on bot args
        assertEquals(actual, arg.flatMap(x -> f.apply(x).flatMap(g, f), x -> g.apply(x).flatMap(g, f)));
    }

    @Test
    public void testGetRight() throws Exception {
        assertEquals(Either.right(1).getRight(), (Integer) 1);
        try {
            Either.left(1).getRight();
            fail();
        } catch (Either.UnwrapException e) {
            //
        }
    }

    @DataProvider(name = "testGetLeft")
    Object[][] data4testGetLeft() {
        return new Object[][]{
                {Either.right("right"), "supplied left", "supplied left"},
                {Either.left("left"), "supplied left", "left"},
        };
    }

    @Test(dataProvider = "testGetLeft")
    public void testGetLeft(Either<String, String> either,
                            String l,
                            String expected) throws Exception {
        assertEquals(either.getLeft(l), expected);
    }

    @Test
    public void testGetLeft() throws Exception {
        assertEquals(Either.left(1).getLeft(), (Integer) 1);
        try {
            Either.right(1).getLeft();
            fail();
        } catch (Either.UnwrapException e) {
            //
        }
    }

    @DataProvider(name = "testGetRight")
    Object[][] data4testGetRight() {
        return new Object[][]{
                {Either.right("right"), "supplied right", "right"},
                {Either.left("left"), "supplied right", "supplied right"},
        };
    }

    @Test(dataProvider = "testGetRight")
    public void testGetRight(Either<String, String> either,
                             String r,
                             String expected) throws Exception {
        assertEquals(either.getRight(r), expected);
    }

    @DataProvider(name = "testBoth")
    Object[][] data4testBoth() {
        return new Object[][]{
                {Either.right(1), F, G, 4},
                {Either.left(1), F, G, 2},
        };
    }

    @Test(dataProvider = "testBoth")
    public void testBoth(Either<Integer, Integer> either,
                         Function<Integer, Integer> lMapper,
                         Function<Integer, Integer> rMapper,
                         Integer expected) throws Exception {
        assertEquals(either.both(lMapper, rMapper), expected);
    }

    @DataProvider(name = "testIsLeft")
    Object[][] data4testIsLeft() {
        return new Object[][]{
                {Either.left(1), true},
                {Either.right(1), false},
        };
    }

    @Test(dataProvider = "testIsLeft")
    public void testIsLeft(Either<Integer, Integer> either, boolean expected) throws Exception {
        assertEquals(either.isLeft(), expected);
    }

    @DataProvider(name = "testIsRight")
    Object[][] data4testIsRight() {
        return new Object[][]{
                {Either.left(1), false},
                {Either.right(1), true},
        };
    }

    @Test(dataProvider = "testIsRight")
    public void testIsRight(Either<Integer, Integer> either, boolean expected) throws Exception {
        assertEquals(either.isRight(), expected);
    }

    @DataProvider(name = "testOnLeft")
    Object[][] data4testOnLeft() {
        return new Object[][]{
                {Either.left(1), 1, true},
                {Either.left(2), 2, true},
                {Either.right(1), 1, false},
                {Either.right(2), 2, false},
        };
    }

    @Test(dataProvider = "testOnLeft")
    public void testOnLeft(Either<Integer, Integer> either,
                           Integer expectedArg,
                           boolean expected) throws Exception {
        final boolean[] accepted = {false};
        either.onLeft(
                x -> {
                    assertEquals(x, expectedArg);
                    accepted[0] = true;
                });
        assertEquals(accepted[0], expected);
    }

    @DataProvider(name = "testOnRight")
    Object[][] data4testOnRight() {
        return new Object[][]{
                {Either.left(1), 1, false},
                {Either.left(2), 2, false},
                {Either.right(1), 1, true},
                {Either.right(2), 2, true},
        };
    }

    @Test(dataProvider = "testOnRight")
    public void testOnRight(Either<Integer, Integer> either,
                            Integer expectedArg,
                            boolean expected) throws Exception {
        final boolean[] accepted = {false};
        either.onRight(
                x -> {
                    assertEquals(x, expectedArg);
                    accepted[0] = true;
                });
        assertEquals(accepted[0], expected);
    }

    @DataProvider(name = "testOnBoth")
    Object[][] data4testOnBoth() {
        return new Object[][]{
                {Either.left(1), 1, true, false},
                {Either.left(2), 2, true, false},
                {Either.right(3), 3, false, true},
                {Either.right(4), 4, false, true},
        };
    }

    @Test(dataProvider = "testOnBoth")
    public void testOnBoth(Either<Integer, Integer> either,
                           Integer expectedArg,
                           boolean lExpected,
                           boolean rExpected) throws Exception {
        final boolean[] lAccepted = {false};
        final boolean[] rAccepted = {false};
        either.onBoth(
                x -> {
                    assertEquals(x, expectedArg);
                    lAccepted[0] = true;
                },
                x -> {
                    assertEquals(x, expectedArg);
                    rAccepted[0] = true;
                });
        assertEquals(lAccepted[0], lExpected);
        assertEquals(rAccepted[0], rExpected);
    }
}