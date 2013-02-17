package com.lewisd.ksp.craftstats.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CfgReader {

    public CfgGroup readCfg(final File file) throws IOException {
        try (final InputStream in = new FileInputStream(file)) {
            return readCfg(in);
        }
    }

    public CfgGroup readCfg(final InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final CfgGroup group = new CfgGroup();
        return readGroup(group, reader);
    }

    private CfgGroup readGroup(final CfgGroup group, final BufferedReader reader) throws IOException {
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                // eof
                return group;
            }
            line = line + "\r\n";
            final String trimmedLine = line.trim();
            if (trimmedLine.equals("}")) {
                // add line to group
                return group;
            }

            if (trimmedLine.equals("{")) {
                // add line to group
                continue;
            }

            if (trimmedLine.startsWith("//")) {
                continue;
            }

            if (trimmedLine.isEmpty()) {
                continue;
            }

            final int indexOfEquals = trimmedLine.indexOf('=');
            if (indexOfEquals < 0) {
                // start of another group
                final CfgLine groupNameLine = new CfgRawLine(line);

                final CfgGroup subgroup = new CfgGroup(groupNameLine);
                readGroup(subgroup, reader);
                group.addSubgroup(subgroup);
            } else {
                final CfgLine cfgLine = new CfgKeyValueLine(line);
                group.addLine(cfgLine);
            }
        }
    }
}
