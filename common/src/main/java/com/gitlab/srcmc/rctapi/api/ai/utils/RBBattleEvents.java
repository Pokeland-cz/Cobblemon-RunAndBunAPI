package com.gitlab.srcmc.rctapi.api.ai.utils;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleStartedPreEvent;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.RunBunAI;
import kotlin.Unit;

public class RBBattleEvents {

    public static void register() {
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe(
                Priority.NORMAL,  // ✅ Priority required
                event -> {
                    System.out.println("Battle is about to start! " +
                            "Players: " + event.getBattle().getPlayers());

                    // 👉 Custom logic here
                    RunBunAI.setHasResetDefault(false);
                    ModCommon.LOG.info("WE HAVE STARTED A NEW BATTLE AND RESET TO DEFAULT");
                    // Example: cancel battle
                    // event.setCanceled(true);

                    return Unit.INSTANCE;  // ✅ Kotlin Unit return
                }
        );
    }
}
