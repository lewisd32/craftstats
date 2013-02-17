package com.lewisd.ksp.craftstats.gamedata;

import java.util.Comparator;


public class PartTitleComparator implements Comparator<Part> {

    @Override
    public int compare(final Part p1, final Part p2) {
        return p1.getTitle().compareTo(p2.getTitle());
    }

}
