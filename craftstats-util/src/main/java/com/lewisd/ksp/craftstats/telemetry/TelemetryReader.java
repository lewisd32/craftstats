package com.lewisd.ksp.craftstats.telemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

public class TelemetryReader {

    public static final long INTERVAL = 1000;

    private final String baseUrl;
    private final URL url;
    private final List<TelemetryListener> listeners = new ArrayList<>();

    public volatile boolean running;
    private Thread thread;

    public TelemetryReader(final String baseUrl, final Map<String, String> parameters, final TelemetryListener listener) throws MalformedURLException {
        this(baseUrl, parameters);
        addListener(listener);
    }

    public TelemetryReader(final String baseUrl, final Map<String, String> parameters) throws MalformedURLException {
        this.baseUrl = baseUrl;

        url = buildUrl(parameters);
    }

    private URL buildUrl(final Map<String, String> parameters) throws MalformedURLException {
        final List<String> parameterStrings = new ArrayList<>(parameters.size());
        for (final Map.Entry<String, String> entry : parameters.entrySet()) {
            parameterStrings.add(entry.getKey() + "=" + entry.getValue());
        }

        return new URL(baseUrl + "/datalink?" + Joiner.on("&").join(parameterStrings));
    }

    public void addListener(final TelemetryListener listener) {
        listeners.add(listener);
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("Already running");
        }
        running = true;
        thread = new Thread(new Reader());
        thread.start();
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("Not running");
        }
        running = false;
        thread.interrupt();
    }

    public Map<String, String> query(final Map<String, String> parameters) throws IOException {
        final URL url = buildUrl(parameters);
        return query(url);
    }

    private Map<String, String> query(final URL url) throws IOException {
        final InputStream input = url.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final String line = reader.readLine();
        input.close();

        final Map<String, String> results = new HashMap<>();
        final String parts[] = line.split(";");
        for (final String part : parts) {
            final String key = part.split("=", 2)[0].trim();
            final String value = part.split("=", 2)[1].trim();
            results.put(key, value);
        }

        return results;
    }

    private class Reader implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            while (running) {
                try {
                    final Map<String, String> results = query(url);

                    for (final TelemetryListener listener : listeners) {
                        listener.handleResult(results);
                    }
                } catch (final IOException e) {
                    for (final TelemetryListener listener : listeners) {
                        listener.handleIOException(e);
                    }
                }

                final long end = System.currentTimeMillis();
                final long duration = end - start;
                if (duration < INTERVAL) {
                    try {
                        Thread.sleep(INTERVAL - duration);
                    } catch (final InterruptedException e1) {
                        return;
                    }
                }
                start += INTERVAL;
            }
        }
    }

}
