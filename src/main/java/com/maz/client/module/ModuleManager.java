package com.maz.client.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> modulesByName = new HashMap<>();

    public void register(Module module) {
        modules.add(module);
        modulesByName.put(normalize(module.getName()), module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public Module getModule(String name) {
        if (name == null) {
            return null;
        }
        return modulesByName.get(normalize(name));
    }

    public void tick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
