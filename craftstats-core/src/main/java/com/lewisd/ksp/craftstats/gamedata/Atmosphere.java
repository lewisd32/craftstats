package com.lewisd.ksp.craftstats.gamedata;

public class Atmosphere {

    public static final Atmosphere KERBIN = new Atmosphere(1, 69077.553);
    public static final Atmosphere NONE = new Atmosphere(0, 0);

    private static final double KERBIN_SURFACE_DENSITY = 1.2230948554874;

    private final double pressure;
    private final double scaleHeight = 5000;
    private final double height;

    public Atmosphere(final double pressure, final double height) {
        this.pressure = pressure;
        this.height = height;
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
