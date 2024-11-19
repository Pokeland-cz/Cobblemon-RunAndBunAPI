package net.ctengine.api.models.converter;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.errors.CTError;
import net.ctengine.api.errors.CTErrors;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.trainer.Trainer;
import net.ctengine.api.trainer.TrainerBag;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.util.Locations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;

/**
 * Defines a conversion from {@link TrainerModel} to {@link Trainer}.
 */
public class TrainerModelConverter implements Converter<TrainerModel, TrainerNPC> {
    private final MinecraftServer server;
    private final PokemonModelConverter pmc;

    /**
     * Creates a new trainer model converter.
     * 
     * @param server Minecraft server this converter is associated to.
     * @param pmc Model converter used for pokemon in trainer teams.
     */
    public TrainerModelConverter(@NotNull MinecraftServer server, @NotNull PokemonModelConverter pmc) {
        this.server = server;
        this.pmc = pmc;
    }

    @Override
    public TrainerNPC toTarget(@NotNull TrainerModel model, @NotNull CTErrors<CTException> errors) {
        if(model.getTeam().size() > 6) {
            errors.add(CTError.of("too many pokemon in party " + model.getTeam().size() + "/6"));
        }

        BattleAI battleAI;

        if(model.getAI() == null) {
            errors.add(CTError.of("unknown AI type"));
            battleAI = new RandomBattleAI();
        } else {
            battleAI = model.getAI().getInstanceFor(this.server);
        }

        var team = model.getTeam().stream().limit(6)
            .map(pkModel -> this.pmc.toTarget(pkModel, errors))
            .toList().toArray(new Pokemon[0]);

        var bag = new TrainerBag();

        model.getBag().forEach(bim -> {
            try {
                bag.add(Locations.withNamespace("cobblemon", bim.getItem()), bim.getQuantity());
            } catch(IllegalArgumentException e) {
                errors.add(CTError.of(e));
            }
        });

        return new TrainerNPC(model.getName(), team, bag, battleAI, EntityType.VILLAGER.create(this.server.overworld()));
    }
}
