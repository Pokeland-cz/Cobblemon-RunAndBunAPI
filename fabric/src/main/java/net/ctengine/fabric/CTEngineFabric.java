package net.ctengine.fabric;

import net.fabricmc.api.ModInitializer;
import net.ctengine.CTEngineMod;

public final class CTEngineFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CTEngineMod.init();
    }
}
