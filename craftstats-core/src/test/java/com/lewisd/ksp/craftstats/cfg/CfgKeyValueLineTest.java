package com.lewisd.ksp.craftstats.cfg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CfgKeyValueLineTest {

    @Test
    public void shouldParseLine() {
        CfgKeyValueLine cfgLine = new CfgKeyValueLine("  hello = world\r\n");
        
        assertEquals("  ", cfgLine.getPrefix());
        assertEquals("hello", cfgLine.getKey());
        assertEquals(" = ", cfgLine.getMiddle());
        assertEquals("world", cfgLine.getValue());
        assertEquals("\r\n", cfgLine.getSuffix());
    }

    @Test
    public void shouldReconstructLine() {
        final String line = "  hello = world\r\n";
        CfgKeyValueLine cfgLine = new CfgKeyValueLine(line);
        
        assertEquals(line, cfgLine.getLine());
    }

}
