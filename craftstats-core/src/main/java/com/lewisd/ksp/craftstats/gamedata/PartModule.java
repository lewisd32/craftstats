package com.lewisd.ksp.craftstats.gamedata;

import com.lewisd.ksp.craftstats.cfg.CfgProperties;


public class PartModule {

    private final String name;
    private final CfgProperties cfgProperties;

    public PartModule(final String name, final CfgProperties cfgProperties) {
        this.name = name;
        this.cfgProperties = cfgProperties.unmodifiable();
    }

    public String getName() {
        return name;
    }

    public CfgProperties getProperties() {
        return cfgProperties.unmodifiable();
    }

}
