package com.lewisd.ksp.craftstats.gamedata.loader;

import java.io.File;
import java.io.IOException;

import com.lewisd.ksp.craftstats.gamedata.Part;
import com.lewisd.ksp.craftstats.gamedata.PartModules;
import com.lewisd.ksp.craftstats.gamedata.Parts;
import com.lewisd.ksp.craftstats.gamedata.Resources;

public class PartsLoader {
    private final PartLoader partLoader;
    private final PartModules modules;
    private final Resources resources;

    public PartsLoader(final PartLoader partLoader, final PartModules modules, final Resources resources) {
        this.partLoader = partLoader;
        this.modules = modules;
        this.resources = resources;
    }

    public Parts loadParts(final File directory) throws IOException {
        final Parts parts = new Parts(modules, resources);

        for (final File file : directory.listFiles()) {
            if (file.isDirectory()) {
                final Part part = partLoader.loadPart(file);
                parts.addPart(part);
            }
        }

        return parts;
    }
}
