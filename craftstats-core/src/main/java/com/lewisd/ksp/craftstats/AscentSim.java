package com.lewisd.ksp.craftstats;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.ascent.AscentEventListener;
import com.lewisd.ksp.craftstats.ascent.AscentSimulator;
import com.lewisd.ksp.craftstats.ascent.PrintingAscentEventListener;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.util.AbstractVehicleEnvironmentMain;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;

public class AscentSim extends AbstractVehicleEnvironmentMain {

    public static void main(final String[] args) throws IOException {
        new AscentSim(args).run();
    }

    private final boolean verbose;
    private final double step;
    private final SortedMap<Double, Double> throttlePercentByMet = new TreeMap<>();
    private final SortedMap<Integer, Double> throttlePercentByStage = new TreeMap<>();

    public AscentSim(final KerbalContext kerbalContext, final boolean verbose, final double step) {
        super(kerbalContext);
        this.verbose = verbose;
        this.step = step;
    }

    public AscentSim(final String[] args) {
        super(args);

        verbose = cmdLine.hasOption("verbose");
        step = Double.parseDouble(cmdLine.getOptionValue("step", "1"));

        if (cmdLine.hasOption("throttle")) {
            for (final String throttleCmd : cmdLine.getOptionValues("throttle")) {
                final String[] parts = throttleCmd.split("=");
                final double throttle = Double.parseDouble(parts[1]);
                if (parts[0].matches("[0-9:]*")) {
                    final double met = parseMet(parts[0]);
                    // System.out.println("Throttle to " + throttle + "% at " + met);
                    throttlePercentByMet.put(met, throttle);
                } else if (parts[0].toUpperCase().startsWith("S")) {
                    final int stage = Integer.parseInt(parts[0].substring(1));
                    // System.out.println("Throttle to " + throttle + "% at stage " + stage);
                    throttlePercentByStage.put(stage, throttle);
                } else {
                    throw new IllegalArgumentException("Unknown throttle param: " + throttleCmd);
                }
            }
        }
    }

    private double parseMet(final String metStr) {
        double met = 0;
        final String[] parts = metStr.split(":");
        final double[] multipliers = new double[] { 1, 60, 60 * 60, 60 * 60 * 24 };
        int x = 0;
        for (int i = parts.length - 1; i >= 0; --i) {
            final String part = parts[i];
            final double value = Double.parseDouble(part);
            final double multiplier = multipliers[x];
            met += value * multiplier;
            ++x;
        }
        return met;
    }

    @Override
    protected void addMoreOptions(final Options options) {
        options.addOption("s", "step", true, "Step size, in seconds. (Default: 1)");
        options.addOption("t", "throttle", true, "Set throttle percentage at a specified MET or stage. eg. -t 2:15=75 or -t S2=100");
        options.addOption("v", "verbose", false, "Verbose output");
    }

    @Override
    protected void checkMoreRequired(final CommandLine cmdLine) {
    }

    @Override
    public void run(final Vehicle vehicle) throws IOException {

        final AscentEventListener listener = new PrintingAscentEventListener(System.out);
        final AscentSimulator simulator = new AscentSimulator(vehicle, step, listener, throttlePercentByMet, throttlePercentByStage);

        simulator.printAscentState(System.out, verbose);

        final double requestedAltitude = 1000000;
        final int printInterval = 1;
        final double maxMet = 1000;

        double extraTime = 5;

        int printCounter = 0;
        while (true) {

            simulator.runStep();

            if (++printCounter == printInterval) {
                printCounter = 0;
                simulator.printAscentState(System.out, verbose);
            }

            if (simulator.getMet() >= maxMet) {
                break;
            }

            if (vehicle.getAltitude() < 0) {
                System.out.println("You will not go to space today.");
                if (extraTime-- <= 0) {
                    break;
                }
            }
            if (vehicle.getAltitude() >= requestedAltitude) {
                System.out.println("Requested altitude reached.");
                if (extraTime-- <= 0) {
                    break;
                }
            }
            if (vehicle.getCurrentStage().getNumber() == 0) {
                System.out.println("Final stage.");
                if (extraTime-- <= 0) {
                    break;
                }
            }
        }

        if (printCounter > 0) {
            simulator.printAscentState(System.out, verbose);
        }

    }

}
