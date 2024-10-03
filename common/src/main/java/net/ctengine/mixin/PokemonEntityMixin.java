package net.ctengine.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.trainer.TrainerPokemon;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends LivingEntity {

    protected PokemonEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow(remap = false)
    public abstract Pokemon getPokemon();

    // Check if Pokemon is trainer owned, used for checking if it should be saved
    @Unique
    private boolean cobblemonTrainers$isTrainerOwned() {
        return TrainerPokemon.isTrainerOwned.contains(getPokemon().getUuid());
    }

    // Controls whether the entity should be saved when the world is unloaded.
    // We set this to false in case the server is shutdown during a battle
    @Inject(method = "shouldSave", at = @At("HEAD"), cancellable = true)
    private void injectShouldSave(CallbackInfoReturnable<Boolean> cir) {
        if (cobblemonTrainers$isTrainerOwned()) cir.setReturnValue(false);
    }

    // If the entity is "removed" then make sure to remove it from the
    // trainer owned array.
    @Inject(method = "remove", at = @At("HEAD"))
    private void injectRemove(RemovalReason reason, CallbackInfo ci) {
        TrainerPokemon.isTrainerOwned.remove(getPokemon().getUuid());
    }
}