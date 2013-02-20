package com.lewisd.ksp.craftstats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

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

    public MergeCraft(final KerbalContext kerbalContext, final List<Vehicle> vehicles) {
        super(kerbalContext);
    }

    public MergeCraft(final String[] args) {
        super(args);
    }

    @Override
    protected void addOptions(final Options options) {
        options.addOption("c", "craft", true, "Path to a vehicle .craft files to merge. Required.");
        options.addOption("d", "destination", true, "Path to the write the merged vehicle .craft to. Must be specified multiple times.");
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

        final Vehicle merged = mergeVehicles(vehicles);

        final String outputPath = cmdLine.getOptionValue("destination");
        final File outputFile = new File(outputPath);
        final String outputFilename = outputFile.getName();
        final String vehicleName = outputFilename.substring(0, outputFilename.lastIndexOf('.'));
        merged.setName(vehicleName);
        vehicleLoader.saveVehicle(merged, outputFile);
    }

    public Vehicle mergeVehicles(final List<Vehicle> vehicleList) {

        final Deque<Vehicle> vehicles = new ArrayDeque<>(vehicleList);

        final Vehicle vehicle = vehicles.removeLast();

        while (!vehicles.isEmpty()) {
            final Vehicle bottom = vehicles.removeLast();

            final VehiclePart lowestDecoupler = vehicle.getLowestDecoupler();
            final VehiclePart highestDecoupler = bottom.getHighestDecoupler();

            System.out.println(vehicle.getName() + " : Lowest decoupler = " + lowestDecoupler);
            System.out.println(bottom.getName() + " : Highest decoupler = " + highestDecoupler);

            final Vect3 lowestNode = lowestDecoupler.getLowestStackNode();
            final Vect3 highestNode = highestDecoupler.getHighestStackNode();

            System.out.println(vehicle.getName() + " : Lowest node = " + lowestNode);
            System.out.println(bottom.getName() + " : Highest node = " + highestNode);

            final Vect3 vehicleOffset = new Vect3(highestNode);
            vehicleOffset.subtract(lowestNode);

            System.out.println("offset = " + vehicleOffset);
            vehicle.reposition(vehicleOffset);

            final VehiclePart bottomJoinPart = highestDecoupler.getPartBelow();

            /*
            if (highestDecoupler.getParent() == bottomJoinPart) {
                // remove the descendants of highestDecoupler from the bottom vehicle.
                bottom.removeParts(bottomJoinPart.getDescendants());
            } else {
                // remove the ancestors of highestDecoupler from the bottom vehicle.
                bottom.removeParts(bottomJoinPart.getAncestorsWithDescentants());
            }
            
            */

            vehicle.addParts(bottom.getAllParts());
        }

        return vehicle;
    }

}
