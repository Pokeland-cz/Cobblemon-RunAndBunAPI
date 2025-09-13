package com.gitlab.srcmc.rctapi.api.ai;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ShowdownMoveset.Gimmick;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.interactive.PotionType;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.RCTApi;
import com.gitlab.srcmc.rctapi.api.ai.config.RCTBattleAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.config.RunBunAIConfig;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.ai.utils.Debug;
import com.gitlab.srcmc.rctapi.api.ai.utils.MoveType;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMath;
import com.gitlab.srcmc.rctapi.api.ai.utils.PokeMathMax;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder;
import com.gitlab.srcmc.rctapi.api.ai.utils.TypeChart;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleEffects.Custom;
import com.gitlab.srcmc.rctapi.api.ai.utils.ResponseBuilder.Choice;
import com.gitlab.srcmc.rctapi.api.battle.BattleManager;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import java.util.Random;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import net.fabricmc.loader.impl.util.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import oshi.driver.windows.wmi.Win32Fan;

public class RunBunAI implements BattleAI {
    private static final Random RANDOM = new Random();
    private static final double SUPER_EFFECTIVE = 2;
    private static final double NOT_VERY_EFFECTIVE = 0.5;
    private static final double IMMUNE = 0;
    private static final Map<ElementalType, Map<ElementalType, Double>> typeChart = new HashMap<>();
    private static final List<String> priorityDamageMoves = new ArrayList<>(List.of(
            "quickattack",
            "extremespeed",
            "fakeout",
            "firstimpression",
            "accelerock",
            "aquajet",
            "bulletpunch",
            "iceshard",
            "machpunch",
            "shadowsneak",
            "suckerpunch",
            "vacuumwave",
            "watershuriken"
    ));
    private static final List<String> abilityStatBooster = new ArrayList<>(List.of(
            "cobblemon.ability.moxie",
            "cobblemon.ability.beastboost",
            "cobblemon.ability.chillingneigh",
            "cobblemon.ability.grimneigh"));
    private static final List<String> highCriticalMoves = new ArrayList<>(List.of(
            "aeroblast",
            "aircutter",
            "aquacutter",
            "attackorder",
            "blazekick",
            "crabhammer",
            "crosschop",
            "crosspoison",
            "direclaw",
            "drillrun",
            "esperwing",
            "ivycudgel",
            "karatechop",
            "leafblade",
            "nightslash",
            "poisontail",
            "psychocut",
            "razorleaf",
            "razorwind",
            "shadowblast",
            "shadowclaw",
            "skyattack",
            "slash",
            "snipeshot",
            "spacialrend",
            "stoneedge",
            "triplearrows"));
    private static final List<String> trapMoves = new ArrayList<>(List.of(
            "bind",
            "firespin",
            "infestation",
            "sandtomb",
            "whirlpool",
            "wrap"));
    private static final List<String> speedReductionMoves = new ArrayList<>(List.of(
            "bulldoze",
            "electroweb",
            "icywind",
            "lowsweep",
            "mudshot",
            "pounce",
            "rocktomb"));
    private static final List<String> physicalAttackReductionMoves = new ArrayList<>(List.of(
            "breakingswipe",
            "lunge",
            "tropkick",
            "skittersmack",
            "spiritbreak"
    ));
    private static final List<String> specialAttackReductionMoves = new ArrayList<>(List.of(
            "chillingwater",
            "mysticalfire",
            "snarl",
            "strugglebug"
    ));
    private static final List<String> generalSetupMoves = new ArrayList<>(List.of("Power-up Punch",
            "Swords Dance",
            "Howl",
            "Stuff Cheeks",
            "Barrier",
            "Acid Armor",
            "Iron Defense",
            "Cotton Guard",
            "Charge Beam",
            "Tail Glow",
            "Nasty Plot",
            "Cosmic Power",
            "Bulk Up",
            "Calm Mind",
            "Dragon Dance",
            "Coil",
            "Hone Claws",
            "Quiver Dance",
            "Shift Gear",
            "Shell Smash",
            "Growth",
            "Work Up",
            "Curse",
            "Coil",
            "No Retreat"));
    private static final List<String> ignoreStatDropAbilities = new ArrayList<>(List.of(
            "cobblemon.ability.contrary",
            "cobblemon.ability.clearbody",
            "cobblemon.ability.whitesmoke"));
    private static final List<String> specialFunctionMoves = new ArrayList<>(List.of("futuresight",
            "relicsong",
            "suckerpunch",
            "pursuit",
            "fellstinger",
            "rollout",
            "stealthrock",
            "spikes",
            "toxicspikes",
            "stickyweb",
            "protect",
            "kingsshield",
            "detect",
            "fling",
            "roleplay",
            "shadowsneak",
            "aquajet",
            "iceshard",
            "magnitude",
            "earthquake",
            "imprison",
            "batonpass",
            "tailwind",
            "trickroom",
            "fakeout",
            "helpinghand",
            "followme",
            "finalgambit",
            "electricterrain",
            "psychicterrain",
            "grassyterrain",
            "mistyterrain",
            "lightscreen",
            "reflect",
            "substitute",
            "explosion",
            "selfdestruct",
            "mistyexplosion",
            "memento",
            "thunderwave",
            "stunspore",
            "glare",
            "nuzzle",
            "zapcannon",
            "willowisp",
            "trick",
            "switcheroo",
            "yawn",
            "darkvoid",
            "grasswhistle",
            "sing",
            "dreameater",
            "nightmare",
            "snore",
            "sleeptalk"));

    private static double typeEffectiveness(ElementalType attacker, ElementalType defender) {
        if (!typeChart.containsKey(defender)) return 1;
        if (typeChart.get(defender).containsKey(attacker)) return typeChart.get(defender).get(attacker);
        return 1;
    }
    private static double typeEffectiveness(ElementalType attacker, ElementalType defender, Ability defenderAbility) {
        String defenderAbilityId = defenderAbility.getDisplayName();
        if (attacker.equals(ElementalTypes.INSTANCE.getWATER())) {
            if (
                    defenderAbilityId.equals("cobblemon.ability.stormdrain") ||
                            defenderAbilityId.equals("cobblemon.ability.waterabsorb") ||
                            defenderAbilityId.equals("cobblemon.ability.dryskin")
            ) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getELECTRIC())) {
            if (
                    defenderAbilityId.equals("cobblemon.ability.voltabsorb") ||
                            defenderAbilityId.equals("cobblemon.ability.lightningrod") ||
                            defenderAbilityId.equals("cobblemon.ability.motordrive")
            ) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getGROUND())) {
            if (
                    defenderAbilityId.equals("cobblemon.ability.levitate") ||
                            defenderAbilityId.equals("cobblemon.ability.eartheater")
            ) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getFIRE())) {
            if (
                    defenderAbilityId.equals("cobblemon.ability.wellbakedbody") ||
                            defenderAbilityId.equals("cobblemon.ability.flashfire")
            ) {
                return 0;
            }
        } else if (attacker.equals(ElementalTypes.INSTANCE.getGRASS())) {
            if (defenderAbilityId.equals("cobblemon.ability.sapsipper")) {
                return 0;
            }
        }

        double typeEffectiveness = typeEffectiveness(attacker, defender);
        if (
                defenderAbilityId.equals("cobblemon.ability.wonderguard") &&
                        typeEffectiveness != SUPER_EFFECTIVE
        ) {
            return 0;
        }

        return typeEffectiveness;
    }
    @NotNull
    @Override
    public ShowdownActionResponse choose(@NotNull ActiveBattlePokemon activeBattlePokemon, @Nullable ShowdownMoveset moveset, boolean forceSwitch) {
        ModCommon.LOG.info("started showdown response.");
        ModCommon.LOG.info(Boolean.toString(forceSwitch));
        String currentHeldItem ="";
        ElementalType activePrimaryType = null;
        ElementalType activeSecondaryType = null;
        double activePokemonPercentHP = 0;
        Ability currentAbility = null;
        BattlePokemon battlePokemon = activeBattlePokemon.getBattlePokemon();
        if (battlePokemon != null) {
            if (battlePokemon.getHeldItemManager().showdownId(battlePokemon) != null) {
                currentHeldItem = battlePokemon.getHeldItemManager().showdownId(battlePokemon);
            }
            ModCommon.LOG.info(Integer.toString(battlePokemon.getEffectedPokemon().getAttack()) + " Attack");
            ModCommon.LOG.info(Integer.toString(battlePokemon.getEffectedPokemon().getSpecialAttack()) + " SpAttack");
            ModCommon.LOG.info(Integer.toString(battlePokemon.getEffectedPokemon().getSpeed()) + " speed");
            ModCommon.LOG.info(Integer.toString(battlePokemon.getEffectedPokemon().getSpecialDefence()) + " spDef");
            ModCommon.LOG.info(Integer.toString(battlePokemon.getEffectedPokemon().getDefence()) + " def");
            activePrimaryType = battlePokemon.getEffectedPokemon().getPrimaryType();
            activeSecondaryType = battlePokemon.getEffectedPokemon().getSecondaryType();
            activePokemonPercentHP = Math.ceil(battlePokemon.getHealth() / battlePokemon.getMaxHealth() * 100);
            currentAbility = battlePokemon.getOriginalPokemon().getAbility();
        }


        List<BattlePokemon> aliveParty = activeBattlePokemon.getActor().getPokemonList().stream()
                .filter(BattlePokemon::canBeSentOut)
                .toList();
        Optional<ActiveBattlePokemon> opponentActiveBattlePokemon = StreamSupport.stream(
                        activeBattlePokemon.getAllActivePokemon().spliterator(), false
                )
                .filter(abp -> !abp.isAllied(activeBattlePokemon))
                .findFirst();
        List<ActiveBattlePokemon> allNPCActiveBattlePokemon = StreamSupport.stream(
                        activeBattlePokemon.getAllActivePokemon().spliterator(), false
                )
                .filter(abp -> abp.isAllied(activeBattlePokemon))
                .toList();
        List<ActiveBattlePokemon> allOpponentActiveBattlePokemon = StreamSupport.stream(
                        activeBattlePokemon.getAllActivePokemon().spliterator(), false
                )
                .filter(abp -> !abp.isAllied(activeBattlePokemon))
                .toList();
        BattleFormat bf = new BattleFormat();
        //setting up logic for double calcs. works the same for singles anyways.


        int currentBattleSlot = allNPCActiveBattlePokemon.indexOf(activeBattlePokemon) != -1
                ? allNPCActiveBattlePokemon.indexOf(activeBattlePokemon) : 0;

        BattlePokemon opponent = allOpponentActiveBattlePokemon.isEmpty()
                ? null : allOpponentActiveBattlePokemon.get(currentBattleSlot).getBattlePokemon();

        if(bf.getBattleType().toString().equals("GEN_9_DOUBLES")){
            int partnerSlot = (currentBattleSlot == 0 ? 1 : 0);
            ActiveBattlePokemon opponentPartner = allOpponentActiveBattlePokemon.get(partnerSlot);
            ActiveBattlePokemon NPCPartner = allNPCActiveBattlePokemon.get(partnerSlot);
        }
        List<Move> oppMoves = new ArrayList<>();
        String opponentAbility = "";
        double oppPercentHP = 0;
        if(opponent != null){
            oppMoves = opponent.getMoveSet().getMoves();
            opponentAbility = opponent.getEffectedPokemon().getAbility().getDisplayName();
            oppPercentHP = Math.ceil(opponent.getHealth()/opponent.getMaxHealth() * 100);//this is rounded up
        }
        boolean isOHKO = false;
        //TODO: SWITCHING SCORING LOGIC STARTS HERE =======================================
        List<BattlePokemon> canSwitchTo = activeBattlePokemon.getActor().getPokemonList().stream()
                .filter(BattlePokemon::canBeSentOut)
                .toList();
        if (canSwitchTo.isEmpty()) return PassActionResponse.INSTANCE;
        Map<BattlePokemon, Integer> switchingScores = new HashMap<>();
        int switchScore = 0;
        boolean isSwitchMonFaster = false;
        boolean doesSwitchOHKO = false;
        boolean doesOppOHKO = false;

        for(BattlePokemon possibleSwitch : canSwitchTo){
            switchScore = 0;
            isSwitchMonFaster = possibleSwitch.getEffectedPokemon().getStat(Stats.SPEED) >= opponent.getEffectedPokemon().getStat(Stats.SPEED);
            doesSwitchOHKO = isOHKO(possibleSwitch.getMoveSet().getMoves(), possibleSwitch, opponent);
            doesOppOHKO = isOHKO(oppMoves, opponent, possibleSwitch);
            if(isSwitchMonFaster && doesSwitchOHKO){
                switchScore += 5;
            }
            //we are slower, we kill opp, opp does not kill us
            else if(!isSwitchMonFaster && !doesOppOHKO && doesSwitchOHKO){
                switchScore += 4;
            }
            // we are faster, deal more damage than we take. (dmg is the percent change not raw number)
            else if(isSwitchMonFaster && highestPercentDamageMove(possibleSwitch, opponent) > highestPercentDamageMove(opponent,possibleSwitch)){
                switchScore += 3;
            }
            else if(!isSwitchMonFaster && highestPercentDamageMove(possibleSwitch, opponent) > highestPercentDamageMove(opponent,possibleSwitch)){
                switchScore += 2;
            }
            else if(isSwitchMonFaster){
                switchScore += 1;
            }
            else if(!isSwitchMonFaster && doesOppOHKO){
                switchScore -=1;
            }
            if(possibleSwitch.getOriginalPokemon().getDisplayName().equals("ditto")){
                switchScore += 2;
            }
            if(isSwitchMonFaster && !doesOppOHKO){
                if(possibleSwitch.getOriginalPokemon().getDisplayName().equals("wynaut")
                    || possibleSwitch.getOriginalPokemon().getDisplayName().equals("wobbuffet")) {
                    switchScore += 2;
                }
            }
            switchingScores.put(possibleSwitch, switchScore);
            ModCommon.LOG.info(possibleSwitch.getOriginalPokemon().getDisplayName().toString() + "  " + Integer.toString(switchScore));
        }
        //filter who has the best switch score
        int maxSwitchingScore = switchingScores.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(Integer.MIN_VALUE);
        List<BattlePokemon> bestSwitches = switchingScores.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == maxSwitchingScore)
                .map(Map.Entry::getKey)
                .toList();
        BattlePokemon nextPokemon = null;
        if (!bestSwitches.isEmpty()) {
            if(bestSwitches.size() > 1){
                 nextPokemon = bestSwitches.getFirst();
            }
            else{
                 nextPokemon = bestSwitches.getFirst();
            }
        }
        if (forceSwitch || activeBattlePokemon.isGone()) {
            if (canSwitchTo.isEmpty()) return PassActionResponse.INSTANCE;
            if (opponentActiveBattlePokemon.isEmpty() || opponentActiveBattlePokemon.get().getBattlePokemon() == null) {
                nextPokemon = bestSwitches.get(RANDOM.nextInt(canSwitchTo.size()));
                nextPokemon.setWillBeSwitchedIn(true);
                return new SwitchActionResponse(nextPokemon.getUuid());
            }
            if (nextPokemon == null) {
                if (!canSwitchTo.isEmpty()) {
                    nextPokemon = canSwitchTo.get(RANDOM.nextInt(canSwitchTo.size()));
                } else {
                    return PassActionResponse.INSTANCE; // no Pokémon to switch to
                }
            }
            nextPokemon.setWillBeSwitchedIn(true);
            return new SwitchActionResponse(nextPokemon.getUuid());
        }


        if (moveset == null) return PassActionResponse.INSTANCE;
        if (moveset.moves.size() == 1 && moveset.moves.get(0).getId().equals("recharge")) {
            return new MoveActionResponse("recharge", null, null);
        }
        List<InBattleMove> inBattleMoves = moveset.moves.stream()
                .filter(InBattleMove::canBeUsed)
                .filter(inBattleMove -> {
                    List<Targetable> targetList = inBattleMove.getTarget().getTargetList().invoke(activeBattlePokemon);
                    return inBattleMove.mustBeUsed() || targetList == null || !targetList.isEmpty();
                }).toList();

        if (inBattleMoves.isEmpty()) return new MoveActionResponse("struggle", null, null);
        if (opponentActiveBattlePokemon.isEmpty()) {
            return new MoveActionResponse(
                    inBattleMoves.get(RANDOM.nextInt(moveset.moves.size())).id, null, null
            );
        }

        if (opponent == null) {
            return new MoveActionResponse(
                    inBattleMoves.get(RANDOM.nextInt(moveset.moves.size())).id, null, null
            );
        }

        Map<InBattleMove, Move> moveMap = new HashMap<>();
        IntStream.range(0, inBattleMoves.size())
                .forEach(i -> moveMap.put(
                        inBattleMoves.get(i),
                        Moves.INSTANCE.all().stream().filter(move -> move.getName().equals(inBattleMoves.get(i).getId()))
                                .findFirst().get().create()
                ));
        Map<InBattleMove, Integer> moveDamages = new HashMap<>();
        inBattleMoves.forEach(inBattleMove -> {
            int dmg = PokeMathMax.damage(
                    activeBattlePokemon.getBattlePokemon(),
                    opponent,
                    moveMap.get(inBattleMove)
            );
            moveDamages.put(inBattleMove, dmg);
            ModCommon.LOG.info(inBattleMove.getId() + "     DAMAGE = " + Integer.toString(dmg));
        });

        List<InBattleMove> killingMoves = new ArrayList<>();
        moveDamages.forEach((move, damage) -> {
            if (damage >= opponent.getHealth()) killingMoves.add(move);
        });
        Map<InBattleMove, Integer> moveScores = new HashMap<>();
        boolean isFaster = activeBattlePokemon.getBattlePokemon().getEffectedPokemon().getStat(Stats.SPEED) >= opponent.getEffectedPokemon().getStat(Stats.SPEED);
        boolean npcIsOHKO = isOHKO(oppMoves, opponent, activeBattlePokemon.getBattlePokemon());
        boolean npcIs2OHKO = is2HKO(oppMoves,opponent,activeBattlePokemon.getBattlePokemon());

        //making list of highestest dmg nonkilling moves, and adding special cases.
        List<InBattleMove> nonKillingPossibleMoves = new ArrayList<>();
        int maxDamage = 0;
        if (killingMoves.isEmpty()) {
                /*nonKillingPossibleMoves = moveDamages.values().stream().filter
                        (high -> high.equals(Collections.max(moveDamages.entrySet(),
                                Map.Entry.comparingByValue()).getKey())).toList());
                */
            for (int dmg : moveDamages.values()) {
                maxDamage = maxDamage >= dmg ? maxDamage : dmg;

            }
            ModCommon.LOG.info("This is the max damage potential    "+ Integer.toString(maxDamage));
            for (Map.Entry<InBattleMove, Integer> entry : moveDamages.entrySet()) {
                if (entry.getValue() == maxDamage) {
                    ModCommon.LOG.info("NonPossibleKill Max Damage Move     " + entry.getKey().getId());
                    nonKillingPossibleMoves.add(entry.getKey());
                } else if (trapMoves.contains(entry.getKey().getId())
                        || physicalAttackReductionMoves.contains(entry.getValue())
                        || specialAttackReductionMoves.contains(entry.getKey())
                        || speedReductionMoves.contains(entry.getValue())) {
                    ModCommon.LOG.info("NonPossibleKill Max Damage Move (Special Case)    " + entry.getKey().getId());
                    nonKillingPossibleMoves.add(entry.getKey());
                }

            }
        }
        //TODO: START OF THE MOVE DAMAGE SCORING CALCULATIONS
        for (var move : moveDamages.entrySet()) {
            InBattleMove currentMove = move.getKey();
            int score = 0;
            //int dmg = PokeMathMax.damage(activeBattlePokemon.getBattlePokemon(), opponent, currentMove);
            double typeEffectiveness1 = typeEffectiveness(TypeChart.getMove(currentMove).getType(),
                    opponent.getEffectedPokemon().getPrimaryType(), opponent.getEffectedPokemon().getAbility());
            double typeEffectiveness2 = typeEffectiveness(TypeChart.getMove(currentMove).getType(),
                    opponent.getEffectedPokemon().getSecondaryType(), opponent.getEffectedPokemon().getAbility());
            double typeEffectivenessResult = typeEffectiveness1 * typeEffectiveness2;

            //useless move because of abilities
            if (typeEffectivenessResult == 0) {
                moveScores.put(currentMove, -20);
                continue;
            }

            //if the damage move is in the killing list.
            if (killingMoves.contains(currentMove)) {
                double roll = RANDOM.nextDouble();
                score = (roll > 0.2) ? 6 : 8;

                if (isFaster) {
                    //We are faster or speed tied and we see a kill with this move. (+12 (80%), +14 (20%))
                    score += 6;
                }
                //if we are slower than the opponent and we see a kill with priority. (+6)
                else {
                    if (priorityDamageMoves.contains(currentMove.id)) {
                        score += 6;
                    }
                    //if we are slower and see a kill without priority
                    else {
                        score += 3;
                    }
                }
                if(currentMove.getId().equals("futuresight")){
                    score+= isFaster ? 8:6;
                }
                //todo: when meloetta uses relic song she transforms to pirouette form.
                // (we want to keep track of if she has used relic song already)
            }
            //highest damaging move, speed reduction moves, ATK/SpATK reduction moves, Trap Moves (6 , 8)
            //if the move is a non-killing move.
            if (!nonKillingPossibleMoves.isEmpty()) {
                for (InBattleMove nonKillingMove : nonKillingPossibleMoves) {
                    double roll = RANDOM.nextDouble();

                    if (trapMoves.contains(nonKillingMove)) {
                        score += (roll > 0.2) ? 6 : 8;
                    }
                    //if the move is a speed reducing move.
                    if (speedReductionMoves.contains(nonKillingMove)) {
                        if (moveScores.get(nonKillingMove) == maxDamage) {
                            roll = RANDOM.nextDouble();
                            score += (roll > 0.2) ? 6 : 8;
                        }
                        //the ai is not faster and the enemy mon can be reduced.
                        else {
                            score += !ignoreStatDropAbilities.contains(opponentAbility) && !isFaster ?
                                     6 : 5;
                        }

                    }

                    if (physicalAttackReductionMoves.contains(nonKillingMove) || specialAttackReductionMoves.contains(nonKillingMove)) {
                        boolean hasSpecialMove = false;
                        boolean hasPhysicalMove = false;
                        String damageCategory ="";
                        //String currentMoveCategory = "";
                        for(Move opponentMove : oppMoves){
                            damageCategory = opponentMove.getDamageCategory().getName();
                            if(damageCategory.equals(DamageCategories.INSTANCE.getPHYSICAL().getName())){
                                hasPhysicalMove = true;
                            }
                            if(damageCategory.equals(DamageCategories.INSTANCE.getSPECIAL().getName())){
                                hasSpecialMove = true;
                            }
                        }
                        //currentMoveCategory = TypeChart.getMove(nonKillingMove).getDamageCategory().getName();
                        if (moveScores.get(nonKillingMove) == maxDamage) {
                            roll = RANDOM.nextDouble();
                            score += (roll > 0.2) ? 6 : 8;
                        } else if(!ignoreStatDropAbilities.contains(opponentAbility)){
                            if(specialAttackReductionMoves.contains(nonKillingMove) && hasSpecialMove){
                                score += 6;
                            }
                            else if(physicalAttackReductionMoves.contains(nonKillingMove) && hasPhysicalMove){
                                score+=6;
                            }
                        }
                        else if(ignoreStatDropAbilities.contains(opponentAbility)){
                            score+=5;
                        }
                    }
                    //if contains in special list then do special functionality bellow.
                    //=============================================================================================
                    if (specialFunctionMoves.contains(nonKillingMove)) {
                        switch (nonKillingMove.getId()){
                            case "futuresight":
                                // If AI is faster than target and is KO’d by target
                                score += isFaster && isOHKO ? 8 : 6;
                                break;
                            case "relicsong":
                                // If in Meloetta base form
                                score += activeBattlePokemon.getBattlePokemon().getName().equals("meloetta") ? 10 : 0;
                                break;
                            case "suckerpunch":
                                //score += activeBattlePokemon.getBattlePokemon().getEffectedPokemon().getDisplayName().equals("meloetta") ? 10 : 0;
                                break;
                            case "pursuit":
                                if(killingMoves.contains(nonKillingMove)){
                                    score+=10;
                                }
                                else{
                                    if(oppPercentHP <= 20){
                                        score+=10;
                                    }
                                    else if(oppPercentHP <= 40){
                                        roll = RANDOM.nextDouble();
                                        score += roll < .5 ? 8 : 0;
                                    }
                                }
                                break;
                            case "fellstinger":
                                roll = RANDOM.nextDouble();
                                int result1 = roll > .8 ? 23 : 21;
                                int result2 = roll > .8 ? 17 : 15;

                                if(activeBattlePokemon.getBattlePokemon().getStatChanges().get(Stats.ATTACK) != 6){
                                    score += isFaster ? result1 : result2;
                                }
                                break;
                            case "rollout":
                                score += 7;
                                break;
                            case "stealthrock":
                                //todo : figure out how to find out turn number. (keep track of all hazards on field)
                                //if first turn out\
                                if(stealthRocks(opponent) !=0){
                                    score -=20;
                                }
                                //else
                                break;
                            case "spikes":
                                //todo: Note: If at least 1 of the corresponding spikes is up already, score is lowered by 1 always
                                roll = RANDOM.nextDouble();
                                int spikeR1 = roll > .75 ? 8 : 9;
                                int spikeR2 = roll > .75 ? 6 : 7;
                                //first turn out ?

                                if(spikesCount(opponent) == 3){
                                    score -=50;
                                }
                                else if(spikesCount(opponent) > 0){
                                    score --;
                                }
                                break;
                            case "toxicspikes":
                                //todo: Note: If at least 1 of the corresponding spikes is up already, score is lowered by 1 always
                                roll = RANDOM.nextDouble();
                                int toxicspikesR1 = roll > .75 ? 8 : 9;
                                int toxicspikesR2 = roll > .75 ? 6 : 7;
                                //first turn out ?
                                if(toxicSpikesCount(opponent) == 3){
                                    score -=50;
                                }
                                else if(toxicSpikesCount(opponent) > 0){
                                    score --;
                                }
                                break;
                            case "stickyweb":
                                //todo: Note: If at least 1 of the corresponding spikes is up already, score is lowered by 1 always
                                roll = RANDOM.nextDouble();
                                int stickywebR1 = roll > .75 ? 9 : 12;
                                int stickywebR2 = roll > .75 ? 6 : 9;
                                //first turn out ?
                                if(stickyWebCount(opponent) !=0){
                                    score -=20;
                                }
                                break;
                            case "protect":
                                score += 6;
                                if(activeBattlePokemon.getBattlePokemon().getEffectedPokemon().getStatus() != null){
                                    score-=2;
                                }
                                if(opponent.getEffectedPokemon().getStatus() != null){
                                    score++;
                                }
                                // todo: If it's AI mon's first turn out and it is not a double battle:
                                //todo : keep track of things that happened last turn and 2 turns ago.
                                break;
                            case "kingsshield":
                                score += 6;
                                //todo: still needs to see parish song
                                if(BattleEffects.Pokemon.Volatile.cursed(activeBattlePokemon.getBattlePokemon())
                                || BattleEffects.Pokemon.Volatile.yawn(activeBattlePokemon.getBattlePokemon())
                                || BattleEffects.Pokemon.Volatile.leech(activeBattlePokemon.getBattlePokemon())
                                || BattleEffects.Pokemon.Volatile.attract(activeBattlePokemon.getBattlePokemon())
                                || BattleEffects.Pokemon.Status.brn(activeBattlePokemon.getBattlePokemon())
                                || BattleEffects.Pokemon.Status.psn(activeBattlePokemon.getBattlePokemon())
                                ||BattleEffects.Pokemon.Status.tox(activeBattlePokemon.getBattlePokemon())){
                                    if(activePokemonPercentHP <= 25){
                                        score -=20;
                                    }
                                    score-=2;
                                }
                                if(opponent.getEffectedPokemon().getStatus() != null){
                                    score++;
                                }
                                if(BattleEffects.Field.Weather.sandstorm(activeBattlePokemon.getBattlePokemon())){
                                    if(!activePrimaryType.toString().equals("ROCK")
                                            &&!activePrimaryType.toString().equals("GROUND")
                                            &&!activePrimaryType.toString().equals("STEEL")
                                            &&!activeSecondaryType.toString().equals("ROCK")
                                            &&!activeSecondaryType.toString().equals("GROUND")
                                            &&!activeSecondaryType.toString().equals("STEEL")){
                                        if(activePokemonPercentHP <= 8){
                                            score-=20;
                                        }

                                    }
                                }
                                // todo: If it's AI mon's first turn out and it is not a double battle:
                                //todo : keep track of things that happened last turn and 2 turns ago.
                                break;
                            case "fling":
                                if(currentHeldItem != null){
                                    break;
                                }
                                double flingEffectiveness = typeEffectiveness(TypeChart.getMove(nonKillingMove).getType(),opponent.getEffectedPokemon().getPrimaryType())
                                        * typeEffectiveness(TypeChart.getMove(nonKillingMove).getType(),opponent.getEffectedPokemon().getSecondaryType());
                                //need to hold a salac berry and fling is not super effective.
                                if(activeBattlePokemon.getBattlePokemon().getEffectedPokemon().heldItem().is(CobblemonItems.SALAC_BERRY)
                                        && (flingEffectiveness <=1)){
                                    score+=9;
                                }
                                //todo: flame orb toxic orb AI??
                                break;
                                //todo: Shadow Sneak, Aqua Jet, Ice Shard: doubles
                            case "roleplay"://todo doubles
                                break;
                            case "magnitude"://todo doubles
                                break;
                            case "earthquake"://todo doubles
                                break;
                            case "imprison":
                                int commonMoves = 0;
                                for(InBattleMove imprisonSet : moveDamages.keySet()){
                                    if(oppMoves.contains(TypeChart.getMove(imprisonSet)))
                                    {
                                        commonMoves ++;
                                    }
                                }
                                score+= commonMoves > 0 ? 9 : -20;
                                break;
                            case "batonpass":
                                if (aliveParty.isEmpty()){
                                    score-= 20;
                                    break;
                                }
                                var statChanges = activeBattlePokemon.getBattlePokemon().getStatChanges();
                                var anyPositive = false;
                                for(Integer value : statChanges.values()){
                                    if(value > 0){
                                        anyPositive = true;
                                    }
                                }
                                //todo: find out when substitute is active.
                                if(!aliveParty.isEmpty() && anyPositive){
                                    score += 14;
                                }
                                break;
                            case "tailwind":
                                if(isPartySlowerThanOpponent(allNPCActiveBattlePokemon,allOpponentActiveBattlePokemon)){
                                    score+=9;
                                }
                                else{
                                    score+=5;
                                }

                                break;
                            case "trickroom":
                                if(isPartySlowerThanOpponent(allNPCActiveBattlePokemon,allOpponentActiveBattlePokemon)){
                                    score+=10;
                                }
                                else{
                                    score+=5;
                                }
                                if(BattleEffects.Field.Room.trickroom(activeBattlePokemon.getBattlePokemon())){
                                    score-=20;
                                }
                                break;
                            case "fakeout":
                                //todo : first turn of mon or not
                                if(opponentAbility.equals("shielddust") || opponentAbility.equals("innerfocus")){
                                    score += 9;
                                }
                                break;
                            case "helpinghand":
                                //todo:AI will not use either of these moves if their partner is also using this move, or their partner is
                                //   using a Status move
                                if(currentBattleSlot == 1){
                                    //we are looking at our partners move choice

                                }
                                break;
                            case "finalgambit":

                                break;
                        }
                    }
                }
            }
            //list of all opponents moves and their OHKO potential.
            if (priorityDamageMoves.contains(move) && !isFaster && isOHKO) {
                score = 11;
            }
            //if our pokemon has a special ability give its score +1
            if (abilityStatBooster.contains(activeBattlePokemon.getBattlePokemon().getOriginalPokemon().getAbility())) {
                score++;
            }
            //if a damaging move has a high crit chance and is Super Effective on the target
            // (50% of the time the score gets increased by 1)
            if (highCriticalMoves.contains(currentMove) && (typeEffectivenessResult == 2 || typeEffectivenessResult == 4)) {
                double roll = RANDOM.nextDouble();
                score = (roll < .5) ? score + 1 : score;
            }
            if (moveDamages.containsKey("acidspray")) {
                score += 6;
            }
            //This puts the final score into the map with its move key.
            if(nonKillingPossibleMoves.contains(move.getKey())){
                double roll = RANDOM.nextDouble();
                score += roll > .2 ? 6:8;
                ModCommon.LOG.info("NONKILLING MOVE " + move.getKey().getId() + "  " + Integer.toString(score));
            }
            moveScores.put(currentMove, score);
            ModCommon.LOG.info(currentMove.getId() + "  " + Integer.toString(score));
        }
        // END OF SCORING LOGIC::START OF SWITCH AI LOGIC
        if(isSwitching(moveScores, aliveParty, activeBattlePokemon.getBattlePokemon(), opponent)){
            double flip = RANDOM.nextDouble();
            boolean result = (flip > .5);
            ModCommon.LOG.info(Boolean.toString(result) + "    coin toss result");
            if(result){
                nextPokemon.setWillBeSwitchedIn(true);
                ModCommon.LOG.info("SWITCHING INTO NEXT MON");
                return new SwitchActionResponse(nextPokemon.getUuid());
                //start switching logic
            }
        }
        else{
            ModCommon.LOG.info("NO REASON TO SWITCH THIS MON");
        }
        int maxScore = moveScores.values()
                .stream()
                .max(Integer::compareTo)
                .orElse(Integer.MIN_VALUE);
        List<InBattleMove> bestMoves = moveScores.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == maxScore)
                .map(Map.Entry::getKey)
                .toList();


        //if there are multiple best moves
        if(bestMoves.size() > 1){
            int randomInt = RANDOM.nextInt(bestMoves.size());
            var bestMove = bestMoves.get(randomInt);
            ModCommon.LOG.info("CHOOSEN BEST MOVE  " + bestMove.getId());
            List<Targetable> targets = bestMove.mustBeUsed() ? null : bestMove.getTarget().getTargetList().invoke(activeBattlePokemon);
            return new MoveActionResponse(bestMove.getId(),
                    targets == null ? null : opponentActiveBattlePokemon.get().getPNX(),
                    null);
        }
        List<Targetable> targets = bestMoves.get(0).mustBeUsed() ? null : bestMoves.get(0).getTarget().getTargetList().invoke(activeBattlePokemon);
        ModCommon.LOG.info("CHOOSEN BEST MOVE  " + bestMoves.get(0).getId());
        return new MoveActionResponse(bestMoves.get(0).getId(),
                targets == null ? null : opponentActiveBattlePokemon.get().getPNX(),
                null);
    }
    public static int spikesCount(BattlePokemon pkmn) {
        return BattleEffects.Side.Hazard.spikes(pkmn);
    }
    public static int toxicSpikesCount(BattlePokemon pkmn) {
        return BattleEffects.Side.Hazard.toxicspikes(pkmn);
    }
    public static int stealthRocks(BattlePokemon pkmn) {
        return BattleEffects.Side.Hazard.stealthrock(pkmn);
    }
    public static int stickyWebCount(BattlePokemon pkmn) {
        return BattleEffects.Side.Hazard.stickyweb(pkmn);
    }
    public static int getSpeedStat(ActiveBattlePokemon pkmn){
        return  pkmn.getBattlePokemon().getEffectedPokemon().getStat(Stats.SPEED);
    }
    public static boolean isOHKO(List<Move> moves, BattlePokemon attacker, BattlePokemon defender){
        int enemyDamage = 0;
        int currentHP = defender.getHealth();
        boolean result = false;
        for (Move currentMove : moves) {
            //activeBattlePokemon.getBattlePokemon().getOriginalPokemon().getPrimaryType();
            enemyDamage = PokeMathMax.damage(attacker, defender, currentMove);
            if (enemyDamage >= currentHP) {
                result = true;
                if(currentHP == defender.getMaxHealth()
                        && defender.getEffectedPokemon().getAbility().getDisplayName().equals("sturdy")){
                    result = false;
                }
                if(defender.getHeldItemManager().showdownId(defender) != null){
                    if(currentHP == defender.getMaxHealth()
                            && defender.getHeldItemManager().showdownId(defender).equals("focus_sash")){
                        result = false;
                    }
                }
                if(result){
                    return true;
                }
            }
        }
        return result;
    }
    public static boolean is2HKO(List<Move> moves, BattlePokemon attacker, BattlePokemon defender) {
        int enemyDamage = 0;
        int currentHP = defender.getHealth();
        for (Move currentMove : moves) {
            //activeBattlePokemon.getBattlePokemon().getOriginalPokemon().getPrimaryType();
            enemyDamage = PokeMathMax.damage(attacker, defender, currentMove);
            if (enemyDamage * 2 >= currentHP) {
                return true;
            }
        }
        return false;
    }
    public static boolean isPartySlowerThanOpponent(List<ActiveBattlePokemon> NPC, List<ActiveBattlePokemon> OPP){
        //get the slowest mon from each team and compare
        int slowestNPC = Math.min(getSpeedStat(NPC.getFirst()) , getSpeedStat(NPC.getLast()));
        int slowestOPP = Math.min(getSpeedStat(OPP.getFirst()) , getSpeedStat(OPP.getLast()));
        if(slowestOPP > slowestNPC){
            return true;
        }
        return false;
    }
    public static boolean isPartyFasterThanOpponent(List<ActiveBattlePokemon> NPC, List<ActiveBattlePokemon> OPP){
        //get the slowest mon from each team and compare
        int fastestNPC = Math.max(getSpeedStat(NPC.getFirst()) , getSpeedStat(NPC.getLast()));
        int fastestOPP = Math.max(getSpeedStat(OPP.getFirst()) , getSpeedStat(OPP.getLast()));
        if(fastestOPP < fastestNPC){
            return true;
        }
        return false;
    }
    public static boolean isSwitching(Map<InBattleMove, Integer> moveScore, List<BattlePokemon> party,BattlePokemon self, BattlePokemon opponent){
        List<Integer> scores = new ArrayList<>();
        boolean isSecondCondition = false;
        boolean isThirdCondition = false;
        for(int val : moveScore.values()){
            scores.add(val);
        }
        boolean hasLowScore = scores.stream().allMatch(s -> s <=-5);
        ModCommon.LOG.info(Boolean.toString(hasLowScore));
        if(Math.ceil(self.getHealth()/self.getMaxHealth()) * 100 <= 50){
            return false;
        }
        for(BattlePokemon pokemon : party){
            //TODO: ((if mon is faster than opp, and not OHKO) || (if mon is slower and not 2OHKO)) && not below 50% hp
            if(pokemon.getEffectedPokemon().getStat(Stats.SPEED) >= opponent.getEffectedPokemon().getStat(Stats.SPEED)
                && !isOHKO(opponent.getMoveSet().getMoves(), opponent, pokemon)){
                isSecondCondition = true;
            }
            if(pokemon.getEffectedPokemon().getStat(Stats.SPEED) < opponent.getEffectedPokemon().getStat(Stats.SPEED)
                    && !is2HKO(opponent.getMoveSet().getMoves(), opponent, pokemon)){
                isThirdCondition = true;
            }
        }
        return isSecondCondition && isThirdCondition && hasLowScore;
    }
    public static double highestPercentDamageMove(BattlePokemon attacker, BattlePokemon defender){
        List<Move> attackerMoves = attacker.getMoveSet().getMoves();
        double highestPercent = 0;
        double currentCalc = 0;
        for(Move move : attackerMoves){
            //TODO: damage delt divided by map hp.
            currentCalc = Math.ceil(PokeMathMax.damage(attacker,defender,move) / defender.getMaxHealth());
            highestPercent = currentCalc > highestPercent ? currentCalc : highestPercent;
        }
        return highestPercent;
    }
    public static void initialiseTypeChart() {
        ElementalTypes types = ElementalTypes.INSTANCE;
        ElementalType NORMAL = types.getNORMAL();
        ElementalType FIGHTING = types.getFIGHTING();
        ElementalType FLYING = types.getFLYING();
        ElementalType POISON = types.getPOISON();
        ElementalType GROUND = types.getGROUND();
        ElementalType ROCK = types.getROCK();
        ElementalType BUG = types.getBUG();
        ElementalType GHOST = types.getGHOST();
        ElementalType STEEL = types.getSTEEL();
        ElementalType FIRE = types.getFIRE();
        ElementalType WATER = types.getWATER();
        ElementalType GRASS = types.getGRASS();
        ElementalType ELECTRIC = types.getELECTRIC();
        ElementalType PSYCHIC = types.getPSYCHIC();
        ElementalType ICE = types.getICE();
        ElementalType DRAGON = types.getDRAGON();
        ElementalType DARK = types.getDARK();
        ElementalType FAIRY = types.getFAIRY();

        typeChart.put(NORMAL, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                GHOST, IMMUNE
        ));
        typeChart.put(FIGHTING, Map.of(
                FLYING, SUPER_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                PSYCHIC, SUPER_EFFECTIVE,
                DARK, NOT_VERY_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE
        ));
        typeChart.put(FLYING, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                GROUND, IMMUNE,
                ROCK, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, SUPER_EFFECTIVE,
                ICE, SUPER_EFFECTIVE
        ));
        typeChart.put(POISON, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                POISON, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                PSYCHIC, SUPER_EFFECTIVE,
                FAIRY, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(GROUND, Map.of(
                POISON, NOT_VERY_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE,
                ELECTRIC, IMMUNE,
                ICE, SUPER_EFFECTIVE
        ));
        typeChart.put(ROCK, Map.of(
                NORMAL, NOT_VERY_EFFECTIVE,
                FIGHTING, SUPER_EFFECTIVE,
                FLYING, NOT_VERY_EFFECTIVE,
                POISON, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE
        ));
        typeChart.put(BUG, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                FLYING, SUPER_EFFECTIVE,
                GROUND, NOT_VERY_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(GHOST, Map.of(
                NORMAL, IMMUNE,
                FIGHTING, IMMUNE,
                POISON, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                GHOST, SUPER_EFFECTIVE,
                DARK, SUPER_EFFECTIVE
        ));
        Map<ElementalType, Double> steelMap = new HashMap<>(Map.of(
                NORMAL, NOT_VERY_EFFECTIVE,
                FIGHTING, SUPER_EFFECTIVE,
                FLYING, NOT_VERY_EFFECTIVE,
                POISON, IMMUNE,
                GROUND, SUPER_EFFECTIVE,
                ROCK, NOT_VERY_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE
        ));
        steelMap.put(PSYCHIC, NOT_VERY_EFFECTIVE);
        steelMap.put(ICE, NOT_VERY_EFFECTIVE);
        steelMap.put(DRAGON, NOT_VERY_EFFECTIVE);
        steelMap.put(FAIRY, NOT_VERY_EFFECTIVE);
        typeChart.put(STEEL, steelMap);
        typeChart.put(FIRE, Map.of(
                GROUND, SUPER_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, SUPER_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE,
                FAIRY, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(WATER, Map.of(
                STEEL, NOT_VERY_EFFECTIVE,
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, SUPER_EFFECTIVE,
                ELECTRIC, SUPER_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(GRASS, Map.of(
                FLYING, SUPER_EFFECTIVE,
                POISON, SUPER_EFFECTIVE,
                GROUND, NOT_VERY_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE,
                ICE, SUPER_EFFECTIVE
        ));
        typeChart.put(ELECTRIC, Map.of(
                FLYING, NOT_VERY_EFFECTIVE,
                GROUND, SUPER_EFFECTIVE,
                STEEL, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(PSYCHIC, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                GHOST, SUPER_EFFECTIVE,
                PSYCHIC, NOT_VERY_EFFECTIVE,
                DARK, SUPER_EFFECTIVE
        ));
        typeChart.put(ICE, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                ROCK, SUPER_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                FIRE, SUPER_EFFECTIVE,
                ICE, NOT_VERY_EFFECTIVE
        ));
        typeChart.put(DRAGON, Map.of(
                FIRE, NOT_VERY_EFFECTIVE,
                WATER, NOT_VERY_EFFECTIVE,
                GRASS, NOT_VERY_EFFECTIVE,
                ELECTRIC, NOT_VERY_EFFECTIVE,
                ICE, SUPER_EFFECTIVE,
                DRAGON, SUPER_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE
        ));
        typeChart.put(DARK, Map.of(
                FIGHTING, SUPER_EFFECTIVE,
                BUG, SUPER_EFFECTIVE,
                GHOST, NOT_VERY_EFFECTIVE,
                PSYCHIC, IMMUNE,
                DARK, NOT_VERY_EFFECTIVE,
                FAIRY, SUPER_EFFECTIVE
        ));
        typeChart.put(FAIRY, Map.of(
                FIGHTING, NOT_VERY_EFFECTIVE,
                POISON, SUPER_EFFECTIVE,
                BUG, NOT_VERY_EFFECTIVE,
                STEEL, SUPER_EFFECTIVE,
                DRAGON, IMMUNE,
                DARK, NOT_VERY_EFFECTIVE
        ));
    }
}
