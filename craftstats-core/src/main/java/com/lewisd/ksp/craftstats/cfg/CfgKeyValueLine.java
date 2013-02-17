package com.lewisd.ksp.craftstats.cfg;

public class CfgKeyValueLine implements CfgLine {
    
    private String prefix; 
    private String key; 
    private String middle; 
    private String value; 
    private String suffix;
    
    public CfgKeyValueLine(String key, String value) {
        this("", key, value);
    }
    
    public CfgKeyValueLine(String prefix, String key, String value) {
        this.prefix = prefix;
        this.key = key;
        middle = " = ";
        this.value = value;
        suffix = "\r\n";
    }
    
    public CfgKeyValueLine(String line) {
        setLine(line);
    }
    
    @Override
    public String getLine() {
        return prefix + key + middle + value + suffix;
    }
    
    @Override
    public void setLine(String line) {
        String parts[] = line.split("=", 2);
        key = parts[0].trim();
        value = parts[1].trim();
        final int equalsIndex = line.indexOf('=');
        final int keyIndex = line.indexOf(key);
        final int valueIndex = line.indexOf(value, equalsIndex+1);
        
        prefix = line.substring(0, keyIndex);
        middle = line.substring(keyIndex + key.length(), valueIndex);
        suffix = line.substring(valueIndex + value.length());
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMiddle() {
        return middle;
    }

    public void setMiddle(String middle) {
        this.middle = middle;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    @Override
    public String toString() {
        return getLine();
    }

}
