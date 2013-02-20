package com.lewisd.ksp.craftstats.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CfgProperties {

    private final List<CfgLine> lines;

    public CfgProperties() {
        this(new ArrayList<CfgLine>());
    }

    public CfgProperties(final List<CfgLine> lines) {
        this.lines = lines;
    }

    public void add(final String key, final String value) {
        final CfgKeyValueLine newLine = new CfgKeyValueLine(key, value);
        addLine(newLine);
    }

    public void addLine(final CfgLine newLine) {
        if (newLine instanceof CfgKeyValueLine) {
            final String key = ((CfgKeyValueLine) newLine).getKey();
            for (int i = lines.size() - 1; i >= 0; --i) {
                final CfgLine line = lines.get(i);
                if (line instanceof CfgKeyValueLine) {
                    if (key.equals(((CfgKeyValueLine) line).getKey())) {
                        lines.add(i + 1, newLine);
                        return;
                    }
                }
            }
        }
        lines.add(newLine);
    }

    public List<String> getList(final String key) {
        final List<String> values = getOptionalList(key);
        if (values.isEmpty()) {
            throw new IllegalStateException("No value for key '" + key + "'");
        }
        return values;
    }

    public List<String> getOptionalList(final String key) {
        final List<String> values = new ArrayList<>();

        for (final CfgLine line : lines) {
            if (line instanceof CfgKeyValueLine) {
                final CfgKeyValueLine keyLine = (CfgKeyValueLine) line;
                if (key.equals(keyLine.getKey())) {
                    values.add(keyLine.getValue());
                }
            }
        }

        return values;
    }

    public String get(final String key) {
        final List<String> values = getList(key);
        if (values.size() != 1) {
            throw new IllegalStateException("Expected 1 value for '" + key + "', found " + values.size());
        }
        final String value = values.get(0);
        if (value.equals("unset")) {
            throw new IllegalStateException("Unset value for key '" + key + "'");
        }
        return value;
    }

    public String getOptional(final String key) {
        final List<String> values = getOptionalList(key);
        if (values.size() > 1) {
            throw new IllegalStateException("Expected 0 or 1 value for '" + key + "', found " + values.size());
        }
        if (values.isEmpty()) {
            return null;
        }
        final String value = values.get(0);
        return value;
    }

    public double getDouble(final String key) {
        return Double.parseDouble(get(key));
    }

    public long getLong(final String key) {
        return Long.parseLong(get(key));
    }

    public int getInt(final String key) {
        return Integer.parseInt(get(key));
    }

    public CfgProperties unmodifiable() {
        return new CfgProperties(Collections.unmodifiableList(lines));
    }

    public void putAll(final CfgProperties cfgProperties) {

        for (final CfgLine line : cfgProperties.lines) {
            if (line instanceof CfgKeyValueLine) {
                final CfgKeyValueLine keyCfg = (CfgKeyValueLine) line;
                final String key = keyCfg.getKey();
                removeKey(key);
                addLine(line);
            } else {
                lines.add(line);
            }

        }
    }

    private void removeKey(final String key) {
        final Iterator<CfgLine> i = lines.iterator();
        while (i.hasNext()) {
            final CfgLine line = i.next();
            if (line instanceof CfgKeyValueLine) {
                if (key.equals(((CfgKeyValueLine) line).getKey())) {
                    i.remove();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Properties<" + lines + ">";
    }

    public CfgKeyValueLine getLine(final String key) {
        final List<CfgKeyValueLine> lines = getLines(key);
        if (lines.size() != 1) {
            throw new IllegalStateException("Expected 1 value for '" + key + "', found " + lines.size());
        }
        final CfgKeyValueLine line = lines.get(0);
        if (line.getValue().equals("unset")) {
            throw new IllegalStateException("Unset value for key '" + key + "'");
        }
        return line;
    }

    public List<CfgKeyValueLine> getLines(final String key) {
        final List<CfgKeyValueLine> matchedLines = new ArrayList<>();

        for (final CfgLine line : lines) {
            if (line instanceof CfgKeyValueLine) {
                final CfgKeyValueLine keyLine = (CfgKeyValueLine) line;
                if (key.equals(keyLine.getKey())) {
                    matchedLines.add(keyLine);
                }
            }
        }

        return matchedLines;
    }

    public List<CfgLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

}
