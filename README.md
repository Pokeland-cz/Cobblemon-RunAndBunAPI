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

    // safety measure
    private static boolean eventsRegistered;

    public static void init(MinecraftServer server) {
        // Initialize (and clear) trainer registry for the server
        Trainers.init(server);

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
                    Trainers.registerNPC(trainerId, GSON.fromJson(rd, TrainerModel.class));
                } catch(CTException errors) {
                    // this will log all issues that the model may has (the trainer will still be registered)
                    CTEngineMod.LOG.error("model validation failure in: " + trainerFile.getPath());
                    errors.getErrors().forEach(error -> CTEngineMod.LOG.error(error.message));
                } catch(IOException e) {
                    CTEngineMod.LOG.error("failed to parse trainer", e);
                }
            }
        }

        // We can easily (un)register players as trainers whenever they log in or out.
        // Note: The TrainerRegistry does not allow to implicitly overwrite an existing
        // trainer id. Using a players display name may be sufficient for this example but
        // in real scenarios a custom resolution of duplicate names would be necessary. It
        // is of course possible to use any other string as id to circumvent this issue
        // (e.g. a players uuid).
        if(!ExampleMod.eventsRegistered) {
            PlayerEvent.PLAYER_JOIN.register(player -> Trainers.registerPlayer(player.getDisplayName().getString(), new TrainerPlayer(player)));
            PlayerEvent.PLAYER_QUIT.register(player -> Trainers.unregisterById(player.getDisplayName().getString()));
            ExampleMod.eventsRegistered = true;
        }
    }
}
```

Starting a battle is now simply a matter of invoking `BattleManager#start` and providing `Trainer` instances for both sides along a `BattleFormat` and some `BattleRules`. You may study the implementation of the `battle` command in [`CTEngineCommands`](common/src/main/java/net/ctengine/commands/CTEngineCommands.java) for an example of how this can be achieved (the `attach` command may also serve as an example of how to associate trainers with entities).

> A `BattleManager` instance can be retrieved from the `CTEngine` singleton yet the `Battles` utility class provides direct access to its interface and may be used for convenience.

## Gradle dependency

Available on [curse(forge)](todo.com) maven:

**Common/Fabric/Neoforge:**

```gradle
dependencies {
    modImplementation "net.ctengine:$ctengine_version"
}
```
