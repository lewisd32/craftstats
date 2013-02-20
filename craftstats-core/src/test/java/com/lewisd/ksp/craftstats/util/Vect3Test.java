package com.lewisd.ksp.craftstats.util;

import static com.lewisd.ksp.craftstats.util.Vect3TestUtil.assertVectEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Vect3Test {

    @Test
    public void testXYAngleNorth() {
        final Vect3 vect = Vect3.forXYAngle(0, 100);
        final Vect3 expected = new Vect3(0.0, -100.0, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(360.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleEast() {
        final Vect3 vect = Vect3.forXYAngle(90, 100);
        final Vect3 expected = new Vect3(100.0, 0.0, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(90.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleSouth() {
        final Vect3 vect = Vect3.forXYAngle(180, 100);
        final Vect3 expected = new Vect3(0.0, 100.0, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(180.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleWest() {
        final Vect3 vect = Vect3.forXYAngle(270, 100);
        final Vect3 expected = new Vect3(-100.0, 0.0, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(270.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleNorthNorthEast() {
        final Vect3 vect = Vect3.forXYAngle(30, 100);
        final Vect3 expected = new Vect3(50.0, -86.602540, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(30.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleNorthNorthWest() {
        final Vect3 vect = Vect3.forXYAngle(330, 100);
        final Vect3 expected = new Vect3(-50.0, -86.602540, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(330.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleSouthSouthEast() {
        final Vect3 vect = Vect3.forXYAngle(150, 100);
        final Vect3 expected = new Vect3(50.0, 86.602540, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(150.0, vect.getXYAngle(), 0.000001);
    }

    @Test
    public void testXYAngleSouthSouthWest() {
        final Vect3 vect = Vect3.forXYAngle(210, 100);
        final Vect3 expected = new Vect3(-50.0, 86.602540, 0.0);
        assertVectEquals(expected, vect);
        assertEquals(210.0, vect.getXYAngle(), 0.000001);
    }

}
