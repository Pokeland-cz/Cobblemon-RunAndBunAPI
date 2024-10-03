package net.ctengine.command;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.ctengine.CTEngine;
import net.ctengine.entity.TrainerVillager;
import net.ctengine.registry.ModEntityRegistry;
import net.ctengine.trainer.TrainerPokemon;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.TrainerRegistry;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BattleTestCommand {
    // This command is entirely temporary for testing purposes

    private static int battleTestCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity serverPlayer = EntityArgumentType.getPlayer(context, "player");

        if (serverPlayer.getWorld() instanceof ServerWorld serverWorld){
            serverPlayer.getServer().execute(() -> {
                Trainer trainer = TrainerRegistry.createTrainer(UUID.randomUUID().toString());
                trainer.setDisplayName("Trainer Test");

                TrainerPokemon pokemon = new TrainerPokemon();
                pokemon.setSpecies(PokemonSpecies.INSTANCE.getByIdentifier( Identifier.of("cobblemon","charmander")));
                trainer.addTrainerPokemon(pokemon);

                TrainerPokemon pokemon2 = new TrainerPokemon();
                pokemon2.setSpecies(PokemonSpecies.INSTANCE.getByIdentifier( Identifier.of("cobblemon","bulbasaur")));
                pokemon2.setLevel(2);
                trainer.addTrainerPokemon(pokemon2);
                trainer.setWinCommand("give %player% minecraft:diamond 20");
                trainer.setCanOnlyBeatOnce(true);

                trainer.save();

                TrainerVillager trainerVillager = new TrainerVillager(ModEntityRegistry.TRAINER_VILLAGER.get(), serverWorld);

                trainerVillager.setPosition(serverPlayer.getPos());

                trainerVillager.setTrainer(trainer);

                trainerVillager.setCustomName(Text.of(trainer.getDisplayName()));
                trainerVillager.setCustomNameVisible(true);
                serverWorld.spawnEntityAndPassengers(trainerVillager);
            });
        }

        return 1;
    }

    public static void register(){
        CommandRegistrationEvent.EVENT.register((dispatcher, access, environment) -> {
            dispatcher.register(CommandManager.literal("ctengine")
                    .then(CommandManager.literal("battleTestCommand")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(context -> {
                                        try{
                                            return battleTestCommand(context);
                                        } catch (CommandSyntaxException e){
                                            CTEngine.LOGGER.info(e.toString());
                                            return -1;
                                        }
                                    }))));
        });
    }
}
