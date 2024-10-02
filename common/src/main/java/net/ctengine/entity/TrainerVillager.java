package net.ctengine.entity;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.base.Suppliers;
import net.ctengine.battle.BattleHandler;
import net.ctengine.CTEngine;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.TrainerRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Supplier;

// This class is ripped from my other mod and again temporary and just used for testing purposes
public class TrainerVillager extends VillagerEntity {

    public static final Supplier<EntityType<TrainerVillager>> TYPE = Suppliers.memoize(() -> EntityType.Builder.create(TrainerVillager::new, SpawnGroup.MISC).build("trainer_villager"));
    private final World entityWorld;
    private Trainer trainer = null;

    public TrainerVillager(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
        this.entityWorld = world;
        this.setPersistent();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }


    @Override
    public void tickMovement() {
        super.tickMovement();
        this.setVelocity(0, 0, 0);
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (player instanceof ServerPlayerEntity serverPlayer && this.entityWorld instanceof ServerWorld serverWorld){
            if (this.trainer != null){

                // Check if trainer team empty
                if (this.trainer.getBattleTeam().isEmpty()) {
                    serverPlayer.sendMessage(Text.of("trainer has no pokemon"));
                    return ActionResult.FAIL;
                }

                // Check if player team has at least one alive pokemon
                PlayerPartyStore playerTeam = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);
                boolean allDead = true;
                boolean hasPokemon = false;

                for(Pokemon pokemon : playerTeam){
                    hasPokemon = true;
                    if (!pokemon.isFainted()) {
                        allDead = false;
                        break;
                    }
                }
                if(allDead && hasPokemon){
                    serverPlayer.sendMessage(Text.of("At least one of your Pokémon must be alive."));
                    return ActionResult.FAIL;
                }

                serverWorld.getServer().execute(() -> {
                    // If everything is fine then continue with battle
                    BattleHandler.requestTrainerBattle(
                            serverPlayer, trainer,
                            this
                    );
                });

                return ActionResult.SUCCESS;
            } else {
                CTEngine.LOGGER.info("DEBUG: PLAYER RIGHT-CLICKED BUT TRAINER IS NULL");
            }

            return ActionResult.FAIL;
        }

        return super.interactMob(player, hand);
    }

    public void setTrainer(Trainer trainer){
        this.trainer = trainer;

    }

    @Override
    public int getMaxLookYawChange() {
        return 360;
    }

    @Override
    public boolean isPushable() {
        return false;
    }


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.trainer != null) {
            nbt.putString("TrainerUUID", this.trainer.getId());
        }
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("TrainerUUID")) {
            String trainerUUID = nbt.getString("TrainerUUID");
            this.trainer = TrainerRegistry.getTrainer(trainerUUID);
        }
    }
}