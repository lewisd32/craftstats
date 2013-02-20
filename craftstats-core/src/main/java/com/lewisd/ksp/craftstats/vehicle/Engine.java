package com.lewisd.ksp.craftstats.vehicle;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.Resource;
import com.lewisd.ksp.craftstats.gamedata.Resources;

public class Engine {

    private final VehiclePart vehiclePart;
    private final CfgGroup engineModule;
    private final Set<VehiclePart> usableFuelTanks = new HashSet<>();
    private final Set<VehiclePart> preferredFuelTanks = new HashSet<>();
    private final Map<Resource, Double> propellantRatios = new HashMap<>();
    private final Stage stage;

    public Engine(final Stage stage, final VehiclePart vehiclePart, final Collection<VehiclePart> stagedParts, final Resources resources) {
        this.stage = stage;
        this.vehiclePart = vehiclePart;
        engineModule = vehiclePart.getPart().getEngineModule();

        if (getSolidFuelMass() > 0) {
            usableFuelTanks.add(vehiclePart);
        } else {
            usableFuelTanks.addAll(findConnectedFuelTanks(vehiclePart));
            usableFuelTanks.retainAll(stagedParts);
        }

        for (final CfgGroup propellantGroup : engineModule.getSubgroup("PROPELLANT")) {
            final Resource resource = resources.getResource(propellantGroup.getProperties().get("name"));
            final double ratio = propellantGroup.getProperties().getDouble("ratio");
            propellantRatios.put(resource, ratio);
        }
    }

    public Set<VehiclePart> getUsableFuelTanks() {
        return usableFuelTanks;
    }

    public Set<VehiclePart> getPreferredFuelTanks() {
        return preferredFuelTanks;
    }

    public void setPreferredFuelTanks(final Set<VehiclePart> fuelTanks) {
        preferredFuelTanks.clear();
        preferredFuelTanks.addAll(fuelTanks);
    }

    public VehiclePart getVehiclePart() {
        return vehiclePart;
    }

    public double getMaxThrust() {
        return engineModule.getProperties().getDouble("maxThrust");
    }

    public double getThrust(final double thrustPercentage) {
        return getMaxThrust() * (thrustPercentage / 100);
    }

    public double getIsp() {
        final Environment environment = stage.getEnvironment();
        final double pressure = Math.min(1, environment.getAtmosphericPressure(stage.getAltitude()));
        return (pressure * getAtmoIsp()) + ((1 - pressure) * getVacuumIsp());
    }

    public double getAtmoIsp() {
        final CfgProperties ispCfgProperties = engineModule.getSubgroup("atmosphereCurve").get(0).getProperties();
        return getIsp(ispCfgProperties, "1 ");
    }

    public double getVacuumIsp() {
        final CfgProperties ispCfgProperties = engineModule.getSubgroup("atmosphereCurve").get(0).getProperties();
        return getIsp(ispCfgProperties, "0 ");
    }

    public double getSolidFuelMass() {
        return vehiclePart.getSolidFuelMass();
    }

    public boolean consumeFuel(final double mass) {
        boolean outOfFuel = false;
        if (preferredFuelTanks.size() == 0) {
            return false;
        }
        final double massPerTank = mass / preferredFuelTanks.size() / propellantRatios.size();
        for (final VehiclePart fuelTank : preferredFuelTanks) {
            for (final Resource resource : propellantRatios.keySet()) {
                final double ratio = propellantRatios.get(resource);

                final double resourceMass = fuelTank.getResourceMass(resource);
                final double newResourceMass = resourceMass - massPerTank * ratio;
                if (newResourceMass < 0) {
                    outOfFuel = true;
                }
                fuelTank.setResourceMass(resource, Math.max(0, newResourceMass));
            }
        }
        return !outOfFuel;
    }

    private static double getIsp(final CfgProperties ispCfgProperties, final String valuePrefix) {
        final List<String> values = ispCfgProperties.getList("key");
        for (final String value : values) {
            if (value.startsWith(valuePrefix)) {
                return Double.parseDouble(value.substring(valuePrefix.length()).trim());
            }
        }
        throw new IllegalStateException("No vacuum isp found in: " + values);
    }

    private Set<VehiclePart> findConnectedFuelTanks(final VehiclePart engine) {
        final Set<VehiclePart> connectedParts = new HashSet<>();

        final Set<VehiclePart> scanned = new HashSet<>();
        final Deque<VehiclePart> parts = new ArrayDeque<>();
        parts.add(engine);
        scanned.add(engine);
        while (!parts.isEmpty()) {
            final VehiclePart part = parts.pop();

            final VehiclePart parent = part.getParent();
            if (parent != null) {
                if (!scanned.contains(parent) && parent.getPart().getFuelCrossFeed()) {
                    scanned.add(parent);
                    connectedParts.add(parent);
                    parts.add(parent);
                }
            }

            for (final VehiclePart child : part.getChildren()) {
                if (!scanned.contains(child) && child.getPart().getFuelCrossFeed()) {
                    scanned.add(child);
                    connectedParts.add(child);
                    parts.add(child);
                }
            }
        }

        final Set<VehiclePart> connectedFuelTanks = new HashSet<>();
        for (final VehiclePart vehiclePart : connectedParts) {
            if (vehiclePart.getLOXFuelMass() > 0) {
                connectedFuelTanks.add(vehiclePart);
            }
        }
        return connectedFuelTanks;
    }

}
