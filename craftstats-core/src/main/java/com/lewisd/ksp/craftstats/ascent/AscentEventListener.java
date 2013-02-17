package com.lewisd.ksp.craftstats.ascent;

import com.lewisd.ksp.craftstats.vehicle.Stage;

public interface AscentEventListener {

    void throttleChanged(double throttle);

    void stageEjected(Stage stage);

}
