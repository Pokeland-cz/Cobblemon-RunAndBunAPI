package com.gitlab.srcmc.rctapi.client.network.handler;

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.client.ModClient;
import com.gitlab.srcmc.rctapi.client.network.packet.BattleDispatchesCompletePacket;

import net.minecraft.client.Minecraft;

public class BattleDispatchesCompleteHandler implements ClientNetworkPacketHandler<BattleDispatchesCompletePacket> {
    @Override
    public void handle(BattleDispatchesCompletePacket arg0, Minecraft arg1) {
        var battle = CobblemonClient.INSTANCE.getBattle();

        if(battle != null) { // TODO: AIActors check
            ModCommon.LOG.info("++ DISPATCHES COMPLETE");
            ModClient.BATTLE_STATE.setDispatchesComplete(true);
        }
    }
}
