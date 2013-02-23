package com.lewisd.ksp.craftstats.gamedata;

public class Atmosphere {

    public static final Atmosphere NONE = new Atmosphere(0, 0, 1);

    public static final Atmosphere EVE = new Atmosphere(5, 96708.574, 7000);
    public static final Atmosphere KERBIN = new Atmosphere(1, 69077.553, 5000);
    public static final Atmosphere DUNA = new Atmosphere(0.2, 41446.532, 3000);
    public static final Atmosphere JOOL = new Atmosphere(15, 138155.11, 9000);
    public static final Atmosphere LAYTHE = new Atmosphere(0.8, 55262.042, 4000);

    private static final double KERBIN_SURFACE_DENSITY = 1.2230948554874;

    private final double pressure;
    private final double scaleHeight;
    private final double height;

    public Atmosphere(final double pressure, final double height, final double scaleHeight) {
        this.pressure = pressure;
        this.height = height;
        this.scaleHeight = scaleHeight;
    }

    public double getPressure() {
        return pressure;
    }

    public double getScaleHeight() {
        return scaleHeight;
    }

    public double getHeight() {
        return height;
    }

    public double getPressure(final double altitude) {
        if (altitude > height) {
            return 0;
        }
        return pressure * Math.exp(-altitude / scaleHeight);
    }

    public double getDensity(final double altitude) {
        return getPressure(altitude) * KERBIN_SURFACE_DENSITY;
    }

}
