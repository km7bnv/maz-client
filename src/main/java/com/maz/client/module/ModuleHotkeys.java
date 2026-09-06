package com.maz.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModuleHotkeys {

    private static final Map<Module, KeyMapping> KEYS = new LinkedHashMap<>();

    private ModuleHotkeys() {
    }

    public static void registerAll(ModuleManager manager, KeyMapping.Category category) {
        for (Module module : manager.getModules()) {
            String id = module.getName().toLowerCase()
                    .replace(' ', '_')
                    .replaceAll("[^a-z0-9_]", "");

            KeyMapping mapping = KeyMappingHelper.registerKeyMapping(
                    new KeyMapping(
                            "key.maz-client.module." + id,
                            InputConstants.Type.KEYSYM,
                            GLFW.GLFW_KEY_UNKNOWN,
                            category
                    )
            );
            KEYS.put(module, mapping);
        }
    }

    public static void tick() {
        for (Map.Entry<Module, KeyMapping> entry : KEYS.entrySet()) {
            while (entry.getValue().consumeClick()) {
                entry.getKey().toggle();
            }
        }
    }
}
