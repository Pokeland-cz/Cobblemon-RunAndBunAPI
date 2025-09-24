package com.gitlab.srcmc.rctapi.api.ai.utils;

import java.util.ArrayList;
import java.util.List;

public class RBMoveList {
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
    private static final List<String> generalSetupMoves = new ArrayList<>(List.of("poweruppunch",
            "swordsdance",
            "howl",
            "stuffcheeks",
            "barrier",
            "acidarmor",
            "irondefense",
            "cottonguard",
            "chargebeam",
            "tailglow",
            "nastyplot",
            "cosmicpower",
            "bulkup",
            "calmmind",
            "dragondance",
            "coil",
            "honeclaws",
            "quiverdance",
            "shiftgear",
            "shellsmash",
            "growth",
            "workup",
            "curse",
            "coil",
            "noretreat",
            "tidyup",
            "geomancy"));
    private static final List<String> ignoreStatDropAbilities = new ArrayList<>(List.of(
            "cobblemon.ability.contrary",
            "cobblemon.ability.clearbody",
            "cobblemon.ability.whitesmoke"));
    private static final List<String> specialFunctionMoves = new ArrayList<>(List.of("futuresight",
            "spikyshield",
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
            "sleeptalk",
            "hypnosis",
            "lovelykiss",
            "sleeppowder",
            "spore",
            "poisongas",
            "poisonpoder",
            "toxic"));

    private static final List<String> soundMoves = new ArrayList<>(List.of ("alluringvoice",
            "boomburst",
            "bugbuzz",
            "chatter",
            "clangingscales",
            "clangoroussoul",
            "clangoroussoulblaze",
            "confide",
            "disarmingvoice",
            "echoedvoice",
            "eeriespell",
            "grasswhistle",
            "growl",
            "healbell",
            "howl",
            "hypervoice",
            "metalsound",
            "nobleroar",
            "overdrive",
            "partingshot",
            "perishsong",
            "psychicnoise",
            "relicsong",
            "roar",
            "round",
            "screech",
            "shadowpanic",
            "sing",
            "snarl",
            "snore",
            "sparklingaria",
            "supersonic",
            "torchsong",
            "uproar"
    ));

    private static final List<String> flinchMoves = new ArrayList<>(List.of ("airslash",
            "astonish",
            "bite",
            "boneclub",
            "darkpulse",
            "doubleironbash",
            "dragonrush",
            "extrasensory",
            "fierywrath",
            "firefang",
            "floatyfall",
            "headbutt",
            "heartstamp",
            "hyperfang",
            "icefang",
            "iciclecrash",
            "ironhead",
            "mountaingale",
            "needlearm",
            "rockslide",
            "rollingkick",
            "skyattack",
            "steamroller",
            "stomp",
            "thunderfang",
            "triplearrows",
            "twister",
            "waterfall",
            "zenheadbutt",
            "zingzap"
    ));

    private static final List<String> thawingMoves = new ArrayList<>(List.of ("burnup",
            "flamewheel",
            "flareblitz",
            "fusionflare",
            "matchagatcha",
            "pyroball",
            "sacredfire",
            "scald",
            "scorchingsands",
            "steameruption"
    ));

    private static final List<String> rechargeMoves = new ArrayList<>(List.of (
            "blastburn",
            "eternabeam",
            "frenzyplant",
            "gigaimpact",
            "hydrocannon",
            "hyperbeam",
            "meteorassault",
            "prismaticlaser",
            "roaroftime",
            "rockwrecker",
            "shadowhalf"
    ));
    private static final List<String> recoveryMoves = new ArrayList<>(List.of (
            "roost",
            "slackoff",
            "healorder",
            "recover",
            "strengthsap",
            "morningsun",
            "synthesis",
            "moonlight",
            "rest",
            "junglehealing",
            "lifedew",
            "softboiled"));
    //TODO capitalize this list
    private static final List<String> megaStones = new ArrayList<>(List.of ("Absolite",
            "Aerodactylite",
            "Aggronite",
            "Alakazite",
            "Altarianite",
            "Ampharosite",
            "Audinite",
            "Banettite",
            "Beedrillite",
            "Blastoisinite",
            "Blazikenite",
            "Cameruptite",
            "Charizarditex",
            "Charizarditey",
            "Diancite",
            "Gardevoirite",
            "Galladite",
            "Gyaradosite",
            "Garchompite",
            "Gengarite",
            "Heracronite",
            "Houndoominite",
            "Lopunnite",
            "Lucarionite",
            "Latiasite",
            "Latiosite",
            "Manectite",
            "Mawilite",
            "Metagrossite",
            "Pinsirite",
            "Sablenite",
            "Salamencite",
            "Sharpedonite",
            "Slowbronite",
            "Steelixite",
            "Swampertite",
            "Sceptilite",
            "Scizorite",
            "Tyranitarite",
            "Venusaurite",
            "Mewtwonitex",
            "Mewtwonitey"));
    private static final List<String> ignoreDamageMoves = new ArrayList<>(List.of ("explosion",
            "selfdestruct",
            "mistyexplosion",
            "rollout",
            "meteorbeam",
            "finalgambit",
            "relicsong",
            "futuresight"
    ));
    private static final List<String> ignoreSleepAbilities = new ArrayList<>(List.of ("comatose",
            "insomnia",
            "vitalspirit",
            "sweetveil",
            "purifyingsalt",
            "goodasgold"
            ));
    public static List<String> getPriorityDamageMoves(){
        return priorityDamageMoves;
    }
    public static List<String> getAbilityStatBooster(){
        return abilityStatBooster;
    }
    public static List<String> getHighCriticalMoves(){
        return highCriticalMoves;
    }
    public static List<String> getTrapMoves(){
        return trapMoves;
    }
    public static List<String> getSpeedReductionMoves(){
        return speedReductionMoves;
    }
    public static List<String> getPhysicalAttackReductionMoves(){
        return physicalAttackReductionMoves;
    }
    public static List<String> getSpecialAttackReductionMoves(){
        return specialAttackReductionMoves;
    }
    public static List<String> getGeneralSetupMoves(){
        return generalSetupMoves;
    }
    public static List<String> getIgnoreStatDropAbilities(){
        return ignoreStatDropAbilities;
    }
    public static List<String> getSpecialFunctionMoves(){
        return specialFunctionMoves;
    }
    public static List<String> getSoundMoves(){
        return soundMoves;
    }
    public static List<String> getFlinchMoves(){
        return flinchMoves;
    }
    public static List<String> getThawingMoves(){
        return thawingMoves;
    }
    public static List<String> getRechargeMoves(){
        return rechargeMoves;
    }
    public static List<String> getRecoveryMoves(){
        return recoveryMoves;
    }
    public static List<String> getMegaStones(){
        return megaStones;
    }
    public static List<String> getIgnoreDamageMoves(){
        return ignoreDamageMoves;
    }
    public static List<String> getIgnoreSleepAbilities(){
        return ignoreSleepAbilities;
    }
}
