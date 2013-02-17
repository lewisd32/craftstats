package com.lewisd.ksp.craftstats.gamedata;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lewisd.ksp.craftstats.cfg.CfgGroup;
import com.lewisd.ksp.craftstats.cfg.CfgKeyValueLine;

public class Parts {
    private static final Logger LOG = Logger.getLogger(Parts.class);

    private final Part UNKNOWN_PART;

    private final Map<String, Part> partsByNameMap = new HashMap<>();

    public Parts(final PartModules modules, final Resources resources) {
        final CfgGroup cfgGroup = new CfgGroup();
        cfgGroup.addLine(new CfgKeyValueLine("mass", "0.1"));
        UNKNOWN_PART = new Part(modules.getPartModule("Part"), cfgGroup, resources);
    }

    public Part getPart(final String name) {
        if (!partsByNameMap.containsKey(name)) {
            LOG.error("No part found for name '" + name + "', using default part.  Stats may be unreliable.");
            return UNKNOWN_PART;
        }
        return partsByNameMap.get(name);
    }

    public void addPart(final Part part) {
        partsByNameMap.put(part.getName(), part);
    }
}
