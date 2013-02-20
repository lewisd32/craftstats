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

    public Vect3 unitVector(final double len) {
        return Vect3.forXYAngle(getXYAngle(), len);
    }

    public double getXYAngle() {
        return normalizeAngle(Math.toDegrees(Math.atan2(y, x)) + 90);
    }

    public static Vect3 forXYAngle(final double positionAngle, final double velocity) {
        final double angle = normalizeAngle(positionAngle - 90);
        final double radians = Math.toRadians(angle);

        final double x = Math.cos(radians) * velocity;
        final double y = Math.sin(radians) * velocity;
        return new Vect3(x, y, 0);
    }

    private static double normalizeAngle(final double angle) {
        if (angle < 0) {
            return angle + 360;
        } else if (angle >= 360) {
            return angle - 360;
        } else {
            return angle;
        }
    }

    @Override
    public String toString() {
        return String.format("%f, %f, %f", x, y, z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Vect3)) {
            return false;
        }
        final Vect3 other = (Vect3) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }
}
