package com.lewisd.ksp.craftstats.gamedata;

import static com.lewisd.ksp.craftstats.util.Vect3TestUtil.assertVectEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.lewisd.ksp.craftstats.util.Vect3;

public class EnvironmentTest {

    @Test
    public void testKerbinPressure0k() {
        assertEquals(1, Environment.KERBIN.getAtmosphericPressure(0), 0.0001);
    }

    @Test
    public void testKerbinPressure10k() {
        assertEquals(0.1353, Environment.KERBIN.getAtmosphericPressure(10000), 0.0001);
    }

    @Test
    public void testKerbinPressure20k() {
        assertEquals(0.0183, Environment.KERBIN.getAtmosphericPressure(20000), 0.0001);
    }

    @Test
    public void testKerbinPressure60k() {
        assertEquals(0.00000614, Environment.KERBIN.getAtmosphericPressure(60000), 0.0000001);
    }

    @Test
    public void testKerbinPressure70k() {
        assertEquals(0.0, Environment.KERBIN.getAtmosphericPressure(70000), 0.0000001);
    }

    @Test
    public void testKerbinGravity0k() {
        assertEquals(9.81, Environment.KERBIN.getGravity(0), 0.0001);
    }

    @Test
    public void testKerbinGravity10k() {
        assertEquals(9.491, Environment.KERBIN.getGravity(10000), 0.0001);
    }

    @Test
    public void testKerbinGravity20k() {
        assertEquals(9.1873, Environment.KERBIN.getGravity(20000), 0.0001);
    }

    @Test
    public void testKerbinGravity60k() {
        assertEquals(8.1074, Environment.KERBIN.getGravity(60000), 0.0001);
    }

    @Test
    public void testKerbinGravity70k() {
        assertEquals(7.8672, Environment.KERBIN.getGravity(70000), 0.0001);
    }

    @Test
    public void testKerbinGravity200k() {
        assertEquals(5.5181, Environment.KERBIN.getGravity(200000), 0.0001);
    }

    @Test
    public void testMunGravity0k() {
        assertEquals(1.6285, Environment.MUN.getGravity(0), 0.0001);
    }

    @Test
    public void testKerbinOrbitalVelocity0k() {
        assertEquals(2426.1, Environment.KERBIN.getOrbitalVelocity(0), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity10k() {
        assertEquals(2406.1, Environment.KERBIN.getOrbitalVelocity(10000), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity20k() {
        assertEquals(2386.6, Environment.KERBIN.getOrbitalVelocity(20000), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity40k() {
        assertEquals(2349.1, Environment.KERBIN.getOrbitalVelocity(40000), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity60k() {
        assertEquals(2313.2, Environment.KERBIN.getOrbitalVelocity(60000), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity70k() {
        assertEquals(2295.8, Environment.KERBIN.getOrbitalVelocity(70000), 0.1);
    }

    @Test
    public void testKerbinOrbitalVelocity200k() {
        assertEquals(2101.1, Environment.KERBIN.getOrbitalVelocity(200000), 0.1);
    }

    @Test
    public void testRotationalVelocity() {
        final double rotationalVelocity = Environment.KERBIN.getRotationalVelocity(2868.75072505 * 1000);
        assertEquals(1009.001773, rotationalVelocity, 0.000001);
    }

    @Test
    public void testRotationalVelocityVectorSurfaceNorth() {
        final Vect3 rotationalVelocityVector = Environment.KERBIN.getRotationalVelocityVector(new Vect3(0, -600000, 0));
        final Vect3 expected = new Vect3(174.53, 0.0, 0.0);
        assertVectEquals(expected, rotationalVelocityVector);
    }

    @Test
    public void testRotationalVelocityVectorSurfaceEast() {
        final Vect3 rotationalVelocityVector = Environment.KERBIN.getRotationalVelocityVector(new Vect3(600000, 0, 0));
        final Vect3 expected = new Vect3(0.0, 174.53, 0.0);
        assertVectEquals(expected, rotationalVelocityVector);
    }

    @Test
    public void testRotationalVelocityVectorGeosyncNorth() {
        final Vect3 rotationalVelocityVector = Environment.KERBIN.getRotationalVelocityVector(new Vect3(0, -600000 - 2868.75072505 * 1000, 0));
        final Vect3 expected = new Vect3(1009.001773, 0.0, 0.0);
        assertVectEquals(expected, rotationalVelocityVector);
    }

}
