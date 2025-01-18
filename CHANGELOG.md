# Changelog

## [0.10.7-beta] - 2025-01-17

**Added:**

- *#20* Extended `BattleManager#start` by a parameter for a callback consumer that receives the `BattleState` at the end of a battle
- *#19* `CommandsContext` to allow registration of commands for different contexts without interfering with each other

**Changed:**

- *#18* Improved commands (api)
  - Better suggestions
  - Win commands support

## [0.10.6-beta] - 2025-01-07

**Fixed:**

- *#17* Trainer pokemon being saved to the world

## [0.10.5-beta] - 2025-01-07

**Added:**

- *#16* Methods for initialization and retrieval of different `RCTApi` instances by id or in bulk (fallback to `RCTApi#DEFAULT_INSTANCE`)
  - `RCTApi#getInstance(String)`
  - `RCTApi#getInstances()`
  - `RCTApi#initInstance(String)`
  - `RCTApi#initInstance(String, TrainerRegistry)`
  - `RCTApi#initInstance(String, TrainerRegistry, BattleManager)`
- *#15* Methods to retrieve `Trainer`s from the `TrainerRegistry` by the original trainer (*OT*) of a given `Pokemon`
  - `TrainerRegistry#getByOT(Pokemon)`
  - `TrainerRegistry#getByOT(Pokemon, Class<T>)`

**Deprecated:**

- *#14* `RCTApi#init()` use new methods instead (see *#16*)

## [0.10.4-beta] - 2024-12-30

**Fixed:**

- *#13* Issues with entity selector in battle command for formats with multiple participants

## [0.10.3-beta] - 2024-12-29

**Added:**

- *#12* `RCTApiCommands.register(String)` allows to register `RCTApi` commands with a different prefix
- *#11* `TrainerRegistry#getId(LivingEntity)` retrieves the trainer id for any given `LivingEntity`

**Changed:**

- *#10* `battle` command now supports entity selectors

## [0.10.2-beta] - 2024-12-23

**Fixed:**

- *#9* Battle music themes not playing in trainer battles (`battle.pvn`)

## [0.10.1-beta] - 2024-12-09

**Fixed:**

- *#8* Missing Cobblemon (version) dependency check
- *#7* Replaced redundant `PokemonEntityMixin` with event handler (which also fixed a crash caused by that mixin on startup)

## [0.10.0-beta] - 2024-12-09

**Added:**

- *#6* Added ai configs `RCTBattleAIConfig`, `SelfdotGen5AIConfig`, `StrongBattleAIConfig` and registered corresponding `JTO` parsers (`rct`, `sd5` and `cbl`)
- *#5* Generic `JTO` (json to object) flexible parser system

**Removed:**

- *#4* `AIType` enum (`TrainerModel.ai` is now defined as `JTO<BattleAI>`)

## [0.9.1-beta] - 2024-12-09

**Changed:**

- *#3* `RCTBattleAI` adjustments/fixes: improved switch evaluation + trainers now actually consider the use of (mostly healing) items

**Fixed:**

- *#2* Issues with `RCTBattleAI` and switch instructions potentially causing softlocks as well as some other corner cases that could cause a battle failure (this might not fixed all issues but it appeared rather stable now during my testings)

## [0.9.0-beta] - 2024-12-03

**Added:**

- *#1* Initial release (trainer management, battle formats, battle ai, trainer models)
