package com.lewisd.ksp.craftstats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.cfg.CfgKeyValueLine;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.util.AbstractMain;
import com.lewisd.ksp.craftstats.util.Vect3;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;
import com.lewisd.ksp.craftstats.vehicle.VehiclePart;
import com.lewisd.ksp.craftstats.vehicle.loader.VehicleLoader;

public class MergeCraft extends AbstractMain {

    public static void main(final String[] args) throws IOException {
        new MergeCraft(args).run();
    }

    private final boolean verbose;

    public MergeCraft(final KerbalContext kerbalContext, final List<Vehicle> vehicles, final boolean verbose) {
        super(kerbalContext);

        this.verbose = verbose;
    }

    public MergeCraft(final String[] args) {
        super(args);

        verbose = cmdLine.hasOption("verbose");
    }

    @Override
    protected void addOptions(final Options options) {
        options.addOption("c", "craft", true,
                          "Path to a vehicle .craft file to merge. Must be specified multiple times. First craft will be on top of the resulting rocket.");
        options.addOption("d", "destination", true, "Path to the write the merged vehicle .craft to. Required.");
        options.addOption("v", "verbose", false, "Verbose output");
    }

    @Override
    protected void checkRequired(final CommandLine cmdLine) {
        require(cmdLine, "craft");
    }

    public void run() throws IOException {
        final VehicleLoader vehicleLoader = kerbalContext.getVehicleLoader();
        final List<Vehicle> vehicles = new ArrayList<>();

        final String craftPaths[] = cmdLine.getOptionValues("craft");
        for (final String craftPath : craftPaths) {
            vehicles.add(vehicleLoader.loadVehicle(new File(craftPath)));
        }

        final String outputPath = cmdLine.getOptionValue("destination");
        final File outputFile = new File(outputPath);
        final String outputFilename = outputFile.getName();
        final String vehicleName = outputFilename.substring(0, outputFilename.lastIndexOf('.'));

        final Vehicle merged = mergeVehicles(vehicleName, vehicles);
        vehicleLoader.saveVehicle(merged, outputFile);
    }

    public Vehicle mergeVehicles(final String mergedName, final List<Vehicle> vehicleList) throws IOException {
        final Parts parts = kerbalContext.getParts();
        final Resources resources = kerbalContext.getResources();

        final Deque<Vehicle> vehicles = new ArrayDeque<>(vehicleList);

        Vehicle vehicle = vehicles.removeLast();

        System.out.println("Starting with " + vehicle.getName() + " on top");
        System.out.println();

        vehicle.setName(mergedName);
        // Set a nice high UID so that there's plenty of room below it for renumbering parts
        vehicle.renumberParts(Vehicle.HIGH_UID);

        while (!vehicles.isEmpty()) {
            final Vehicle bottom = vehicles.removeLast();
            System.out.println("Adding " + bottom.getName() + " to the bottom");

            // Renumber UIDs to avoid overlap
            bottom.renumberParts(vehicle.getLowestUid() - Vehicle.UID_INCREMENT);

            final VehiclePart lowestDecoupler = vehicle.getLowestDecoupler();

            final VehiclePart highestDecoupler = bottom.getHighestDecoupler();
            final VehiclePart partAbove = highestDecoupler.getPartAbove();
            final VehiclePart partBelow = highestDecoupler.getPartBelow();

            if (verbose) {
                System.out.println(vehicle.getName() + " : Lowest decoupler = " + lowestDecoupler);

                System.out.println(bottom.getName() + " : Part above highest decoupler: " + partAbove);
                System.out.println(bottom.getName() + " : Highest decoupler = " + highestDecoupler);
                System.out.println(bottom.getName() + " : Part below highest decoupler: " + partBelow);
            }


            trimParts(bottom, highestDecoupler, partAbove, partBelow);

            repositionVehicle(vehicle, bottom, lowestDecoupler, partBelow);

            final Vehicle newBottom = bottom.cloneFromCfg(parts, resources);
            vehicle = vehicle.cloneFromCfg(parts, resources);

            // Shift stages

            shiftStages(vehicle, newBottom);

            vehicle.addParts(newBottom.getAllParts());

            attachParts(vehicle.getPart(lowestDecoupler.getId()), vehicle.getPart(partBelow.getId()));

            System.out.println();
        }

        vehicle.repairLinks();

        return vehicle;
    }

    protected void shiftStages(final Vehicle vehicle, final Vehicle newBottom) {

        final int vehicleFirstStage = vehicle.getStagesFromBottom().get(0).getNumber();
        final int vehicleLastStage = Math.max(vehicle.getStagesFromTop().get(0).getNumber(), vehicleFirstStage + 1);

        final int bottomLastStage = newBottom.getStagesFromTop().get(0).getNumber();

        final int stageShift = vehicleFirstStage + 1 - bottomLastStage;

        if (verbose) {
            System.out.println(vehicle.getName() + " : first stage = " + vehicleFirstStage);
            System.out.println(vehicle.getName() + " : last stage = " + vehicleLastStage);
            System.out.println(newBottom.getName() + " : Shifting stages by " + stageShift);
        }

        newBottom.shiftStages(stageShift);
    }

    protected void repositionVehicle(final Vehicle vehicle, final Vehicle bottom, final VehiclePart lowestDecoupler, final VehiclePart partBelow) {
        // Reposition vehicle over bottom

        final Vect3 lowestNode = lowestDecoupler.getLowestStackNode();
        final Vect3 highestNode = partBelow.getHighestStackNode();
        final Vect3 vehicleOffset = new Vect3(highestNode);
        vehicleOffset.subtract(lowestNode);

        if (verbose) {
            System.out.println(vehicle.getName() + " : Lowest node of " + lowestDecoupler + " = " + lowestNode);
            System.out.println(bottom.getName() + " : Highest node of " + partBelow + " = " + highestNode);

            System.out.println("offset = " + vehicleOffset);
            System.out.println("Repositioning " + vehicle.getName());
        }

        vehicle.reposition(vehicleOffset);
    }

    protected void trimParts(final Vehicle bottom, final VehiclePart highestDecoupler, final VehiclePart partAbove, final VehiclePart partBelow) {
        if (partAbove != null) {
            final Collection<VehiclePart> removeParts;
            if (highestDecoupler.getParent() == partAbove) {
                if (verbose) {
                    System.out.println("Removing ancestors");
                }
                removeParts = highestDecoupler.getAncestorsWithDescendants();
            } else {
                if (verbose) {
                    System.out.println("Removing descendants");
                }
                removeParts = highestDecoupler.getDescendants();
            }
            removeParts.add(highestDecoupler);

            for (final VehiclePart child : highestDecoupler.getChildren()) {
                if (child != partBelow) {
                    removeParts.add(child);
                    removeParts.addAll(child.getDescendants());
                }
            }

            if (verbose) {
                System.out.println(bottom.getName() + " : Removing parts: " + removeParts);
            }
            bottom.removeParts(removeParts);
        }
    }

    protected void attachParts(final VehiclePart lowestDecoupler, final VehiclePart partBelow) {
        final CfgProperties decouplerProperties = lowestDecoupler.getCfgGroup().getModifiableProperties();
        final CfgProperties partBelowProperties = partBelow.getCfgGroup().getModifiableProperties();

        final List<String> partBelowAttachmentLines = partBelowProperties.getOptionalList("attN");
        final List<String> decouplerAttachmentLines = decouplerProperties.getOptionalList("attN");

        // TODO: need to figure out whether to do this, or the reverse.
        // Will depend on the way the lowestDecoupler and partBelow are oriented.
        lowestDecoupler.addChild(partBelow);
        decouplerProperties.addLine(new CfgKeyValueLine("\t", "attN", "bottom," + partBelow.getId()));
        partBelowProperties.addLine(new CfgKeyValueLine("\t", "attN", "top," + lowestDecoupler.getId()));
    }

}
