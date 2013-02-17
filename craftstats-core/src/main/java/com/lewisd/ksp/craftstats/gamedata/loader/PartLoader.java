package com.lewisd.ksp.craftstats.gamedata.loader;

import java.io.File;
import java.io.IOException;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgProperties;
import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.PartModule;
import com.lewisd.ksp.craftstats.gamedata.PartModules;
import com.lewisd.ksp.craftstats.gamedata.Resources;

public class PartLoader {

    private final CfgReader cfgReader;
    private final Resources resources;
    private final PartModules partModules;

    public PartLoader(final CfgReader cfgReader, final PartModules partModules, final Resources resources) {
        this.cfgReader = cfgReader;
        this.partModules = partModules;
        this.resources = resources;
    }

    Part loadPart(final File partDirectory) throws IOException {
        final CfgGroup root = cfgReader.readCfg(new File(partDirectory, "part.cfg"));
        final CfgProperties properties = root.getProperties();

        final String moduleName = properties.get("module");

        final PartModule module = partModules.getPartModule(moduleName);

        return new Part(module, root, resources);
    }

}
