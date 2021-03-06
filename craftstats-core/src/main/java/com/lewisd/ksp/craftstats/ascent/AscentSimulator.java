package com.lewisd.ksp.craftstats.ascent;

import java.io.PrintStream;
import java.util.SortedMap;

import com.lewisd.ksp.craftstats.util.Vect3;
import com.lewisd.ksp.craftstats.vehicle.Engine;
import com.lewisd.ksp.craftstats.vehicle.Stage;
import com.lewisd.ksp.craftstats.vehicle.Vehicle;

public class AscentSimulator {

    private final Vehicle vehicle;
    private final double step;
    private double met;
    private double throttle = 100;
    private Vect3 thrustVector = new Vect3(0, 0, 0);
    private Vect3 dragVector = new Vect3(0, 0, 0);
    private Vect3 accelVector = new Vect3(0, 0, 0);
    private final SortedMap<Double, Double> throttlePercentByMet;
    private final SortedMap<Integer, Double> throttlePercentByStage;
    private final AscentEventListener listener;

    public AscentSimulator(final Vehicle vehicle, final double step, final AscentEventListener listener,
                           final SortedMap<Double, Double> throttlePercentByMet,
                           final SortedMap<Integer, Double> throttlePercentByStage) {
        this.vehicle = vehicle;
        this.step = step;
        this.listener = listener;
        this.throttlePercentByMet = throttlePercentByMet;
        this.throttlePercentByStage = throttlePercentByStage;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public double getStep() {
        return step;
    }

    public double getMet() {
        return met;
    }

    public Vect3 getAccelVector() {
        return accelVector;
    }

    public void runStep() {
        met += step;

        if (!throttlePercentByMet.isEmpty()) {
            final double key = throttlePercentByMet.firstKey();
            if (key < met) {
                throttle = throttlePercentByMet.get(key);
                listener.throttleChanged(throttle);
                throttlePercentByMet.remove(key);
            }
        }

        final Stage stage = vehicle.getStagesFromBottom().get(0);

        if (stage.getLOXFuelMass() == 0 && stage.getSolidFuelMass() == 0 && stage.getNumber() != 0) {
            // eject stage since all engines have stopped
            vehicle.ejectStage();
            listener.stageEjected(stage);

            if (!throttlePercentByStage.isEmpty()) {
                final int key = throttlePercentByStage.firstKey();
                if (key >= vehicle.getStagesFromBottom().get(0).getNumber()) {
                    throttle = throttlePercentByStage.get(key);
                    listener.throttleChanged(throttle);
                    throttlePercentByStage.remove(key);
                }
            }
            return;
        }

        final double g = vehicle.getEnvironment().getGravity(vehicle.getAltitude());

        final double pitch = vehicle.getPitch();
        final double mass = stage.getMass();

        thrustVector = new Vect3(0, 0, 0);

        for (final Engine engine : stage.getActiveEngines()) {
            final double thrust = engine.getThrust(throttle);
            final double isp = engine.getIsp();
            final double fuelFlowRate = (thrust / g) / isp;
            final double fuelBurned = fuelFlowRate * step;

            final double accel = thrust / mass;

            if (engine.consumeFuel(fuelBurned)) {
                thrustVector.add(new Vect3(0, accel, 0));
            }
        }

        dragVector = vehicle.getDragAcceleration();
        accelVector = new Vect3(0, -g, 0);

        accelVector.add(thrustVector);
        accelVector.add(dragVector);
        accelVector.rotate(0, 0, pitch);

        if (!stage.launchClampsReleased()) {
            accelVector.setY(0);
        }

        final Vect3 stepAccelVector = new Vect3(accelVector);
        stepAccelVector.multiply(step);

        vehicle.getVelocity().add(stepAccelVector);

        final Vect3 stepVelocityVector = new Vect3(vehicle.getVelocity());
        stepVelocityVector.multiply(step);
        vehicle.getPosition().add(stepVelocityVector);

    }

    public void printAscentState(final PrintStream out, final boolean verbose) {
        final Vect3 orbitalVelocity = vehicle.getVelocity();
        final Vect3 position = vehicle.getPosition();
        final Vect3 rotationalVelocity = vehicle.getEnvironment().getRotationalVelocityVector(position);
        final Vect3 surfaceVelocity = new Vect3(rotationalVelocity);
        surfaceVelocity.subtract(orbitalVelocity);
        final double altitute = vehicle.getAltitude();
        final double pitch = vehicle.getPitch();
        final double mass = vehicle.getMass();
        final double accel = accelVector.getY();

        long millis = (long) (met * 1000);
        final int hours = (int) (millis / (1000 * 60 * 60));
        millis -= hours * (1000 * 60 * 60);
        final int minutes = (int) (millis / (1000 * 60));
        millis -= minutes * (1000 * 60);
        final double seconds = millis / 1000.0;

        // System.out.printf("%3.1f : velocity=%.1fm/s, %.1fm/s, %.1fm/s%n", met, orbitalVelocity.getX(), orbitalVelocity.getY(), orbitalVelocity.getZ());
        out.printf("%02d:%02d:%04.1f : surface velocity=%.1fm/s accel=%.2fm/s/s alt=%.3fkm pitch=%.0f° mass=%.4ft%n",
                   hours, minutes, seconds,
                   surfaceVelocity.getLength(),
                   accel,
                   altitute / 1000,
                   pitch,
                   mass);
        if (verbose) {
            System.out.printf("             thrust=%.1fm/s, %.1fm/s, %.1fm/s, magnitude=%.1fm/s%n",
                              thrustVector.getX(), thrustVector.getY(), thrustVector.getZ(), thrustVector.getLength());
            System.out.printf("             drag=%.1fm/s, %.1fm/s, %.1fm/s, magnitude=%.1fm/s%n",
                              dragVector.getX(), dragVector.getY(), dragVector.getZ(), dragVector.getLength());
            System.out.printf("             orbital velocity=%.1fm/s, %.1fm/s, %.1fm/s, magnitude=%.1fm/s%n",
                              orbitalVelocity.getX(), orbitalVelocity.getY(), orbitalVelocity.getZ(), orbitalVelocity.getLength());
            System.out.printf("             surface velocity=%.1fm/s, %.1fm/s, %.1fm/s, magnitude=%.1fm/s%n",
                              surfaceVelocity.getX(), surfaceVelocity.getY(), surfaceVelocity.getZ(), surfaceVelocity.getLength());
            System.out.printf("             rotational velocity=%.1fm/s, %.1fm/s, %.1fm/s, magnitude=%.1fm/s%n",
                              rotationalVelocity.getX(), rotationalVelocity.getY(), rotationalVelocity.getZ(), rotationalVelocity.getLength());
            System.out.printf("             position=%.1fm, %.1fm, %.1fm%n", position.getX(), position.getY(), position.getZ());
            System.out.printf("             fuel=%.5ft LOX, %.5ft Solid%n",
                              vehicle.getCurrentStage().getLOXFuelMass(), vehicle.getCurrentStage().getSolidFuelMass());
        }
    }

}
