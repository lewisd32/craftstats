package com.lewisd.ksp.craftstats.gamedata.loader;

import java.io.IOException;
import java.io.InputStream;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.gamedata.PartModule;

public class PartModuleLoader {

    private final CfgReader cfgReader;

    public PartModuleLoader(final CfgReader cfgReader) {
        this.cfgReader = cfgReader;
    }

    public PartModule loadPartModule(final String moduleName) {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (final InputStream in = classloader.getResourceAsStream("partmodules/" + moduleName + ".cfg")) {
            if (in == null) {
                throw new IllegalArgumentException("Unable to find part module definition for " + moduleName);
            }
            final CfgGroup root = cfgReader.readCfg(in);
            final CfgProperties cfgProperties = root.getProperties();

            return new PartModule(moduleName, cfgProperties);
        } catch (final IOException e) {
            throw new RuntimeException("Error while loading part module " + moduleName, e);
        }
    }

}
