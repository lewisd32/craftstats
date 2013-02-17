package com.lewisd.ksp.craftstats.cfg;

import java.io.InputStream;

import org.junit.Test;

public class CfgReaderTest {
    
    private final CfgReader cfgReader = new CfgReader();

    @Test
    public void test() throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("complex.cfg");
        final CfgGroup craft = cfgReader.readCfg(in);
        
        System.out.println(craft);
    }

}
