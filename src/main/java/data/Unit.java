/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package data;

/**
 * Unit type to represent one and only value
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-20)
 */
public final class Unit {
    private static final Unit UNIT = new Unit();

    private Unit() {
    }

    public static Unit unit() {
        return UNIT;
    }
}