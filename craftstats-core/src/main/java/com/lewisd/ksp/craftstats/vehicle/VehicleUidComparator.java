package com.lewisd.ksp.craftstats.vehicle;

import java.util.Comparator;

public class VehicleUidComparator implements Comparator<VehiclePart> {

    @Override
    public int compare(final VehiclePart vp1, final VehiclePart vp2) {
        return Long.compare(vp1.getUid(), vp2.getUid());
    }

}
