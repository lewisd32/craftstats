package com.lewisd.ksp.craftstats.vehicle;

import java.util.List;

import com.lewisd.ksp.craftstats.util.Vect3;

public class VehicleParts {

    public static VehiclePart getLowest(final List<VehiclePart> vehicleParts) {
        VehiclePart lowestPart = null;
        Vect3 lowestNode = null;
        for (final VehiclePart vehiclePart : vehicleParts) {
            final Vect3 node = vehiclePart.getLowestStackNode();
            if (node != null) {
                if (lowestNode == null || node.getY() < lowestNode.getY()) {
                    lowestPart = vehiclePart;
                    lowestNode = node;
                }
            }
        }
        return lowestPart;
    }

    public static VehiclePart getHighest(final List<VehiclePart> vehicleParts) {
        VehiclePart highestPart = null;
        Vect3 highestNode = null;
        for (final VehiclePart vehiclePart : vehicleParts) {
            final Vect3 node = vehiclePart.getLowestStackNode();
            if (node != null) {
                if (highestNode == null || node.getY() > highestNode.getY()) {
                    highestPart = vehiclePart;
                    highestNode = node;
                }
            }
        }
        return highestPart;

    }

}
