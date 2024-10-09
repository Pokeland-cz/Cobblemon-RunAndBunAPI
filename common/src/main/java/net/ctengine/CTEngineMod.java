package net.ctengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.architectury.event.events.common.LifecycleEvent;
import net.ctengine.api.CTEngine;
import net.ctengine.api.trainer.TrainerRegistry;
import net.ctengine.commands.CTEngineCommands;
import net.ctengine.example.ExampleMod;

/**
 * Mod initialization logic.
 */
public class CTEngineMod {
    public static final String MOD_ID = "ctengine";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LifecycleEvent.SERVER_STARTED.register(ExampleMod::init); // example...
        CTEngine.init(new TrainerRegistry());
        CTEngineCommands.register();
    }
}
