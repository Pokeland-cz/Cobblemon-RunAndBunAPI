package net.ctengine.api.trainer;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A trainer that is represented by a player.
 */
public class TrainerPlayer implements Trainer {
    private ServerPlayerEntity player;

    /**
     * Creates a trainer for the given player instance.
     * 
     * @param player Player instance to associate the trainer to.
     */
    public TrainerPlayer(@NotNull ServerPlayerEntity player) {
        this.player = player;
    }

    /**
     * Retrieves the player associated to this trainer.
     * 
     * @return Player instance.
     */
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
