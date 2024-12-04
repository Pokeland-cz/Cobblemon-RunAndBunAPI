# Radical Cobblemon Trainers - API

Trainer management and battle API for [Cobblemon](https://cobblemon.com/en).

This API was developed as foundation for the [Radical CobblemonTrainers](https://gitlab.com/srcmc/rct/mod) mod and as replacement for the discontinued dependency [CobblemonTrainers](https://github.com/davo899/CobblemonTrainers) for Minecraft >= `1.21` and Cobblemon >= `1.6`.

Yet it is designed as independent library to provide a subset of similar features for everyone to use.

## Key features

- Trainer management and entity association (trainer registry)
- Support for different battle formats (1v1 SINGLE, 1v1 DOUBLE, 2v2 MULTI, ...)
- Custom battle rules (e.g. max item usages per battle)
- Extended AI features (currently only supported by `RCTBattleAI`):
  - usual battle activities (move selection, switch, ...)
  - gimmicks: mega evolve, dynamax, z-moves, terastallize (TODO)
  - trainers can carry and use items
- Trainer and pokemon models (pojos) for easy parsing
  - Converters to Cobblemon types
  - Model validation (collects all errors before an exception is thrown)

## Example

Following [ExampleMod](common/src/main/java/com/gitlab/srcmc/rctapi/example/ExampleMod.java) provides a *common* implementation using Architectury:

```java
public class ExampleMod {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    private static String fileToId(File file) {
        var name = file.getName().toLowerCase().trim();
        var i = name.lastIndexOf('.');
        return i < 0 ? name : name.substring(0, i);
    }

    // Call this in the common setup phase of the mod. E.g. in onInitialize() of your
    // ModInitializer on Fabric or in the constructor of your @Mod annotated class on
    // Neoforge.
    public static void init() {
        // We may initialize the RCTApi singleton with custom implementations of
        // TrainerRegistry and BattleManager. If not explicitly initialized (i.e. with
        // RCTApi#init(TrainerRegistry, BattleManager)) a RCTApi instance will be lazily
        // instantiated on first retrieval with RCTApi#getInstance() using a default
        // constructed TrainerRegistry and BattleManager.

        RCTApiCommands.register(); // commands are not registered unless explicitly doing so.
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
        // Initialize (and clear) the trainer registry for the server
        var trainerRegistry = RCTApi.getInstance().getTrainerRegistry();
        trainerRegistry.init(server); // this is required

        // We look for trainer json files in 'minecraft/trainers'
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
                    // This will log all issues that the model may has (the trainer was registered regardless)
                    ModCommon.LOG.error("Model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> ModCommon.LOG.error(error.message));
                } catch(IOException e) {
                    // The trainer was not registered
                    ModCommon.LOG.error("Failed to parse trainer", e);
                }
            }
        }
    }

    // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
    // trainer id. Using a players name may be sufficient for this example but in real
    // scenarios a custom resolution of duplicate ids would be necessary. It is of
    // course possible to use any other string as id to circumvent this issue (e.g. a
    // players uuid).
    static void onPlayerJoin(ServerPlayer player) {
        RCTApi.getInstance().getTrainerRegistry().registerPlayer(player.getName().getString(), player);
    }

    static void onPlayerQuit(Player player) {
        RCTApi.getInstance().getTrainerRegistry().unregisterById(player.getName().getString());
    }
}
```

---

Starting a battle is now simply a matter of invoking `BattleManager#start` and providing `Trainer` instances for both sides along a `BattleFormat` and some `BattleRules`. One may study the implementation of the `battle` command in [`RCTApiCommands`](common/src/main/java/com/gitlab/srcmc/rctapi/commands/RCTApiCommands.java) for an example of how this can be achieved (the `attach` command may also serve as an example of how to associate trainers with entities) but to give a brief overview:

```java
RCTApi.getInstance().getTrainerRegistry().getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
```

Attaches the trainer with `trainerId` to the `trainerEntity` (can be any `LivingEntity`).

---

```java
RCTApi.getInstance().getBattleManager().start(trainerPlayer, trainerNPC, new BattleRules());
```

Starts a battle between the `trainerPlayer` and `trainerNPC` in the `GEN_9_SINGLES` battle format and with default `BattleRules`.

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

> You can find the `fileId` in the URL of the curseforge downlaod page for a specific version.
