package com.lewisd.ksp.craftstats.telemetry;

import java.io.IOException;
import java.util.Map;

public interface TelemetryListener {

    void handleResult(Map<String, String> result);

    void handleIOException(IOException e);
}
