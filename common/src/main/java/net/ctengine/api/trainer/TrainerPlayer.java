package net.ctengine.api.trainer;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class TrainerPlayer implements Trainer {
    private ServerPlayerEntity player;

    public TrainerPlayer(@NotNull ServerPlayerEntity player) {
        this.player = player;
    }

    @NotNull
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override @NotNull
    public String getName() {
        return this.player.getDisplayName().getString();
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        var party = new ArrayList<Pokemon>();
        Cobblemon.INSTANCE.getStorage().getParty(this.player).forEach(party::add);
        return party.toArray(new Pokemon[party.size()]);
    }

    @Override @Nullable
    public LivingEntity getEntity() {
        return this.getPlayer();
    }
}
