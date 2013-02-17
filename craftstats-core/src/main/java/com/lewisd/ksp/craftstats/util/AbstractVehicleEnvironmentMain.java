package com.lewisd.ksp.craftstats.util;

import java.io.IOException;

import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;

public abstract class AbstractVehicleEnvironmentMain extends AbstractVehicleMain {

    public AbstractVehicleEnvironmentMain(final KerbalContext kerbalContext) {
        super(kerbalContext);
    }

    public AbstractVehicleEnvironmentMain(final String[] args) {
        super(args);
    }

    @Override
    public Vehicle setupVehicle() throws IOException {
        final Vehicle vehicle = super.setupVehicle();

        final String[] envStrs = cmdLine.getOptionValues("environment");

        Double initialAltitude = null;
        if (envStrs == null || envStrs.length == 0) {
            vehicle.setEnvironment(Environment.KERBIN);
        } else {
            for (final String envStr : envStrs) {
                final String[] parts = envStr.split(":");
                if (parts.length == 1) {
                    final Environment environment = Environment.getNamed(parts[0]);
                    vehicle.setEnvironment(environment);
                } else if (parts.length == 2) {
                    if (parts[0].matches("[0-9]*")) {
                        final int stage = Integer.parseInt(parts[0]);
                        for (int s = stage; s >= 0; s--) {
                            final Environment environment = Environment.getNamed(parts[1]);
                            vehicle.getStage(s).setEnvironment(environment);
                        }
                    } else if (parts[1].matches("[0-9]*")) {
                        final Environment environment = Environment.getNamed(parts[0]);
                        vehicle.setEnvironment(environment);
                        final double altitude = Double.parseDouble(parts[1]);
                        initialAltitude = altitude;
                    }
                } else if (parts.length == 3) {
                    final int stage = Integer.parseInt(parts[0]);
                    final Environment environment = Environment.getNamed(parts[1]);
                    final double altitude = Double.parseDouble(parts[2]);
                    for (int s = stage; s >= 0; s--) {
                        vehicle.getStage(s).setEnvironment(environment);
                        vehicle.getStage(s).setAltitude(altitude);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown value for environment parameter: " + envStr);
                }
            }
        }

        vehicle.initSimulation();

        if (initialAltitude != null) {
            vehicle.setAltitude(initialAltitude);
        }

        return vehicle;
    }

    @Override
    protected void addOptions(final Options options) {
        super.addOptions(options);
        options.addOption("e", "environment", true,
                          "Environment for vehicle or stage to operate in. Can be specified multiple times. eg. -e Kerbin -e 3:Kerbin:20 -e 2:Kerbin:70 -e 1:Mun:0");
        addMoreOptions(options);
    }

}
