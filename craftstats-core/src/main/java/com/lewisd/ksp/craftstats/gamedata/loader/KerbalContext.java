package com.lewisd.ksp.craftstats.gamedata.loader;

import java.io.File;
import java.io.IOException;

import com.lewisd.ksp.craftstats.cfg.CfgReader;
import com.lewisd.ksp.craftstats.cfg.CfgWriter;
import com.lewisd.ksp.craftstats.gamedata.PartModules;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;
import com.lewisd.ksp.craftstats.vehicle.loader.VehicleLoader;

public class KerbalContext {

    private final CfgReader cfgReader = new CfgReader();
    private final CfgWriter cfgWriter = new CfgWriter();
    private final PartModuleLoader partModuleLoader = new PartModuleLoader(cfgReader);
    private final PartModules partModules = new PartModules(partModuleLoader);
    private final ResourcesLoader resourcesLoader = new ResourcesLoader(cfgReader);

    private final File partsDir;
    private final File resourcesFile;
    private PartsLoader partsLoader;
    private PartLoader partLoader;
    private Resources resources;
    private Parts parts;

    public KerbalContext() throws IOException {
        this(getKspHome());
    }

    public KerbalContext(final String kspHome) throws IOException {
        this(kspHome, true);
    }

    public KerbalContext(final String kspHome, final boolean load) throws IOException {
        partsDir = new File(kspHome, "Parts");
        resourcesFile = new File(kspHome, "Resources/ResourcesGeneric.cfg");

        if (load) {
            loadParts();
        }
    }

    public void loadResources() throws IOException {
        if (resources == null) {
            resources = resourcesLoader.loadResources(resourcesFile);
        }
    }

    public void loadParts() throws IOException {
        if (parts == null) {
            loadResources();
            partLoader = new PartLoader(cfgReader, partModules, resources);
            partsLoader = new PartsLoader(partLoader, partModules, resources);
            parts = partsLoader.loadParts(partsDir);
        }
    }

    public CfgReader getCfgReader() {
        return cfgReader;
    }

    public Parts getParts() throws IOException {
        loadParts();
        return parts;
    }

    public Resources getResources() throws IOException {
        loadResources();
        return resources;
    }

    public VehicleLoader getVehicleLoader() throws IOException {
        loadParts();
        return new VehicleLoader(cfgReader, cfgWriter, parts, resources);
    }

    public static String getKspHome() {
        final String propKspHome = System.getProperty("ksp_home");
        if (propKspHome != null) {
            return propKspHome;
        }
        final String envKspHome = System.getenv().get("KSP_HOME");
        if (envKspHome != null) {
            return envKspHome;
        }
        throw new RuntimeException("KSP directory has not been configured.");
    }

}
