package com.lewisd.ksp.craftstats.vehicle;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.PartTitleComparator;
import com.lewisd.ksp.craftstats.gamedata.Resource;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.util.Vect3;



public class VehiclePart {

    private final Resources resources;
    private final Part part;
    private final CfgGroup partGroup;
    private final Set<VehiclePart> children = new HashSet<>();
    private final Map<Resource, Double> resourceQuantities = new HashMap<>();

    private VehiclePart parent;
    private Vehicle vehicle;
    private String id;

    public VehiclePart(final String id, final Part part, final CfgGroup partGroup, final Resources resources) {
        this.id = id;
        this.part = part;
        this.partGroup = partGroup;
        this.resources = resources;

        for (final PartResource partResource : part.getResources()) {
            resourceQuantities.put(partResource.getResource(), partResource.getAmount());
        }
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(final Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getId() {
        return id;
    }

    public long getUid() {
        return Long.parseLong(id.substring(id.indexOf('_') + 1));
    }

    public void setUid(final long uid) {
        id = id.substring(0, id.indexOf('_') + 1) + uid;
    }

    public Part getPart() {
        return part;
    }

    public int getDetachStage() {
        return Integer.parseInt(partGroup.getProperties().get("dstg"));
    }

    public int getActivateStage() {
        return Integer.parseInt(partGroup.getProperties().get("istg"));
    }

    public int getQueuedStage() {
        return Integer.parseInt(partGroup.getProperties().get("sqor"));
    }

    public int getStageIndex() {
        return Integer.parseInt(partGroup.getProperties().get("sidx"));
    }

    public Vect3 getPosition() {
        return Vect3.parse(partGroup.getProperties().get("pos"));
    }

    public void setPosition(final Vect3 position) {
        final String positionString = String.format("%.5f, %.5f, %.5f", position.getX(), position.getY(), position.getZ());
        partGroup.getProperties().getLine("pos").setValue(positionString);
    }

    public double getResourceMass(final Resource resource) {
        if (resourceQuantities.get(resource) != null) {
            return resourceQuantities.get(resource) * resource.getDensity();
        }
        return 0;
    }

    public double getResourceMass(final Set<Resource> resources) {
        double mass = 0;
        for (final Resource resource : resources) {
            if (resourceQuantities.get(resource) != null) {
                mass += resourceQuantities.get(resource) * resource.getDensity();
            }
        }
        return mass;
    }

    public void setResourceMass(final Resource resource, final double resourceMass) {
        resourceQuantities.put(resource, resourceMass / resource.getDensity());
    }

    public double getSolidFuelMass() {
        return getResourceMass(resources.getSolidFuels());
    }

    public double getLOXFuelMass() {
        return getResourceMass(resources.getLOXFuels());
    }

    private double getMaxSolidFuelMass() {
        return part.getMaxSolidFuelMass();
    }

    private double getMaxLOXFuelMass() {
        return part.getMaxLOXFuelMass();
    }

    public double getDryMass() {
        return part.getMaxMass() - getMaxLOXFuelMass() - getMaxSolidFuelMass();
    }

    public double getMaxMass() {
        if (part.isLaunchClamp()) {
            return 0;
        }
        return part.getMaxMass();
    }

    public double getMass() {
        if (part.isLaunchClamp()) {
            return 0;
        }
        return part.getDryMass() + getLOXFuelMass() + getSolidFuelMass();
    }

    public Set<String> getAllLinkedPartNames() {
        final Set<String> names = new HashSet<>();

        final List<String> links = partGroup.getProperties().getOptionalList("link");
        if (links != null) {
            names.addAll(links);
        }

        final List<String> attNs = partGroup.getProperties().getOptionalList("attN");
        if (attNs != null) {
            for (final String value : attNs) {
                final String name = value.substring(value.indexOf(',') + 1).trim();
                names.add(name);
            }
        }

        final List<String> srfNs = partGroup.getProperties().getOptionalList("srfN");
        if (srfNs != null) {
            for (final String value : srfNs) {
                final String name = value.substring(value.indexOf(',') + 1).trim();
                names.add(name);
            }
        }

        return names;
    }

    private void setParent(final VehiclePart parent) {
        this.parent = parent;
    }

    public void addChild(final VehiclePart linkedPart) {
        children.add(linkedPart);
        linkedPart.setParent(this);
    }

    public VehiclePart getParent() {
        return parent;
    }

    public Set<VehiclePart> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public SortedSet<VehiclePart> getSortedChildren() {
        final SortedSet<VehiclePart> sortedChildren = new TreeSet<VehiclePart>(new VehiclePartPartComparator(new PartTitleComparator()));
        sortedChildren.addAll(children);
        return sortedChildren;
    }

    public Set<VehiclePart> getDescendants() {
        final Set<VehiclePart> descendants = new HashSet<>();

        descendants.addAll(children);

        for (final VehiclePart child : children) {
            descendants.addAll(child.getDescendants());
        }

        return descendants;
    }

    @Override
    public String toString() {
        return part.getTitle() + "(" + id + ")";
    }

}
