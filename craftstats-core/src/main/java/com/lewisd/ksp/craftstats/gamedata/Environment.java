package com.lewisd.ksp.craftstats.gamedata;

import com.lewisd.ksp.craftstats.util.Vect3;

public class Environment {

    // TODO: Should I merge Atmosphere into Environment?

    // Name, radius, grav(mu), escapeVel, rotationalVel
    public static final Environment KERBOL = new Environment("Kerbol", 261600000, 1.172332794E+18, 94672.01, 3804.8, Atmosphere.NONE);
    public static final Environment MOHO = new Environment("Moho", 250000, 245250003655.0, 1400.71, 1.2982, Atmosphere.NONE);
    public static final Environment EVE = new Environment("Eve", 700000, 8171730229211.0, 4831.96, 54.636, Atmosphere.EVE);
    public static final Environment GILLY = new Environment("Gilly", 13000, 8289450, 35.71, 2.8909, Atmosphere.NONE);
    public static final Environment KERBIN = new Environment("Kerbin", 600000, 3.5316E+12, 3431.03, 174.53, Atmosphere.KERBIN);
    public static final Environment MUN = new Environment("Mun", 200000, 65138397521.0, 807.08, 9.0416, Atmosphere.NONE);
    public static final Environment MINMUS = new Environment("Minmus", 60000, 1765800026, 242.61, 9.3315, Atmosphere.NONE);
    public static final Environment DUNA = new Environment("Duna", 320000, 301363211975.0, 1372.41, 30.688, Atmosphere.DUNA);
    public static final Environment IKE = new Environment("Ike", 130000, 18568368573.0, 534.48, 12.467, Atmosphere.NONE);
    public static final Environment DRES = new Environment("Dres", 138000, 21484488600.0, 558, 24.916, Atmosphere.NONE);
    public static final Environment JOOL = new Environment("Jool", 600000, 282528004209995.0, 9704.43, 1047.2, Atmosphere.JOOL);
    public static final Environment LAYTHE = new Environment("Laythe", 500000, 1962000029236.0, 2801.43, 59.297, Atmosphere.LAYTHE);
    public static final Environment VALL = new Environment("Vall", 300000, 207481499474.0, 1176.1, 17.789, Atmosphere.NONE);
    public static final Environment TYLO = new Environment("Tylo", 600000, 2825280042100.0, 3068.81, 17.789, Atmosphere.NONE);
    public static final Environment BOP = new Environment("Bop", 65000, 2486834944.0, 276.62, 0.75005, Atmosphere.NONE);
    public static final Environment POL = new Environment("Pol", 44000, 227905920, 181.12, 0.30653, Atmosphere.NONE);
    public static final Environment EELOO = new Environment("Eeloo", 210000, 74410814527.0, 841.83, 67.804, Atmosphere.NONE);

    public static final Environment KERBIN_VACUUM = new Environment("Kerbin in vacuum", 600000, 3.5316E+12, 3431.03, 174.53, Atmosphere.NONE);

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
