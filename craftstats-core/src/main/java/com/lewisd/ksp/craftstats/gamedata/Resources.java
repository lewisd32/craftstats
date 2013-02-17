package com.lewisd.ksp.craftstats.gamedata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class Resources {

    private final Map<String, Resource> resources = new HashMap<>();

    public Resources(Map<String, Resource> resources) {
        this.resources.putAll(resources);
    }
    
    public Resource getResource(String name) {
        return resources.get(name);
    }
    
    public Set<Resource> getRCSFuels() {
        return Sets.newHashSet(getResource("MonoPropellant"));
    }
    
    public Set<Resource> getSolidFuels() {
        return Sets.newHashSet(getResource("SolidFuel"));
    }
    
    public Set<Resource> getLOXFuels() {
        return Sets.newHashSet(getResource("LiquidFuel"), getResource("Oxidizer"));
    }
    

    
}
