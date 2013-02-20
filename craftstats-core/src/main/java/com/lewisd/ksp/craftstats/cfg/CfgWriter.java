package com.lewisd.ksp.craftstats.cfg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CfgWriter {

    public void writeCfg(final CfgGroup group, final File file) throws IOException {
        try (final OutputStream out = new FileOutputStream(file)) {
            writeCfg(group, out);
        }
    }

    public void writeCfg(final CfgGroup group, final OutputStream in) throws IOException {
        final PrintWriter writer = new PrintWriter(new OutputStreamWriter(in));
        writeGroup(group, writer);
        writer.flush();
    }

    private void writeGroup(final CfgGroup group, final PrintWriter writer) {

        for (final CfgLine line : group.getHeaderLines()) {
            writer.print(line.getLine());
        }

        for (final CfgLine line : group.getProperties().getLines()) {
            writer.print(line.getLine());
        }

        for (final CfgGroup subGroup : group.getSubgroups()) {
            writeGroup(subGroup, writer);
        }

        for (final CfgLine line : group.getFooterLines()) {
            writer.print(line.getLine());
        }

    }

}