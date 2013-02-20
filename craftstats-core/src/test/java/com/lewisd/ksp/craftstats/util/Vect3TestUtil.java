package com.lewisd.ksp.craftstats.util;

import static org.junit.Assert.assertEquals;

public class Vect3TestUtil {

    public static void assertVectEquals(final Vect3 expected, final Vect3 actual) {
        assertEquals("Expected " + expected + " but was " + actual, expected.getX(), actual.getX(), 0.000001);
        assertEquals("Expected " + expected + " but was " + actual, expected.getY(), actual.getY(), 0.000001);
        assertEquals("Expected " + expected + " but was " + actual, expected.getZ(), actual.getZ(), 0.000001);
    }

}
