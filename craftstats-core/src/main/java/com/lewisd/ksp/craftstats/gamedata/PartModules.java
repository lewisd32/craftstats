package com.lewisd.ksp.craftstats.gamedata;

import java.util.HashMap;
import java.util.Map;

import com.lewisd.ksp.craftstats.gamedata.loader.PartModuleLoader;

public class PartModules {

    private final PartModuleLoader loader;
    private final Map<String, PartModule> partModulesByNameMap = new HashMap<>();
    
    public PartModules(PartModuleLoader loader) {
        this.loader = loader; 
    }
    
    public PartModule getPartModule(String moduleName) {
        if (!partModulesByNameMap.containsKey(moduleName)) {
            PartModule partModule = loader.loadPartModule(moduleName);
            if (partModule == null) {
                throw new IllegalArgumentException("No part module found for name '" + moduleName + "'");
            } else {
                partModulesByNameMap.put(moduleName, partModule);
            }
        }
        return partModulesByNameMap.get(moduleName);
    }

    public void addPartModule(PartModule partModule) {
        partModulesByNameMap.put(partModule.getName(), partModule);
    }
}
