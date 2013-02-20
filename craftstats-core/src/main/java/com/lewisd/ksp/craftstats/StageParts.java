package com.lewisd.ksp.craftstats;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.util.AbstractVehicleMain;
import com.lewisd.ksp.craftstats.vehicle.Stage;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;
import com.lewisd.ksp.craftstats.vehicle.VehiclePart;

public class StageParts extends AbstractVehicleMain {

    public static void main(final String[] args) throws IOException {
        new StageParts(args).run();
    }

    private final boolean verbose;
    private final boolean showAllParts;

    public StageParts(final KerbalContext kerbalContext, final boolean verbose, final boolean showAllParts) {
        super(kerbalContext);
        this.verbose = verbose;
        this.showAllParts = showAllParts;
    }

    public StageParts(final String[] args) {
        super(args);

        verbose = cmdLine.hasOption("verbose");
        showAllParts = cmdLine.hasOption("all");
    }

    @Override
    protected void addMoreOptions(final Options options) {
        options.addOption("v", "verbose", false, "Verbose output");
        options.addOption("a", "all", false, "List all parts, not just major ones.");
    }

    @Override
    protected void checkMoreRequired(final CommandLine cmdLine) {
    }

    @Override
    public void run(final Vehicle vehicle) throws IOException {
        printStages(vehicle, System.out);

    }

    private void printStages(final Vehicle vehicle, final PrintStream out) {
        for (final Stage stage : vehicle.getStagesFromTop()) {
            printStageStats(out, stage);
        }
    }

    private void printStageStats(final PrintStream out, final Stage stage) {
        final int stageNumber = stage.getNumber();
        out.println("Stage " + stageNumber + ":");
        for (final VehiclePart vehiclePart : stage.getAllParts()) {
            if (shouldShow(vehiclePart)) {
                System.out.print("  = " + vehiclePart.getPart().getTitle());
                if (verbose) {
                    out.print(" (" + vehiclePart.getId() + ")");
                }
                if (stage.getEngineIfActive(vehiclePart) != null) {
                    System.out.print(" (active)");
                }
                System.out.println();
            }
        }

        if (stage.getDroppedParts().isEmpty()) {
            System.out.println("No parts dropped");
        } else {
            System.out.println("Dropped:");
            for (final VehiclePart vehiclePart : stage.getDroppedParts()) {
                if (shouldShow(vehiclePart)) {
                    System.out.print("  - " + vehiclePart.getPart().getTitle());
                    if (verbose) {
                        out.print(" (" + vehiclePart.getId() + ")");
                    }
                    System.out.println();
                }
            }
        }
        System.out.println();
    }

    private boolean shouldShow(final VehiclePart vehiclePart) {
        if (vehiclePart.getQueuedStage() >= 0 || vehiclePart.getLOXFuelMass() > 0 || vehiclePart.getPart().isPod()) {
            return true;
        }
        return showAllParts;
    }

}
