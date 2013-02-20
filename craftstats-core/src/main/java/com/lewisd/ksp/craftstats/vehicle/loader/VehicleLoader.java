package com.lewisd.ksp.craftstats.vehicle.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.cfg.CfgWriter;
import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;
import com.lewisd.ksp.craftstats.vehicle.VehiclePart;

public class VehicleLoader {

    private final CfgReader cfgReader;
    private final CfgWriter cfgWriter;
    private final Parts parts;
    private final Resources resources;

    public VehicleLoader(final CfgReader cfgReader, final CfgWriter cfgWriter, final Parts parts, final Resources resources) {
        this.cfgReader = cfgReader;
        this.cfgWriter = cfgWriter;
        this.parts = parts;
        this.resources = resources;
    }

    public Vehicle loadVehicle(final File file) throws IOException {
        try (final InputStream in = new FileInputStream(file)) {
            return loadVehicle(in);
        }
    }

    public Vehicle loadVehicle(final InputStream in) throws IOException {

        final CfgGroup root = cfgReader.readCfg(in);

        final List<VehiclePart> vehicleParts = new ArrayList<>();
        final List<CfgGroup> partGroups = root.getSubgroup("PART");
        for (final CfgGroup partGroup : partGroups) {
            final CfgProperties cfgProperties = partGroup.getProperties();
            final String id = cfgProperties.get("part");
            final String partName = id.substring(0, id.indexOf('_'));
            final Part part = parts.getPart(partName);

            final VehiclePart vehiclePart = new VehiclePart(part, partGroup, resources);
            vehicleParts.add(vehiclePart);
        }

        final Vehicle vehicle = new Vehicle(root, vehicleParts, resources);

        for (final VehiclePart vehiclePart : vehicleParts) {
            vehiclePart.setVehicle(vehicle);
        }

        return vehicle;
    }

    public void saveVehicle(final Vehicle vehicle, final File file) throws IOException {
        final CfgGroup cfgGroup = vehicle.getCfgGroup();
        cfgWriter.writeCfg(cfgGroup, file);
    }

}
