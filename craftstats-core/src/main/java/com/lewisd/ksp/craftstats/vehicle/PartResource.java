package com.lewisd.ksp.craftstats.vehicle;

import com.lewisd.ksp.craftstats.gamedata.Resource;

public class PartResource {

    private final Resource resource;
    private final double amount;
    private final double maxAmount;

    public PartResource(Resource resource, double amount, double maxAmount) {
        this.resource = resource;
        this.amount = amount;
        this.maxAmount = maxAmount;
    }

    public Resource getResource() {
        return resource;
    }

    public double getAmount() {
        return amount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

}
