package com.lewisd.ksp.craftstats.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.PartTitleComparator;
import com.lewisd.ksp.craftstats.gamedata.Resources;

public class Stage {

    private final int stageNumber;
    private final Map<String, VehiclePart> partByIdMap = new HashMap<>();
    private final Set<VehiclePart> droppedParts = new HashSet<>();
    private final Set<Engine> activeEngines = new HashSet<>();
    private final Vehicle vehicle;
    private Environment environment;
    private Double altitude;

    private boolean launchClampsReleased = true;

    public Stage(final Vehicle vehicle, final int stageNumber, final Collection<VehiclePart> stagedParts, final Collection<VehiclePart> droppedParts,
                 final Resources resources) {
        this.vehicle = vehicle;
        this.environment = vehicle.getEnvironment();
        this.stageNumber = stageNumber;

        for (final VehiclePart vehiclePart : stagedParts) {
            final String id = vehiclePart.getId();
            partByIdMap.put(id, vehiclePart);
        }

        for (final VehiclePart vehiclePart : droppedParts) {
            this.droppedParts.add(vehiclePart);
        }

        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final Part part = vehiclePart.getPart();
            if (part.isEngine()) {
                if (vehiclePart.getActivateStage() >= stageNumber) {
                    activeEngines.add(new Engine(this, vehiclePart, stagedParts, resources));
                }
            }
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    public Double getAltitude() {
        if (altitude == null) {
            return vehicle.getAltitude();
        }
        return altitude;
    }

    public void setAltitude(final Double altitude) {
        this.altitude = altitude;
    }

    public int getNumber() {
        return stageNumber;
    }

    public Collection<VehiclePart> getAllParts() {
        final List<VehiclePart> parts = new ArrayList<>();
        parts.addAll(partByIdMap.values());
        Collections.sort(parts, new VehiclePartPartComparator(new PartTitleComparator()));
        return parts;
    }

    public Collection<VehiclePart> getDroppedParts() {
        final List<VehiclePart> parts = new ArrayList<>();
        parts.addAll(droppedParts);
        Collections.sort(parts, new VehiclePartPartComparator(new PartTitleComparator()));
        return parts;
    }

    public Collection<Engine> getActiveEngines() {
        return activeEngines;
    }

    public Engine getEngineIfActive(final VehiclePart vehiclePart) {
        for (final Engine engine : activeEngines) {
            if (engine.getVehiclePart() == vehiclePart) {
                return engine;
            }
        }
        return null;
    }

    public double getMass() {
        double mass = 0;
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            mass += vehiclePart.getMass();
        }
        return mass;
    }

    public double getDryMass() {
        return getMass() - getLOXFuelMass() - getSolidFuelMass();
    }

    public double getIsp() {
        final double totalThrust = getMaxThrust();
        if (totalThrust == 0) {
            return 0;
        }

        double weightedIsp = 0;
        for (final Engine engine : getActiveEngines()) {
            final double maxThrust = engine.getMaxThrust();
            final double isp = engine.getIsp();
            weightedIsp = maxThrust / isp;
        }
        return totalThrust / weightedIsp;
    }

    public double getMaxThrust() {
        double totalThrust = 0;
        for (final Engine engine : getActiveEngines()) {
            final double maxThrust = engine.getMaxThrust();
            totalThrust += maxThrust;
        }
        return totalThrust;
    }

    public double getDeltaV() {
        double totalDeltaV = 0;
        final double stageMass = getMass();

        // FIXME Just dividing by the total without weighting based on per-engine fuel consumption rates is wrong
        int totalLiquidEngines = 0;
        for (final Engine engine : activeEngines) {
            if (engine.getSolidFuelMass() == 0) {
                ++totalLiquidEngines;
            }
        }

        final double g = environment.getGravity(vehicle.getAltitude());

        for (final Engine engine : activeEngines) {
            double burnedFuelMass;
            if (engine.getSolidFuelMass() > 0) {
                burnedFuelMass = engine.getSolidFuelMass();
            } else {
                burnedFuelMass = getLOXFuelMass() / totalLiquidEngines;
            }
            final double deltaV = g * engine.getIsp() * Math.log(stageMass / (stageMass - burnedFuelMass));
            totalDeltaV += deltaV;
        }
        return totalDeltaV;
    }

    public double getLOXFuelMass() {
        double fuelMass = 0;
        final Set<VehiclePart> fuelTanks = new HashSet<>();
        for (final Engine engine : getActiveEngines()) {
            fuelTanks.addAll(engine.getPreferredFuelTanks());
        }

        for (final VehiclePart fuelTank : fuelTanks) {
            fuelMass += fuelTank.getLOXFuelMass();
        }
        return fuelMass;
    }

    public double getSolidFuelMass() {
        double fuelMass = 0;
        for (final Engine engine : activeEngines) {
            fuelMass += engine.getSolidFuelMass();
        }
        return fuelMass;
    }

    public boolean hasLaunchClamps() {
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            if (vehiclePart.getPart().isLaunchClamp()) {
                return true;
            }
        }
        return false;
    }

    public boolean launchClampsReleased() {
        return launchClampsReleased;
    }

    public void setLaunchClampsReleased(final boolean launchClampsReleased) {
        this.launchClampsReleased = launchClampsReleased;
    }

}
