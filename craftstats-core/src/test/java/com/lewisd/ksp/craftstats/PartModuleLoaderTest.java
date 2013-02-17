package com.lewisd.ksp.craftstats;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.gamedata.PartModule;
import com.lewisd.ksp.craftstats.gamedata.loader.PartModuleLoader;

public class PartModuleLoaderTest {
    
    private final CfgReader cfgReader = new CfgReader();
    private final PartModuleLoader partModuleLoader = new PartModuleLoader(cfgReader);

    @Test
    public void test() throws Exception {
        
        final PartModule sasModule = partModuleLoader.loadPartModule("SASModule");
        
        assertEquals("unset", sasModule.getProperties().getOptional("maxTorque"));
        assertEquals("unset", sasModule.getProperties().getOptional("Ki"));
        assertEquals("unset", sasModule.getProperties().getOptional("Kp"));
        assertEquals("unset", sasModule.getProperties().getOptional("Kd"));
        assertNull(sasModule.getProperties().getOptional("foo"));
    }

}
