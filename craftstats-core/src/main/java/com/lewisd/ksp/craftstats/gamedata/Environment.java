package com.lewisd.ksp.craftstats.gamedata;

import com.lewisd.ksp.craftstats.util.Vect3;

public class Environment {

    public static final Environment KERBIN = new Environment("Kerbin", 600000, 3.5316E+12, 3431.03, 174.53, Atmosphere.KERBIN);
    public static final Environment KERBIN_VACUUM = new Environment("Kerbin in vacuum", 600000, 3.5316E+12, 3431.03, 174.53, Atmosphere.NONE);
    public static final Environment MUN = new Environment("Mun", 200000, 65138397521.0, 807.08, 9.0416, Atmosphere.NONE);

    private final String name;
    private final double radiusAtSurface;
    private final double gravitationalParam;
    private final double escapeVelocity;
    private final double rotationalVelocity;
    private final Atmosphere atmosphere;

    public Environment(final String name, final double radiusAtSurface, final double gravitationalParam, final double escapeVelocity,
                       final double rotationalVelocity, final Atmosphere atmosphere) {
        this.name = name;
        this.radiusAtSurface = radiusAtSurface;
        this.gravitationalParam = gravitationalParam;
        this.escapeVelocity = escapeVelocity;
        this.rotationalVelocity = rotationalVelocity;
        this.atmosphere = atmosphere;
    }

    public String getName() {
        return name;
    }

    public double getRadiusAtSurface() {
        return radiusAtSurface;
    }

    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    public double getEscapeVelocity() {
        return escapeVelocity;
    }

    public double getAtmosphericPressure(final double altitude) {
        return atmosphere.getPressure(altitude);
    }

    public double getGravity(final double altitude) {
        final double radius = getRadius(altitude);
        return gravitationalParam / (radius * radius);
    }

    public double getRadius(final double altitude) {
        return radiusAtSurface + altitude;
    }

    public double getOrbitalVelocity(final int altitude) {
        return Math.sqrt(gravitationalParam / (radiusAtSurface + altitude));
    }

    public Vect3 getInitialPosition() {
        return new Vect3(0, radiusAtSurface, 0);
    }

    public Vect3 getInitialVelocity() {
        return getRotationalVelocityVector(getInitialPosition());
    }

    public Vect3 getRotationalVelocityVector(final Vect3 position) {
        final double altitude = position.getLength() - radiusAtSurface;
        final double velocity = getRotationalVelocity(altitude);

        final double positionAngle = position.getXYAngle();
        final double rotationalVelocityAngle = positionAngle - 90;
        return Vect3.forXYAngle(rotationalVelocityAngle, velocity);
    }

    public double getRotationalVelocity(final double altitude) {
        final double radius = getRadius(altitude);
        return (radius / radiusAtSurface) * rotationalVelocity;
    }

    public static Environment getNamed(final String name) {
        if ("kerbin".equalsIgnoreCase(name)) {
            return KERBIN;
        } else if ("kerbin_vacuum".equalsIgnoreCase(name)) {
            return KERBIN_VACUUM;
        } else if ("mun".equalsIgnoreCase(name)) {
            return MUN;
        }
        throw new IllegalArgumentException("No environment found for name '" + name + "'");
    }

}
