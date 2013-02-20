package com.lewisd.ksp.craftstats.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

    public CfgGroup(final String groupName) {
        this.groupName = groupName;

        headerLines.add(new CfgRawLine(groupName + "\r\n"));
        headerLines.add(new CfgRawLine("{\r\n"));

        footerLines.add(new CfgRawLine("}\r\n"));
    }

    public CfgGroup(final CfgLine groupNameLine) {
        groupName = groupNameLine.getLine().trim();

        headerLines.add(groupNameLine);
    }

    public String getGroupName() {
        return groupName;
    }

    public List<CfgLine> getHeaderLines() {
        return headerLines;
    }

    public List<CfgLine> getFooterLines() {
        return footerLines;
    }

    public void addLine(final CfgLine newLine) {
        properties.addLine(newLine);
    }

    public void addHeaderLine(final CfgLine cfgLine) {
        headerLines.add(cfgLine);
    }

    public void addFooterLine(final CfgLine cfgLine) {
        footerLines.add(cfgLine);
    }

    public CfgProperties getProperties() {
        return properties.unmodifiable();
    }

    public Set<String> getSubgroupTypes() {
        final Set<String> types = new HashSet<>();
        for (final CfgGroup group : subgroups) {
            types.add(group.getGroupName());
        }
        return Collections.unmodifiableSet(types);
    }

    public List<CfgGroup> getSubgroup(final String type) {
        final List<CfgGroup> groups = new ArrayList<>();
        for (final CfgGroup group : subgroups) {
            if (type.equals(group.getGroupName())) {
                groups.add(group);
            }
        }
        return Collections.unmodifiableList(groups);
    }

    public List<CfgGroup> getSubgroups() {
        return Collections.unmodifiableList(subgroups);
    }

    public void addSubgroup(final CfgGroup subgroup) {
        final String type = subgroup.getGroupName();
        for (int i = subgroups.size() - 1; i >= 0; --i) {
            final CfgGroup group = subgroups.get(i);
            if (type.equals(group.getGroupName())) {
                subgroups.add(i + 1, subgroup);
                return;
            }
        }
        subgroups.add(subgroup);
    }

    public void removeSubgroup(final CfgGroup subgroup) {
        final Iterator<CfgGroup> i = subgroups.iterator();
        while (i.hasNext()) {
            final CfgGroup group = i.next();
            if (group == subgroup) {
                i.remove();
                return;
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(groupName);
        sb.append(" { ");
        sb.append("properties=" + properties);
        sb.append("subgroups=" + subgroups);
        sb.append(" } ");

        return sb.toString();
    }

}
