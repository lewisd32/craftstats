package com.lewisd.ksp.craftstats;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.loader.KerbalContext;

public class PartLoaderTest {

    private KerbalContext kerbalContext;

    @Before
    public void setup() throws Exception {
        kerbalContext = new KerbalContext();
    }

    @Test
    public void test() throws Exception {

        final Parts parts = kerbalContext.getParts();

        final Part sasModule = parts.getPart("sasModule");;

        assertEquals("20", sasModule.getProperties().get("maxTorque"));
        assertEquals("0", sasModule.getProperties().get("Ki"));
        assertEquals("5.0", sasModule.getProperties().get("Kp"));
        assertEquals("3.5", sasModule.getProperties().get("Kd"));
        assertNull(sasModule.getProperties().getOptional("foo"));
    }

}
