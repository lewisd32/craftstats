package com.lewisd.ksp.craftstats.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CfgGroup {

    private final CfgProperties properties = new CfgProperties();
    private final List<CfgGroup> subgroups = new ArrayList<>();
    private final List<CfgLine> headerLines = new ArrayList<>();
    private final List<CfgLine> footerLines = new ArrayList<>();
    private final String groupName;
    
    public CfgGroup() {
        groupName = null;
    }
    
    public CfgGroup(String groupName) {
        this.groupName = groupName;
        
        headerLines.add(new CfgRawLine(groupName + "\r\n"));
        headerLines.add(new CfgRawLine("{\r\n"));
        
        footerLines.add(new CfgRawLine("}\r\n"));
    }
    
    public CfgGroup(CfgLine groupNameLine) {
        groupName = groupNameLine.getLine().trim();
        
        headerLines.add(groupNameLine);
    }
    
    public String getGroupName() {
        return groupName;
    }

    public void addLine(CfgLine newLine) {
        properties.addLine(newLine);
    }
    
    public CfgProperties getProperties() {
        return properties.unmodifiable();
    }

    public Set<String> getSubgroupTypes() {
        Set<String> types = new HashSet<>();
        for (CfgGroup group : subgroups) {
            types.add(group.getGroupName());
        }
        return Collections.unmodifiableSet(types);
    }
    
    public List<CfgGroup> getSubgroup(String type) {
        List<CfgGroup> groups = new ArrayList<>();
        for (CfgGroup group : subgroups) {
            if (type.equals(group.getGroupName())) {
                groups.add(group);
            }
        }        
        return Collections.unmodifiableList(groups);
    }

    public List<CfgGroup> getSubgroups() {
        return Collections.unmodifiableList(subgroups);
    }
    
    public void addSubgroup(CfgGroup subgroup) {
        final String type = subgroup.getGroupName();
        for (int i = subgroups.size() - 1; i >= 0; --i) {
            CfgGroup group = subgroups.get(i);
            if (type.equals(group.getGroupName())) {
                subgroups.add(i+1, subgroup);
                return;
            }
        }
        subgroups.add(subgroup);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(groupName);
        sb.append(" { ");
        sb.append("properties=" + properties);
        sb.append("subgroups=" + subgroups);
        sb.append(" } ");
        
        return sb.toString();
    }
    
    
}
