package com.lewisd.ksp.craftstats.vehicle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.PartTitleComparator;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.util.Vect3;

public class Vehicle {

    private static final double DRAG_MULTIPLIER = 0.008;

    private final Map<String, VehiclePart> partByIdMap = new HashMap<>();
    private final SortedMap<Integer, Stage> stages = new TreeMap<>();

    private final CfgGroup vehicleGroup;
    private final VehiclePart rootPart;
    private Environment environment;
    private Vect3 position;
    private Vect3 velocity;
    private double pitch;


    public Vehicle(final CfgGroup root, final List<VehiclePart> vehicleParts, final Resources resources) {
        vehicleGroup = root;

        rootPart = vehicleParts.get(0);
        for (final VehiclePart vehiclePart : vehicleParts) {
            final String id = vehiclePart.getId();
            partByIdMap.put(id, vehiclePart);
        }

        buildPartTree();

        final SortedSet<Integer> stageNumberSet = findStageNumbers();
        buildStages(vehicleParts, stageNumberSet, resources);
        determineStageFuel();
        findLaunchClamps();
    }

    public String getName() {
        return vehicleGroup.getProperties().get("ship");
    }

    public String getVersion() {
        return vehicleGroup.getProperties().get("version");
    }

    public String getType() {
        return vehicleGroup.getProperties().get("type");
    }

    public double getHeight() {
        // FIXME: This should be calculated from the parts. May need to take launch pad into account too.
        return 87;
    }

    public VehiclePart getRootPart() {
        return rootPart;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
        for (final Stage stage : stages.values()) {
            stage.setEnvironment(environment);
        }
    }

    public double getAltitude() {
        return position.getLength() - environment.getRadiusAtSurface();
    }

    public void setAltitude(final double altitude) {
        final double length = position.getLength();
        position.multiply((altitude + environment.getRadiusAtSurface()) / length);
    }

    public Vect3 getPosition() {
        return position;
    }

    public void setPosition(final Vect3 position) {
        this.position = position;
    }

    public Vect3 getVelocity() {
        return velocity;
    }

    public void setVelocity(final Vect3 velocity) {
        this.velocity = velocity;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(final double pitch) {
        this.pitch = pitch;
    }

    public void initSimulation() {
        velocity = environment.getInitialVelocity();
        position = environment.getInitialPosition();
        pitch = 0;
        setAltitude(getHeight());
    }

    public Collection<VehiclePart> getAllParts() {
        final List<VehiclePart> parts = new ArrayList<>();
        parts.addAll(partByIdMap.values());
        Collections.sort(parts, new VehiclePartPartComparator(new PartTitleComparator()));
        return parts;
    }

    public Stage getCurrentStage() {
        return getStagesFromBottom().get(0);
    }

    public List<Stage> getStagesFromTop() {
        return new ArrayList<>(stages.values());
    }

    public List<Stage> getStagesFromBottom() {
        final List<Stage> reverseStages = new ArrayList<>();
        reverseStages.addAll(stages.values());
        Collections.reverse(reverseStages);
        return reverseStages;
    }

    public Stage getStage(final int stage) {
        return stages.get(stage);
    }

    public double getMass() {
        return getStagesFromBottom().get(0).getMass();
    }

    public double getDryMass() {
        return getStagesFromBottom().get(0).getDryMass();
    }

    public void ejectStage() {
        final Stage bottomStage = getStagesFromBottom().get(0);
        stages.remove(bottomStage.getNumber());
    }

    public double getCoefficientOfDrag() {
        return 0.2; // Not really constant, but is probably ok for most rockets.
    }

    public Vect3 getDragAcceleration() {
        // FIXME: hard-coded for vertical flight only. Fix when making pitch work.
        final double v = getVelocity().getLength();
        final double force = 0.5 * getMass() * DRAG_MULTIPLIER * getCoefficientOfDrag() * environment.getAtmosphere().getDensity(getAltitude()) * v * v;
        final double accel = force / getMass();

        if (getVelocity().getY() > 0) {
            return new Vect3(0, -accel, 0);
        } else {
            return new Vect3(0, accel, 0);
        }
    }

    private void buildPartTree() {
        final Set<VehiclePart> connected = new HashSet<>();
        final Deque<VehiclePart> leafs = new ArrayDeque<VehiclePart>();
        leafs.push(rootPart);
        connected.add(rootPart);

        while (!leafs.isEmpty()) {
            final VehiclePart leaf = leafs.pollLast();
            final Set<String> linkedPartNames = leaf.getAllLinkedPartNames();

            for (final String linkedPartName : linkedPartNames) {
                final VehiclePart linkedPart = partByIdMap.get(linkedPartName);
                if (linkedPart == null) {
                    throw new RuntimeException("No part found: " + linkedPartName);
                }
                if (!connected.contains(linkedPart)) {
                    leaf.addChild(linkedPart);
                    leafs.push(linkedPart);
                    connected.add(linkedPart);
                }
            }
        }
    }

    private SortedSet<Integer> findStageNumbers() {
        final SortedSet<Integer> stageNumberSet = new TreeSet<>();
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final int stageNumber = vehiclePart.getQueuedStage();
            if (stageNumber >= 0) {
                stageNumberSet.add(stageNumber);
            }
        }
        return stageNumberSet;
    }

    private Map<Integer, List<VehiclePart>> findDecouplers() {
        final Map<Integer, List<VehiclePart>> stageDecouplers = new HashMap<>();
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final int stageNumber = vehiclePart.getQueuedStage();
            if (stageNumber >= 0) {
                if (vehiclePart.getPart().isDecoupler() || vehiclePart.getPart().isLaunchClamp()) {
                    List<VehiclePart> decouplers = stageDecouplers.get(stageNumber);
                    if (decouplers == null) {
                        decouplers = new ArrayList<>();
                        stageDecouplers.put(stageNumber, decouplers);
                    }
                    decouplers.add(vehiclePart);
                }
            }
        }
        return stageDecouplers;
    }

    private void buildStages(final List<VehiclePart> vehicleParts, final SortedSet<Integer> stageNumberSet, final Resources resources) {
        final Map<Integer, List<VehiclePart>> stageDecouplers = findDecouplers();

        final List<VehiclePart> stagedParts = new ArrayList<>(vehicleParts);
        final List<Integer> reverseStageNumbers = new ArrayList<>(stageNumberSet);
        Collections.reverse(reverseStageNumbers);


        for (final int stageNumber : reverseStageNumbers) {
            final List<VehiclePart> decouplers = stageDecouplers.get(stageNumber);
            final List<VehiclePart> droppedParts = new ArrayList<>();
            if (decouplers != null) {
                for (final VehiclePart decoupler : decouplers) {
                    droppedParts.add(decoupler);
                    droppedParts.addAll(decoupler.getDescendants());
                }
            }

            droppedParts.retainAll(stagedParts);
            stagedParts.removeAll(droppedParts);

            final Stage stage = new Stage(this, stageNumber, stagedParts, droppedParts, resources);
            stages.put(stageNumber, stage);
        }
    }

    private void determineStageFuel() {
        final Set<VehiclePart> fuelTanksUsedAbove = new HashSet<>();
        for (final Stage stage : getStagesFromTop()) {
            final Set<VehiclePart> stageFuelTanks = new HashSet<>();
            for (final Engine engine : stage.getActiveEngines()) {
                stageFuelTanks.addAll(engine.getUsableFuelTanks());
            }

            for (final Engine engine : stage.getActiveEngines()) {
                final Set<VehiclePart> preferredFuelTanks = new HashSet<>();
                preferredFuelTanks.addAll(engine.getUsableFuelTanks());
                preferredFuelTanks.removeAll(fuelTanksUsedAbove);
                engine.setPreferredFuelTanks(preferredFuelTanks);
            }

            fuelTanksUsedAbove.addAll(stageFuelTanks);
        }
    }

    private void findLaunchClamps() {
        boolean launchClampsReleased = true;
        for (final Stage stage : stages.values()) {
            if (stage.hasLaunchClamps()) {
                launchClampsReleased = false;
                break;
            }
        }

        if (launchClampsReleased) {
            return;
        }

        int finalLaunchClampStage = Integer.MAX_VALUE;
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            if (vehiclePart.getPart().isLaunchClamp()) {
                finalLaunchClampStage = Math.min(finalLaunchClampStage, vehiclePart.getQueuedStage());
            }
        }

        for (final Stage stage : stages.values()) {
            if (stage.getNumber() <= finalLaunchClampStage) {
                stage.setLaunchClampsReleased(true);
            } else {
                stage.setLaunchClampsReleased(false);
            }
        }
    }

}
