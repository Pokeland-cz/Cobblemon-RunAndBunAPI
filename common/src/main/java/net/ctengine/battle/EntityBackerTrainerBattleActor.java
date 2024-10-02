package net.ctengine.battle;

import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

public class EntityBackerTrainerBattleActor extends AIBattleActor implements EntityBackedBattleActor<LivingEntity> {
    private final String name;
    private final LivingEntity entity;

    public EntityBackerTrainerBattleActor(
            String name,
            LivingEntity entity,
            UUID uuid,
            List<BattlePokemon> pokemonList,
            BattleAI artificialDecider
    ) {
        super(uuid, pokemonList, artificialDecider);
        this.name = name;
        this.entity = entity;
    }

    @Override
    public LivingEntity getEntity() {
        return this.entity;
    }

    @NotNull
    @Override
    public ActorType getType() {
        return ActorType.NPC;
    }

    @NotNull
    @Override
    public MutableText getName() {
        return Text.literal(this.name);
    }

    @NotNull
    @Override
    public MutableText nameOwned(@NotNull String s) {
        return battleLang("owned_pokemon", getName(), this.name);
    }

    @Nullable
    @Override
    public Vec3d getInitialPos() {
        return this.entity.getPos();
    }
}