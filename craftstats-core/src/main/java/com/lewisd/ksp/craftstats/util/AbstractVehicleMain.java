package com.lewisd.ksp.craftstats.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.lewisd.ksp.craftstats.gamedata.Environment;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;

public abstract class AbstractVehicleMain extends AbstractMain {

    public AbstractVehicleMain(final KerbalContext kerbalContext) {
        super(kerbalContext);
    }

    public AbstractVehicleMain(final String[] args) {
        super(args);
    }

    public void run() throws IOException {
        final Vehicle vehicle = setupVehicle();

        run(vehicle);
    }

    protected Vehicle setupVehicle() throws IOException {
        final String vehiclePath = cmdLine.getOptionValue("craft");
        final Vehicle vehicle = kerbalContext.getVehicleLoader().loadVehicle(new File(vehiclePath));

        vehicle.setEnvironment(Environment.KERBIN);

        vehicle.initSimulation();
        return vehicle;
    }

    @Override
    protected void checkRequired(final CommandLine cmdLine) {
        require(cmdLine, "craft");
        checkMoreRequired(cmdLine);
    }

    @Override
    protected void addOptions(final Options options) {
        options.addOption("c", "craft", true, "Path to a vehicle .craft file. Required.");
        addMoreOptions(options);
    }

    protected abstract void checkMoreRequired(CommandLine cmdLine);

    protected abstract void addMoreOptions(Options options);

    protected abstract void run(Vehicle vehicle) throws IOException;

}
