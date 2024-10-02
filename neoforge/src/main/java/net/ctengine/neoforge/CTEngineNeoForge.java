package net.ctengine.neoforge;

import net.neoforged.fml.common.Mod;

import net.ctengine.CTEngine;

@Mod(CTEngine.MOD_ID)
public final class CTEngineNeoForge {
    public CTEngineNeoForge() {
        // Run our common setup.
        CTEngine.init();
    }
}
