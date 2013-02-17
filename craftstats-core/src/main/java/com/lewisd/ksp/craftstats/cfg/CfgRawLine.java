package com.lewisd.ksp.craftstats.cfg;

public class CfgRawLine implements CfgLine {

    private String line;
    
    public CfgRawLine() {
    }
    
    public CfgRawLine(String line) {
        this.line = line;
    }

    @Override
    public String getLine() {
        return line;
    }
    @Override
    public void setLine(String line) {
        this.line = line;
    }

}
