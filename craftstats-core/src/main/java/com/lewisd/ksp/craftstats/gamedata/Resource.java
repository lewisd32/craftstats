package com.lewisd.ksp.craftstats.gamedata;

public class Resource {
    private final String name;
    private final double density;

    public Resource(String name, double density) {
        this.name = name;
        this.density = density;
    }

    public String getName() {
        return name;
    }

    public double getDensity() {
        return density;
    }
    
}
