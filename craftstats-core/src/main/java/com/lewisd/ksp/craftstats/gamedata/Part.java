package com.lewisd.ksp.craftstats.gamedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.vehicle.PartResource;

public class Part {

    private static final Set<String> SOLID_FUELS = Sets.newHashSet("SolidFuel");
    private static final Set<String> LOX_FUELS = Sets.newHashSet("LiquidFuel", "Oxidizer");

    private final CfgProperties properties = new CfgProperties();
    private final Map<String, PartResource> resourcesByName = new HashMap<>();
    private final Map<String, CfgGroup> modulesByName = new HashMap<>();
    private final PartModule module;

    public Part(final PartModule module, final CfgGroup cfgGroup, final Resources resources) {
        this.module = module;
        properties.putAll(module.getProperties());
        properties.putAll(cfgGroup.getProperties());

        for (final CfgGroup resourceGroup : cfgGroup.getSubgroup("RESOURCE")) {
            final String resourceName = resourceGroup.getProperties().get("name");
            final double amount = resourceGroup.getProperties().getDouble("amount");
            final double maxAmount = resourceGroup.getProperties().getDouble("maxAmount");
            final Resource resource = resources.getResource(resourceName);
            final PartResource partResource = new PartResource(resource, amount, maxAmount);
            this.resourcesByName.put(resourceName, partResource);
        }

        for (final CfgGroup moduleGroup : cfgGroup.getSubgroup("MODULE")) {
            final String moduleName = moduleGroup.getProperties().get("name");
            modulesByName.put(moduleName, moduleGroup);
        }
    }

    public String getName() {
        return properties.get("name").replace('_', '.');
    }

    public CfgProperties getProperties() {
        return properties.unmodifiable();
    }

    public Collection<PartResource> getResources() {
        return resourcesByName.values();
    }

    public CfgGroup getModule(final String name) {
        return modulesByName.get(name);
    }

    public String getTitle() {
        return getProperties().get("title");
    }

    public double getDryMass() {
        return getMaxMass() - getMaxSolidFuelMass() - getMaxLOXFuelMass();
    }

    public double getMaxSolidFuelMass() {
        return getFuelMass(SOLID_FUELS);
    }

    public double getMaxLOXFuelMass() {
        return getFuelMass(LOX_FUELS);
    }

    public double getMaxMass() {
        double mass = getProperties().getDouble("mass");

        for (final PartResource partResource : getResources()) {
            final double amount = partResource.getAmount();
            final double density = partResource.getResource().getDensity();

            mass += amount * density;
        }

        return mass;
    }

    public boolean getFuelCrossFeed() {
        return getProperties().get("fuelCrossFeed").equalsIgnoreCase("true");
    }

    public boolean isEngine() {
        return getModule("ModuleEngines") != null;
    }

    public boolean isDecoupler() {
        return getModule("ModuleDecouple") != null || getModule("ModuleAnchoredDecoupler") != null;
    }

    public boolean isLaunchClamp() {
        return getModule("LaunchClamp") != null;
    }

    public boolean isPod() {
        return module.getName().equals("CommandPod");
    }

    public CfgGroup getEngineModule() {
        return getModule("ModuleEngines");
    }

    private double getFuelMass(final Set<String> fuelTypes) {
        double fuelMass = 0;
        for (final PartResource partResource : getResources()) {
            if (fuelTypes.contains(partResource.getResource().getName())) {
                final double amount = partResource.getAmount();
                final double density = partResource.getResource().getDensity();

                fuelMass += amount * density;
            }
        }
        return fuelMass;
    }

}
