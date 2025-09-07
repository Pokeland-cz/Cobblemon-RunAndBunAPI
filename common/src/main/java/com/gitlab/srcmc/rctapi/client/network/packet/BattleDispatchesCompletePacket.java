package com.gitlab.srcmc.rctapi.client.network.packet;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import kotlin.jvm.functions.Function1;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class BattleDispatchesCompletePacket implements NetworkPacket<BattleDispatchesCompletePacket> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rctapi", "battle_dispatches_complete");

    public static BattleDispatchesCompletePacket decode(RegistryFriendlyByteBuf arg0) {
        return new BattleDispatchesCompletePacket();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf arg0) {
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void sendToAllPlayers() {
        throw new UnsupportedOperationException("Unimplemented method 'sendToAllPlayers'");
    }

    @Override
    public void sendToPlayer(ServerPlayer arg0) {
        CobblemonNetwork.INSTANCE.sendPacketToPlayer(arg0, this);
    }

    @Override
    public void sendToPlayers(Iterable<? extends ServerPlayer> arg0) {
        CobblemonNetwork.INSTANCE.sendPacketToPlayers(arg0, this);
    }

    @Override
    public void sendToPlayersAround(double arg0, double arg1, double arg2, double arg3, ResourceKey<Level> arg4, Function1<? super ServerPlayer, Boolean> arg5) {
        throw new UnsupportedOperationException("Unimplemented method 'sendToPlayersAround'");
    }

    @Override
    public void sendToServer() {
        throw new UnsupportedOperationException("Unimplemented method 'sendToServer'");
    }

    @Override
    public Type<BattleDispatchesCompletePacket> type() {
        return new CustomPacketPayload.Type<BattleDispatchesCompletePacket>(this.getId());
    }
}
