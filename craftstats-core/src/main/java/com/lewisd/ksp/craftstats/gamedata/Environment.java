package com.lewisd.ksp.craftstats.gamedata;

import com.lewisd.ksp.craftstats.util.Vect3;

public class Environment {

    public static final Environment KERBIN = new Environment("Kerbin", 600000, 3.5316E+12, 3431.03, Atmosphere.KERBIN);
    public static final Environment KERBIN_VACUUM = new Environment("Kerbin in vacuum", 600000, 3.5316E+12, 3431.03, Atmosphere.NONE);
    public static final Environment MUN = new Environment("Mun", 200000, 65138397521.0, 807.08, Atmosphere.NONE);

    private final double radiusAtSurface;
    private final double gravitationalParam;
    private final Atmosphere atmosphere;
    private final double escapeVelocity;
    private final String name;

    public Environment(final String name, final double radiusAtSurface, final double gravitationalParam, final double escapeVelocity,
                       final Atmosphere atmosphere) {
        this.name = name;
        this.radiusAtSurface = radiusAtSurface;
        this.gravitationalParam = gravitationalParam;
        this.escapeVelocity = escapeVelocity;
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

    public Vect3 getInitialVelocity() {
        // return new Vect3(getOrbitalVelocity(0), 0, 0);
        return new Vect3(0, 0, 0);
    }

    public Vect3 getInitialPosition() {
        return new Vect3(0, radiusAtSurface, 0);
    }

    public static Environment getNamed(final String name) {
        if ("kerbin".equalsIgnoreCase(name)) {
            return KERBIN;
        } else if ("mun".equalsIgnoreCase(name)) {
            return MUN;
        }
        throw new IllegalArgumentException("No environment found for name '" + name + "'");
    }

}
