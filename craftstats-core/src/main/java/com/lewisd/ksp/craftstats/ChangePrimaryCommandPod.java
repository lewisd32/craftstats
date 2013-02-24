package com.lewisd.ksp.craftstats;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.util.AbstractVehicleMain;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;
import com.lewisd.ksp.craftstats.vehicle.VehiclePart;

public class ChangePrimaryCommandPod extends AbstractVehicleMain {

    public static void main(final String[] args) throws IOException {
        new ChangePrimaryCommandPod(args).run();
    }

    private final String id;
    private final boolean verbose;

    public ChangePrimaryCommandPod(final KerbalContext kerbalContext, final String id, final boolean verbose) {
        super(kerbalContext);
        this.id = id;
        this.verbose = verbose;
    }

    public ChangePrimaryCommandPod(final String[] args) {
        super(args);

        id = cmdLine.getOptionValue("id");
        verbose = cmdLine.hasOption("verbose");
    }

    @Override
    protected void addMoreOptions(final Options options) {
        options.addOption("i", "id", true, "ID of command pod to set as primary. Required.");
        options.addOption("v", "verbose", false, "Verbose output");
    }

    @Override
    protected void checkMoreRequired(final CommandLine cmdLine) {
        require(cmdLine, "id");
    }

    @Override
    protected void run(Vehicle vehicle) throws IOException {
        final VehiclePart newCommandPod = vehicle.getPart(id);

        if (newCommandPod == null) {
            System.err.println("No part found for id: " + id);
            System.exit(2);
        }

        if (newCommandPod.getUid() == vehicle.getHighestUid()) {
            System.out.println("Already primary command pod.");
            System.exit(0);
        }

        final Parts parts = kerbalContext.getParts();
        final Resources resources = kerbalContext.getResources();


        final long newUid = vehicle.getHighestUid() + Vehicle.UID_INCREMENT;

        if (verbose) {
            System.out.println("Using new UID: " + newUid);
        }

        newCommandPod.setUid(newUid);

        // vehicle.rootPart needs to be reset, so just rebuild the vehicle from the cfg
        vehicle = vehicle.cloneFromCfg(parts, resources);

        vehicle.repairLinks();

        final String vehiclePath = cmdLine.getOptionValue("craft");
        System.out.println("Saving to " + vehiclePath);
        kerbalContext.getVehicleLoader().saveVehicle(vehicle, new File(vehiclePath));
    }

}
