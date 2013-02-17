package com.lewisd.ksp.craftstats.ascent;

import java.io.PrintStream;

import com.lewisd.ksp.craftstats.vehicle.Stage;

public class PrintingAscentEventListener implements AscentEventListener {

    private final PrintStream out;

    public PrintingAscentEventListener(final PrintStream out) {
        this.out = out;
    }

    @Override
    public void throttleChanged(final double throttle) {
        out.println("Setting throttle to " + throttle + "%");
    }

    @Override
    public void stageEjected(final Stage stage) {
        out.println("Eject stage " + stage.getNumber());
    }

}
