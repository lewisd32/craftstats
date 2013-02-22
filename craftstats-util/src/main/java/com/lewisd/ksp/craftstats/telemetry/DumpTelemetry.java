package com.lewisd.ksp.craftstats.telemetry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DumpTelemetry implements TelemetryListener {

    private static final int INTERVAL = 1000;

    private static final Map<String, String> HEADER_PARAMETERS = buildHeaderParameters();
    private static final Map<String, String> PARAMETERS = buildParameters();

    private static Map<String, String> buildHeaderParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("met", "vessel.missionTime");
        parameters.put("name", "vessel.vesselName");
        return parameters;
    }

    private static Map<String, String> buildParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("met", "vessel.missionTime");
        parameters.put("alt", "vessel.altitude");
        parameters.put("vspeed", "vessel.verticalSpeed");
        return parameters;
    }

    private final TelemetryReader telemetryReader;
    private final String directory;
    private PrintWriter writer;

    public DumpTelemetry(final String baseUrl, final String directory) throws IOException {
        this.directory = directory;
        telemetryReader = new TelemetryReader(baseUrl, PARAMETERS, this);
    }


    public static void main(final String args[]) throws Exception {
        final String baseUrl = "http://localhost:8080/telemachus";
        if (args.length > 0) {
            new DumpTelemetry(baseUrl, args[0]).run();
        } else {
            new DumpTelemetry(baseUrl, null).run();
        }
    }

    public void run() throws IOException {
        final Map<String, String> header = telemetryReader.query(HEADER_PARAMETERS);

        if (directory != null) {
            final String name = header.get("name");
            final File file = getUnusedFile(name);
            System.out.println("Writing telemetry to " + file.getAbsolutePath());
            writer = new PrintWriter(file);
        } else {
            writer = null;
        }

        System.out.println(header);
        if (writer != null) {
            writer.println(header);
        }
        telemetryReader.start();
        try {
            Thread.sleep(1000000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    private File getUnusedFile(final String name) {
        int id = 0;
        while (true) {
            final File file = new File(directory, String.format("%s-%05d.telemetry", name, id));
            if (!file.exists()) {
                return file;
            }
            id++;
        }
    }

    @Override
    public void handleResult(final Map<String, String> result) {
        System.out.println(result);
        if (writer != null) {
            writer.println(result);
            writer.flush();
        }
    }

    @Override
    public void handleIOException(final IOException e) {
        e.printStackTrace();
    }

}
