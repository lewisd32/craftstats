package com.lewisd.ksp.craftstats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;

@Ignore
public class CraftMergerTest {

    private static final String LINE_ENDING = "\r\n";
    private KerbalContext kerbalContext;

    @Before
    public void setup() throws Exception {
        kerbalContext = new KerbalContext();
    }

    @Test
    public void test() throws Exception {
        final String payloadFile = "src/test/resources/XO Lander Mk1.craft";
        final String launcherFile = "src/test/resources/XO Launcher HD Mk1.craft";

        CfgGroup payloadGroup;
        CfgGroup launcherGroup;

        try (final FileInputStream payloadIn = new FileInputStream(payloadFile);
             final FileInputStream launcherIn = new FileInputStream(launcherFile)) {
            payloadGroup = kerbalContext.getCfgReader().readCfg(payloadIn);
            launcherGroup = kerbalContext.getCfgReader().readCfg(launcherIn);
        }

        final String payloadName = payloadGroup.getProperties().get("ship");
        final String launcherName = launcherGroup.getProperties().get("ship");

        final String mergedName = launcherName + " + " + payloadName;

        long minId = Long.MAX_VALUE;
        for (final CfgGroup partGroup : launcherGroup.getSubgroup("PART")) {
            final String partName = partGroup.getProperties().get("part");
            final String idStr = partName.substring(partName.indexOf('_')+1);
            final long id = Long.parseLong(idStr);
            minId = Math.min(id, minId);
        }

        System.out.println("Found min uid for launcher: " + minId);

        final Map<String, String> payloadIdRemap = new HashMap<>();
        for (final CfgGroup partGroup : payloadGroup.getSubgroup("PART")) {
            final String partName = partGroup.getProperties().get("part");
            final String name = partName.substring(0, partName.indexOf('_'));
            final long newId = minId - 1;
            minId = newId;
            final String newName = name + "_" + newId;
            payloadIdRemap.put(partName, newName);
            System.out.println("Remapping " + partName + " to " + newName);
        }


        final String outputFile = "src/test/resources/" + mergedName + ".craft";

        try (final FileWriter mergedOut = new FileWriter(outputFile);
             final BufferedReader payloadReader = new BufferedReader(new FileReader(payloadFile));
             final BufferedReader launcherReader = new BufferedReader(new FileReader(launcherFile))) {

            ignoreLine(payloadReader.readLine(), "ship");
            ignoreLine(payloadReader.readLine(), "version");
            ignoreLine(payloadReader.readLine(), "type");

            ignoreLine(launcherReader.readLine(), "ship");
            mergedOut.write("ship = " + mergedName);
            mergedOut.write(LINE_ENDING);

            String line;
            while ((line = launcherReader.readLine()) != null) {
                mergedOut.write(line);
                mergedOut.write(LINE_ENDING);
            }


            while ((line = payloadReader.readLine()) != null) {

                for (final Map.Entry<String, String> entry : payloadIdRemap.entrySet()) {
                    line = line.replaceAll(entry.getKey(), entry.getValue());
                }

                mergedOut.write(line);
                mergedOut.write(LINE_ENDING);
            }
        }


    }

    private void ignoreLine(final String line, final String expectedKey) {
        final String trimmedLine = line.trim();
        final int indexOfEquals = trimmedLine.indexOf('=');
        if (indexOfEquals < 0) {
            throw new IllegalStateException("Expected " + expectedKey + " but got line: " + trimmedLine);
        }
        final String key = trimmedLine.substring(0, indexOfEquals).trim();
        if (!key.equals(expectedKey)) {
            throw new IllegalStateException("Expected " + expectedKey + " but got " + key);
        }
    }

}
