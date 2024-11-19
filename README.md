# CTEngine

Trainer management and battle API for [Cobblemon](https://cobblemon.com/en).

## Key features

- Trainer management and entity association (trainer registry)
- Support for different battle formats (1v1 SINGLE, 1v1 DOUBLE, 2v2 MULTI, ...)
- Custom battle rules (e.g. max item usages per battle)
- Extended AI features:
  - usual battle activities (move selection, switch, ...)
  - gimmicks: mega evolve, dynamax, z-moves, terastallize (TODO)
  - carry and use items
- Trainer and pokemon models (pojos) for easy parsing
  - Converters to Cobblemon types
  - Model validation (collects all errors before an exception is thrown)

## API Documentaion

You can find the full api documentation [here](todo.com).

## Example

Following [ExampleMod](common/src/main/java/net/ctengine/example/ExampleMod.java) provides a *common* implementation using Architectury:

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

    // Call this in the common setup phase of the mod. E.g. in onInitialze() of your
    // ModInitializer on Fabric or in the constructor of your @Mod annotated class on
    // Neoforge.
    public static void init() {
        CTEngineCommands.register(); // commands from this mod are not registered unless explicitly doing so.
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
        // We may initialize the CTEngine singleton with custom implementations of
        // TrainerRegistry and BattleManager. If not explicitly initialized (i.e. with
        // CTEngine#init(TrainerRegistry, BattleManager)) a CTEngine instance will be
        // lazily instantiated on first retrieval with CTEnginge.getInstance() using
        // a default contstructed TrainerRegistry and BattleManager.

        // Initialize (and clear) the trainer registry for the server
        var trainerRegistry = CTEngine.getInstance().getTrainerRegistry();
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
                } catch(CTException errors) {
                    // This will log all issues that the model may has (the trainer was registered regardless)
                    CTEngineMod.LOG.error("Model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> CTEngineMod.LOG.error(error.message));
                } catch(IOException e) {
                    // The trainer was not registered
                    CTEngineMod.LOG.error("Failed to parse trainer", e);
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
        CTEngine.getInstance().getTrainerRegistry().registerPlayer(player.getName().getString(), player);
    }

    static void onPlayerQuit(Player player) {
        CTEngine.getInstance().getTrainerRegistry().unregisterById(player.getName().getString());
    }
}
```

---

Starting a battle is now simply a matter of invoking `BattleManager#start` and providing `Trainer` instances for both sides along a `BattleFormat` and some `BattleRules`. One may study the implementation of the `battle` command in [`CTEngineCommands`](common/src/main/java/net/ctengine/commands/CTEngineCommands.java) for an example of how this can be achieved (the `attach` command may also serve as an example of how to associate trainers with entities) but to give a brief overview:

```java
CTEngine.getInstance().getTrainerRegistry().getById(trainerId, TrainerNPC.class).setEntity(trainerEntity);
```

Attaches the trainer with `trainerId` to the `trainerEntity` (can be any `LivingEntity`).

---

```java
CTEngine.getInstance().getBattleManager().start(trainerPlayer, trainerNPC, new BattleRules());
```

Starts a battle between the `trainerPlayer` and `trainerNPC` in the `GEN_9_SINGLES` battle format and with default `BattleRules`.

## Gradle dependency

Available on [curse(forge)](todo.com) maven.

**Common:**

```gradle
dependencies {
    modImplementation "net.ctengine-common:1.0.0"
}
```

**Fabric:**

```gradle
dependencies {
    modImplementation "net.ctengine-fabrci:1.0.0"
}
```

**Neoforge:**

```gradle
dependencies {
    modImplementation "net.ctengine-neoforge:1.0.0"
}
```
