package com.lewisd.ksp.craftstats.vehicle;

import java.util.Comparator;

import com.lewisd.ksp.craftstats.gamedata.Part;

public class VehiclePartPartComparator implements Comparator<VehiclePart> {

    private final Comparator<Part> partComparator;

    public VehiclePartPartComparator(final Comparator<Part> partComparator) {
        this.partComparator = partComparator;
    }

    @Override
    public int compare(final VehiclePart vp1, final VehiclePart vp2) {
        final int result = partComparator.compare(vp1.getPart(), vp2.getPart());
        if (result == 0) {
            return vp1.getId().compareTo(vp2.getId());
        }
        return result;
    }

}
