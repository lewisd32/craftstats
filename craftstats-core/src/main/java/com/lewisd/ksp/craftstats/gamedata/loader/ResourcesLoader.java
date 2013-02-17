package com.lewisd.ksp.craftstats.gamedata.loader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.gamedata.Resource;
import com.lewisd.ksp.craftstats.gamedata.Resources;

public class ResourcesLoader {

    private final CfgReader cfgReader;

    public ResourcesLoader(final CfgReader cfgReader) {
        this.cfgReader = cfgReader;
    }

    public Resources loadResources(final File resourceFile) throws IOException {
        final Map<String, Resource> resources = new HashMap<>();

        final CfgGroup root = cfgReader.readCfg(resourceFile);

        for (final CfgGroup resourceGroup : root.getSubgroup("RESOURCE_DEFINITION")) {
            final String name = resourceGroup.getProperties().get("name");
            final double density = resourceGroup.getProperties().getDouble("density");

            final Resource resource = new Resource(name, density);
            resources.put(name, resource);
        }

        return new Resources(resources);
    }
}
