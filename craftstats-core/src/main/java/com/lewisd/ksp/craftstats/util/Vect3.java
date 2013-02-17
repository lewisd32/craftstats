package com.lewisd.ksp.craftstats.util;

public class Vect3 {

    private double x;
    private double y;
    private double z;


    public Vect3(final Vect3 vect) {
        this.x = vect.x;
        this.y = vect.y;
        this.z = vect.z;
    }

    public Vect3(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(final double z) {
        this.z = z;
    }

    public void add(final Vect3 vect) {
        x = x + vect.x;
        y = y + vect.y;
        z = z + vect.z;
    }

    public void subtract(final Vect3 vect) {
        x = x - vect.x;
        y = y - vect.y;
        z = z - vect.z;
    }

    public void multiply(final double m) {
        x = x * m;
        y = y * m;
        z = z * m;
    }

    public static Vect3 parse(final String string) {
        final String parts[] = string.split(",");

        final double x = Double.parseDouble(parts[0]);
        final double y = Double.parseDouble(parts[1]);
        final double z = Double.parseDouble(parts[2]);

        return new Vect3(x, y, z);
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void rotate(final double x, final double y, final double z) {
        // TODO: implement rotation around each of these axis
    }

}
