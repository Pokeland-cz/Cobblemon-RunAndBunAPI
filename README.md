# Radical Cobblemon Trainers - API

Trainer management and battle API for [Cobblemon](https://cobblemon.com/en).

This API was developed as foundation for the [Radical Cobblemon Trainers](https://gitlab.com/srcmc/rct/mod) mod and as replacement for the discontinued dependency [CobblemonTrainers](https://github.com/davo899/CobblemonTrainers) for Minecraft >= `1.21` and Cobblemon >= `1.6`.

Yet it is designed as independent library to provide a subset of similar features for everyone to use.

## Key features

- Trainer management and entity association (trainer registry)
- Support for different battle formats (1v1 SINGLE, 1v1 DOUBLE, 2v2 MULTI, ...)
- Custom battle rules (e.g. max item usages per battle)
- Extended AI features (currently only supported by `RCTBattleAI`):
  - Usual battle activities (move selection, switch, ...)
  - Gimmicks: Mega evolve, Dynamax, Z-Moves, Terastallize, etc. (most gimmicks that are activated by held items will require another mod to provide them. [Mega Showdown](https://modrinth.com/mod/cobblemon-mega-showdown) would be an example and is also required for dyna- and gmax)
  - Trainers can carry and use items
- Trainer and pokemon models (pojos) for easy parsing
  - Converters to Cobblemon types
  - Model validation (collects all errors before an exception is thrown)

## Example

Following [ExampleMod](common/src/main/java/com/gitlab/srcmc/rctapi/example/ExampleMod.java) provides a *common* implementation using Architectury:

```java
public class ExampleMod {
    private static final String MOD_ID = "example_mod";

    private static String fileToId(File file) {
        var name = file.getName().toLowerCase().trim();
        var i = name.lastIndexOf('.');
        return (i < 0 ? name : name.substring(0, i)).replace(' ', '_');
    }

    // Our own instance of the service.
    private static final RCTApi RCT = RCTApi.initInstance(MOD_ID);

    // Using a gson builder provided by the api is necessary.
    private static final Gson GSON = RCT.gsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    // Call this in the common setup phase of the mod. E.g. in onInitialize() of your
    // ModInitializer on Fabric or in the constructor of your @Mod annotated class on
    // Neoforge.
    public static void init() {
        ExampleMod.registerEvents();
    }

    static void registerEvents() {
        // A server instance is required to initialize a TrainerRegistry hence this is the
        // earliest possible point to register trainers (see below).
        LifecycleEvent.SERVER_STARTING.register(ExampleMod::onServerStarting);

        // We can easily (un)register players as trainers whenever they log in or out.
        PlayerEvent.PLAYER_JOIN.register(ExampleMod::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(ExampleMod::onPlayerQuit);
    }

    static void onServerStarting(MinecraftServer server) {
        // Initialize (and clear) the trainer registry for the server.
        var trainerRegistry = RCT.getTrainerRegistry();
        trainerRegistry.init(server); // this is required

        // We look for trainer json files in 'minecraft/trainers'.
        var trainerDir = Path.of(server.getWorldPath(LevelResource.ROOT).toString(), "..", "..", "trainers").toFile();
        var files = trainerDir.listFiles(f -> f.getName().toLowerCase().endsWith(".json"));

        if(files != null) {
            for(var trainerFile : files) {
                try(var rd = new BufferedReader(new FileReader(trainerFile))) {
                    // We use the file name as trainer id and parse the content into a TrainerModel
                    // instance, which is then provided to the TrainerRegistry to register a new
                    // TrainerNPC.
                    var trainerId = fileToId(trainerFile);
                    trainerRegistry.registerNPC(trainerId, GSON.fromJson(rd, TrainerModel.class));
                } catch(RCTException errors) {
                    // This will log all issues that the model may has (the trainer was registered regardless).
                    ModCommon.LOG.error("Model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> ModCommon.LOG.error(error.message));
                } catch(IOException e) {
                    // The trainer was not registered.
                    ModCommon.LOG.error("Failed to parse trainer", e);
                }
            }
        }
    }

    // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
    // trainer id. Using a players name should be sufficient in most scenarios (an
    // alternative could be a players uuid).
    static void onPlayerJoin(ServerPlayer player) {
        RCT.getTrainerRegistry().registerPlayer(player.getName().getString(), player);
    }

    static void onPlayerQuit(Player player) {
        RCT.getTrainerRegistry().unregisterById(player.getName().getString());
    }
}
```

---

Starting a battle is now simply a matter of invoking `BattleManager#startBattle` and providing `Trainer` instances for both sides along a `BattleFormat` and some `BattleRules`. One may study the implementation of the `battle` command in [*`TBCS`*](https://gitlab.com/srcmc/tbcs/-/blob/master/common/src/main/java/com/gitlab/srcmc/tbcs/commands/CommandsContext.java?ref_type=heads) for an example of how this can be achieved (the `attach` command may also serve as an example of how to associate trainers with entities) but to give a brief overview:

```java
RCTApi.getInstance("example_mod").getTrainerRegistry().getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
```

Attaches the trainer with `trainerId` to the `trainerEntity` (can be any `LivingEntity`).

---

```java
RCTApi.getInstance("example_mod").getBattleManager().startBattle(trainerPlayer, trainerNPC, new BattleRules());
```

Starts a battle between the `trainerPlayer` and `trainerNPC` in the `GEN_9_SINGLES` battle format and with default `BattleRules`.

> **Tip**: It it usually a good idea to always (re)attach a trainer to a known entity immediately before a battle is started. If the entity happens to be in an unloaded chunk a battle may softlock!

## Gradle dependency

Available on [cursemaven](https://www.curseforge.com/minecraft/mc-mods/radical-cobblemon-trainers-api/) (curseforge).

**Common:**

```gradle
dependencies {
    modImplementation "curse.maven:radical-cobblemon-trainers-api-1152792:<fileId>"
}
```

**Fabric:**

```gradle
dependencies {
    modImplementation "curse.maven:radical-cobblemon-trainers-api-1152792:<fileId>"
}
```

**Neoforge:**

```gradle
dependencies {
    modImplementation "curse.maven:radical-cobblemon-trainers-api-1152792:<fileId>"
}
```

> You can find the `fileId` in the URL of the curseforge download page for a specific version.
