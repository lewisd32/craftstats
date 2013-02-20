package com.lewisd.ksp.craftstats.vehicle;

import java.util.Comparator;

import com.lewisd.ksp.craftstats.util.Vect3;

public class Vect3YComparator implements Comparator<Vect3> {

    @Override
    public int compare(final Vect3 v1, final Vect3 v2) {
        return Double.compare(v1.getY(), v2.getY());
    }

}
