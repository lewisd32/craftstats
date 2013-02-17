package com.lewisd.ksp.craftstats.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;

public abstract class AbstractMain {

    protected final KerbalContext kerbalContext;
    protected final CommandLine cmdLine;
    private Options options;

    protected AbstractMain(final KerbalContext kerbalContext) {
        this.kerbalContext = kerbalContext;
        cmdLine = null;
    }

    protected AbstractMain(final String[] args) {

        options = new Options();
        options.addOption("k", "ksp", true, "Path to KSP. Required, unless KSP_HOME environment variable is set.");

        addOptions(options);

        final CommandLineParser parser = new PosixParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
            checkRequired(cmdLine);
        } catch (final ParseException e) {
            e.printStackTrace(System.err);
            System.exit(2);
        }
        this.cmdLine = cmdLine;

        try {
            kerbalContext = setupKerbalContext(cmdLine);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to configure KerbalContext", e);
        }
    }

    private KerbalContext setupKerbalContext(final CommandLine cmdLine) throws IOException {
        final KerbalContext kerbalContext;
        if (cmdLine.hasOption("ksp")) {
            kerbalContext = new KerbalContext(cmdLine.getOptionValue("ksp"));
        } else {
            kerbalContext = new KerbalContext();
        }
        return kerbalContext;
    }

    protected void require(final CommandLine cmdLine, final String param) {
        if (!cmdLine.hasOption(param)) {
            System.err.println("Missing required parameter: " + param);
            System.err.println();
            final HelpFormatter formatter = new HelpFormatter();
            final PrintWriter writer = new PrintWriter(System.err);
            formatter.printHelp(writer, 74, this.getClass().getSimpleName().toLowerCase(), null, options, 1, 3, null, true);
            writer.flush();
            System.exit(1);
        }
    }

    protected abstract void checkRequired(CommandLine cmdLine);

    protected abstract void addOptions(Options options);

}
