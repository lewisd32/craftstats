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
import com.lewisd.ksp.craftstats.cfg.CfgKeyValueLine;
import com.lewisd.ksp.craftstats.cfg.CfgLine;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.PartTitleComparator;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.util.Vect3;

public class Vehicle {

    public static final long HIGH_UID = 4294878628L;
    public static final long UID_INCREMENT = 24;

    private static final double DRAG_MULTIPLIER = 0.008;


    private final Map<String, VehiclePart> partByIdMap = new HashMap<>();
    private final SortedMap<Integer, Stage> stages = new TreeMap<>();

    private final CfgGroup vehicleGroup;
    private final VehiclePart rootPart;
    private Environment environment;
    private Vect3 position;
    private Vect3 velocity;
    private double pitch;

    public Vehicle(final CfgGroup root, final Parts parts, final Resources resources) {
        vehicleGroup = root;

        final List<VehiclePart> vehicleParts = new ArrayList<>();
        final List<CfgGroup> partGroups = root.getSubgroup("PART");
        for (final CfgGroup partGroup : partGroups) {
            final CfgProperties cfgProperties = partGroup.getProperties();
            final String id = cfgProperties.get("part");
            final String partName = id.substring(0, id.indexOf('_'));
            final Part part = parts.getPart(partName);

            final VehiclePart vehiclePart = new VehiclePart(part, partGroup, resources);
            vehiclePart.setVehicle(this);
            vehicleParts.add(vehiclePart);
        }

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

    public Vehicle cloneFromCfg(final Parts parts, final Resources resources) {
        return new Vehicle(vehicleGroup, parts, resources);
    }

    public void addParts(final Collection<VehiclePart> vehicleParts) {
        for (final VehiclePart vehiclePart : vehicleParts) {
            final String id = vehiclePart.getId();
            if (partByIdMap.containsKey(id)) {
                throw new IllegalArgumentException("Cannot add part with duplicate id " + id);
            }
            vehiclePart.setVehicle(this);
            partByIdMap.put(id, vehiclePart);
            vehicleGroup.addSubgroup(vehiclePart.getCfgGroup());
        }
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

    public void setName(final String vehicleName) {
        vehicleGroup.getProperties().getLine("ship").setValue(vehicleName);
    }

    public long getLowestUid() {
        return getPartsByUidOrder().get(0).getUid();
    }

    public long getHighestUid() {
        return getPartsByReverseUidOrder().get(0).getUid();
    }

    public double getHeight() {
        // FIXME: This should be calculated from the parts. May need to take launch pad into account too.
        return 87;
    }

    public void reposition(final Vect3 offset) {
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final Vect3 oldPosition = vehiclePart.getPosition();
            final Vect3 partPosition = vehiclePart.getPosition();
            partPosition.add(offset);
            vehiclePart.setPosition(partPosition);
        }
    }

    public VehiclePart getLowestDecoupler() {
        final List<VehiclePart> decouplers = findDecouplers();
        return VehicleParts.getLowest(decouplers);
    }

    public VehiclePart getHighestDecoupler() {
        final List<VehiclePart> decouplers = findDecouplers();
        return VehicleParts.getHighest(decouplers);
    }

    public CfgGroup getCfgGroup() {
        return vehicleGroup;
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
        /*
        final double v = getVelocity().getLength();
        final double force = 0.5 * getMass() * DRAG_MULTIPLIER * getCoefficientOfDrag() * environment.getAtmosphere().getDensity(getAltitude()) * v * v;
        final double accel = force / getMass();

        if (getVelocity().getY() > 0) {
            return new Vect3(0, -accel, 0);
        } else {
            return new Vect3(0, accel, 0);
        }
        */

        final Vect3 atmosphereVelocity = getEnvironment().getRotationalVelocityVector(getPosition());
        final Vect3 velocity = getVelocity();

        final Vect3 relativeAtmosphericVelocity = new Vect3(velocity);
        relativeAtmosphericVelocity.subtract(atmosphereVelocity);

        final double v = relativeAtmosphericVelocity.getLength();
        double maximumDrag = 0;
        for (final VehiclePart vehiclePart : getCurrentStage().getAllParts()) {
            maximumDrag += vehiclePart.getPart().getMaximumDrag() * vehiclePart.getMass();
        }
        final double density = getEnvironment().getAtmosphere().getDensity(getAltitude());
        final double dragAccel = (DRAG_MULTIPLIER * 0.5 * maximumDrag * density * v * v) / getMass();

        final Vect3 dragVector = relativeAtmosphericVelocity.unitVector(-dragAccel);
        return dragVector;
    }

    public VehiclePart getPart(final String id) {
        return partByIdMap.get(id);
    }

    public void renumberParts(final long highestUid) {
        final List<VehiclePart> parts = getPartsByReverseUidOrder();
        // parts will be sorted from highest UID first, to lowest UID last

        final Map<String, String> newIdByOldIdMap = new HashMap<>();

        long uid = highestUid;
        for (final VehiclePart vehiclePart : parts) {
            final String oldId = vehiclePart.getId();
            vehiclePart.setUidWithoutRemap(uid);
            newIdByOldIdMap.put(oldId, vehiclePart.getId());
            uid -= UID_INCREMENT;
        }

        remap(vehicleGroup, newIdByOldIdMap);

        reorderCfgGroups();
    }

    private List<VehiclePart> getPartsByUidOrder() {
        final List<VehiclePart> parts = new ArrayList<>(partByIdMap.values());
        Collections.sort(parts, new VehicleUidComparator());
        return parts;
    }

    private List<VehiclePart> getPartsByReverseUidOrder() {
        final List<VehiclePart> parts = new ArrayList<>(partByIdMap.values());
        Collections.sort(parts, Collections.reverseOrder(new VehicleUidComparator()));
        return parts;
    }

    private void reorderCfgGroups() {
        final List<VehiclePart> parts = getPartsByReverseUidOrder();
        for (final VehiclePart vehiclePart : parts) {
            vehicleGroup.removeSubgroup(vehiclePart.getCfgGroup());
            vehicleGroup.addSubgroup(vehiclePart.getCfgGroup());
        }
    }

    public void remapId(final String oldId, final String newId) {
        final Map<String, String> newIdByOldIdMap = new HashMap<>();
        newIdByOldIdMap.put(oldId, newId);
        remap(vehicleGroup, newIdByOldIdMap);
        reorderCfgGroups();
    }

    private static void remap(final CfgGroup cfgGroup, final Map<String, String> newIdByOldIdMap) {
        for (final CfgLine line : cfgGroup.getProperties().getLines()) {
            if (line instanceof CfgKeyValueLine) {
                final CfgKeyValueLine keyValueLine = (CfgKeyValueLine) line;
                String value = keyValueLine.getValue();
                for (final Map.Entry<String, String> entry : newIdByOldIdMap.entrySet()) {
                    final String oldId = entry.getKey();
                    final String newId = entry.getValue();
                    value = value.replaceAll(oldId, newId);
                }
                keyValueLine.setValue(value);
            }
        }

        for (final CfgGroup subGroup : cfgGroup.getSubgroups()) {
            remap(subGroup, newIdByOldIdMap);
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

    private Map<Integer, List<VehiclePart>> findDecouplersByStage() {
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
        final Map<Integer, List<VehiclePart>> stageDecouplers = findDecouplersByStage();

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

    private List<VehiclePart> findDecouplers() {
        final List<VehiclePart> decouplers = new ArrayList<>();
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            if (vehiclePart.getPart().isDecoupler()) {
                decouplers.add(vehiclePart);
            }
        }
        return decouplers;
    }

    public void removeParts(final Collection<VehiclePart> vehicleParts) {
        final Set<String> removedIds = new HashSet<>();
        for (final VehiclePart vehiclePart : vehicleParts) {
            vehicleGroup.removeSubgroup(vehiclePart.getCfgGroup());
            final String id = vehiclePart.getId();
            removedIds.add(id);
        }
        partByIdMap.keySet().removeAll(removedIds);

        /*
        for (final Map.Entry<String, VehiclePart> entry : partByIdMap.entrySet()) {
            final VehiclePart vehiclePart = entry.getValue();
            final CfgGroup cfgGroup = vehiclePart.getCfgGroup();

            for (final VehiclePart removedPart : vehicleParts) {
                final CfgProperties properties = cfgGroup.getModifiableProperties();
                final List<CfgLine> lines = new ArrayList<>(properties.getLines());
                for (final CfgLine line : lines) {
                    if (line instanceof CfgKeyValueLine) {
                        final CfgKeyValueLine cfgKeyLine = (CfgKeyValueLine) line;
                        if (((CfgKeyValueLine) line).getValue().contains(removedPart.getId())) {
                            properties.removeLine(line);
                        }
                    }
                }
            }
        }
        */

        // Remove any connections to missing parts

        for (final Map.Entry<String, VehiclePart> entry : partByIdMap.entrySet()) {
            final VehiclePart vehiclePart = entry.getValue();
            final List<String> linkedIds = vehiclePart.getLinkedPartIds();
            for (final String id : linkedIds) {
                if (removedIds.contains(id)) {
                    vehiclePart.removeLinkedPart(id);
                }
            }
        }

    }

    public void repairLinks() {
        removeLinks();
        rebuildLinks();
    }

    protected void removeLinks() {
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final CfgProperties properties = vehiclePart.getCfgGroup().getModifiableProperties();
            final List<CfgKeyValueLine> links = properties.getLines("link");
            for (final CfgKeyValueLine link : links) {
                properties.removeLine(link);
            }
        }
    }

    private void rebuildLinks() {
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            for (final VehiclePart child : vehiclePart.getChildren()) {
                vehiclePart.addLink(child);
            }
        }
    }

    public void shiftStages(final int stageIncrement) {
        for (final VehiclePart vehiclePart : partByIdMap.values()) {
            final CfgProperties properties = vehiclePart.getCfgGroup().getModifiableProperties();

            shiftStage(properties, "sqor", stageIncrement);
            shiftStage(properties, "istg", stageIncrement);
            shiftStage(properties, "dstg", stageIncrement);
        }
    }

    private void shiftStage(final CfgProperties properties, final String key, final int stageIncrement) {
        final CfgKeyValueLine line = properties.getLine(key);
        final int stage = Integer.parseInt(line.getValue());
        if (stage >= 0) {
            line.setValue(Integer.toString(stage + stageIncrement));
        }
    }

}
