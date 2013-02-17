package com.lewisd.ksp.craftstats;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.util.AbstractVehicleEnvironmentMain;
import com.lewisd.ksp.craftstats.vehicle.Engine;
import com.lewisd.ksp.craftstats.vehicle.Stage;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;
import com.lewisd.ksp.craftstats.vehicle.VehiclePart;

public class VehicleStats extends AbstractVehicleEnvironmentMain {

    public static void main(final String[] args) throws IOException {
        new VehicleStats(args).run();
    }

    private final boolean verbose;
    private final boolean showStages;

    public VehicleStats(final KerbalContext kerbalContext, final boolean verbose, final boolean showStages) {
        super(kerbalContext);
        this.verbose = verbose;
        this.showStages = showStages;
    }

    public VehicleStats(final String[] args) {
        super(args);

        verbose = cmdLine.hasOption("verbose");
        showStages = cmdLine.hasOption("stages");
    }

    @Override
    protected void addMoreOptions(final Options options) {
        options.addOption("s", "stages", false, "Show information about individual stages.");
        options.addOption("v", "verbose", false, "Verbose output");
    }

    @Override
    protected void checkMoreRequired(final CommandLine cmdLine) {
    }

    @Override
    public void run(final Vehicle vehicle) throws IOException {
        printVehicleStats(vehicle, System.out);

    }

    private void printVehicleStats(final Vehicle vehicle, final PrintStream out) {

        if (showStages) {
            for (final Stage stage : vehicle.getStagesFromTop()) {
                final int stageNumber = stage.getNumber();
                out.println();
                out.println("Stage " + stageNumber + ":");
                printStageStats(out, stage);
            }
        } else {
            out.println("Bottom stage:");
            printStageStats(out, vehicle.getStagesFromBottom().get(0));
        }
        out.println();

        double totalDeltaV = 0;
        for (final Stage stage : vehicle.getStagesFromTop()) {
            totalDeltaV += stage.getDeltaV();
        }
        out.printf("Total delta V = %.1f m/s%n", totalDeltaV);
    }

    private void printStageStats(final PrintStream out, final Stage stage) {
        final int stageNumber = stage.getNumber();
        final Environment environment = stage.getEnvironment();

        System.out.printf("  %s @ %.3fkm%n", environment.getName(), stage.getAltitude());

        final Collection<Engine> engines = stage.getActiveEngines();
        if (stageNumber != 0) {
            if (engines.isEmpty()) {
                out.println("  No active engines");
                return;
            }

            if (stage.getMass() == stage.getDryMass()) {
                return;
            }

            if (!stage.launchClampsReleased()) {
                return;
            }
        }

        out.println("  Active engines:");
        for (final Engine engine : engines) {
            final VehiclePart vehiclePart = engine.getVehiclePart();
            final double mass = vehiclePart.getMass();
            final double maxThrust = engine.getMaxThrust();
            final double atmoIsp = engine.getAtmoIsp();
            final double vacuumIsp = engine.getVacuumIsp();
            out.printf("    %s", vehiclePart.getPart().getTitle());
            if (verbose) {
                out.printf(" (%s) : %.3f t (maxThrust=%.1f, ispa=%.1f, ispv=%.1f)", vehiclePart.getId(), mass, maxThrust, atmoIsp, vacuumIsp);
            }
            out.println();
        }

        final double g = environment.getGravity(stage.getAltitude());

        final double totalStageDryMass = stage.getDryMass();
        final double totalStageMass = stage.getMass();
        final double totalStageThrust = stage.getMaxThrust();
        final double totalStageIsp = stage.getIsp();
        final double burnedFuelMass = stage.getLOXFuelMass() + stage.getSolidFuelMass();

        final double twWet = totalStageThrust / (g * totalStageMass);
        final double twDry = totalStageThrust / (g * totalStageDryMass);

        out.printf("  Mass: %.3f t (%.3f t dry)%n", totalStageMass, totalStageDryMass);
        out.printf("  Max thrust = %.1f kN%n", totalStageThrust);
        out.printf("  Isp = %.1f m/s%n", totalStageIsp);
        out.printf("  Delta V = %.1f m/s%n", stage.getDeltaV());
        out.printf("  T/W wet = %.2f%n", twWet);
        out.printf("  T/W dry = %.2f%n", twDry);
        out.printf("  Burned fuel = %.1f t%n", burnedFuelMass);

        final double fuelFlowRate = (totalStageThrust / 9.81) / totalStageIsp;
        out.printf("  Fuel flow rate = %.3f t/s%n", fuelFlowRate);

        final double burnDuration = burnedFuelMass / fuelFlowRate;
        out.printf("  100%% thrust burn duration = %.1f s%n", burnDuration);

        final double accelWet = totalStageThrust / totalStageMass;
        final double accelDry = totalStageThrust / totalStageDryMass;
        out.printf("  Acceleration         = %.1f m/s/s (%.1f m/s/s dry)%n", accelWet, accelDry);
        out.printf("  Acceleration after G = %.1f m/s/s (%.1f m/s/s dry)%n", (accelWet - g), (accelDry - g));
        out.printf("  G-force              = %.1f G's (%.1f G's dry)%n", (accelWet - g) / 9.81, (accelDry - g) / 9.81);
    }

}
