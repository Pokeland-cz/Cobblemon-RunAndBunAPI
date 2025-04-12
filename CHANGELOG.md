# Changelog

## [0.11.0-beta] - 2025-04-12

**Added:**

- `BattleManager#startBattle()` with `UUID` return type as replacement for `BattleManager#start()`
- `nickname` property for `PokemonModel`s

**Changed:**

- Adjustments to `RCTBattleAI` *move* and *switch* evaluations (e.g. better awareness of shedinjas wonderguard) and other tweaks
- If the `name` of a `TrainerNPC` is unset or empty it will now fall back to the display name of the entity the trainer is attached to

**Fixed:**

- `RCTBattleAI` attempting to choose various illegal actions, causing battles to softlock in certain situations and other issues (e.g. switching out against pokemon with "arena trap" or while "mean look" is active)

**Deprecated:**

- `BattleManager#start()` and helper methods

**Removed:**

- Demo commands (now implemented in [tbcs](https://gitlab.com/srcmc/tbcs))
  - `BattleEndCommand`
  - `CommandsContext`
  - `RCTApiCommands`
  - other related classes
- `BattleState#BattleState(PokemonBattle, BattleRules)`
- `RCTApi#getInstance()`
- `RCTApi#init(TrainerRegistry, BattleManager)`
- `RCTApi#initInstance(String, TrainerRegistry)`
- `RCTApi#initInstance(String, TrainerRegistry, BattleManager)`

## [0.10.15-beta] - 2025-03-29

**Changed:**

- Serializable model and `JTO` classes

## [0.10.14-beta] - 2025-03-20

**Fixed:**

- Potential mod initialization issues when multiple mods register to this api (hopefully)

## [0.10.13-beta] - 2025-03-15

**Fixed:**

- Fixed potential issues with randomized pokemon stats when there are multiple mods that depend on this api

## [0.10.12-beta] - 2025-02-05

**Fixed:**

- Fix non-initialized IV and EV props in `PokemonModel` when using Cobblemon Pokemon class constructor (thanks Gitoido)

## [0.10.11-beta] - 2025-01-28

**Fixed:**

- EVs/IVs of trainer pokemon being reset/randomized at start of battles

## [0.10.10-beta] - 2025-01-26

**Added:**

- `BattleState#isEndForced()`: checks if battle was forcefully ended (i.e. draw)

**Changed:**

- `Events#BATTLE_ENDED` now fired after a battle was unregistered from the `BattleManager` and also if a battle was forcfully ended

**Fixed:**

- Incompatibility with Cobblemon 1.6.1 (min required version)

## [0.10.9-beta] - 2025-01-24

**Fixed:**

- Clients crashing when logging out from a server on fabric

## [0.10.8-beta] - 2025-01-22

**Added:**

- `BattleManager#of(PokemonBattle)`, `BattleManager#queryToEnd(PokemonBattle)` and `BattleManager#tick()`

**Fixed:**

- Command api mixing up winners and losers

## [0.10.7-beta] - 2025-01-21

**Added:**

- Simple Event API
  - Event `BATTLE_ENDED`
  - Event `BATTLE_STARTED`
  - Event `TRAINER_REGISTRED`
  - Event `TRAINER_UNREGISTRED`
  - `EventContext` shared between the `TrainerRegistry` and `BattleManager` of an `RCTApi` service (but distinct of those from other services)
- `BattleManager#getStates()` to retrieve all active battle states
- `CommandsContext` to allow registration of commands for different contexts without interfering with each other

**Changed:**

- Improved commands (api)
  - Better suggestions
  - Win commands support
- Some design adjustments to how `BattleManager` instances are handled (now distinct for each registered `RCTApi` service)
- The mod is now also a requirement for clients (for now)
- `BattleManager#end(UUID, boolean)` overload to forcefully end battles

**Fixed:**

- Possibility of starting battles against trainers not attached to an entity (potential softlock)
- Slight adjustments to mod initialization (hopefully fixes some *random* startup crashes on neoforge)
- Trainer pokemon being catchable and/or not recalled in some scenarios

**Removed:**

- `GEN_9_ROYAL` battle format for now (as it appears to not be implemented yet in Cobblemon)

## [0.10.6-beta] - 2025-01-07

**Fixed:**

- Trainer pokemon being saved to the world

## [0.10.5-beta] - 2025-01-07

**Added:**

- Methods for initialization and retrieval of different `RCTApi` instances by id or in bulk (fallback to `RCTApi#DEFAULT_INSTANCE`)
  - `RCTApi#getInstance(String)`
  - `RCTApi#getInstances()`
  - `RCTApi#initInstance(String)`
  - `RCTApi#initInstance(String, TrainerRegistry)`
  - `RCTApi#initInstance(String, TrainerRegistry, BattleManager)`
- Methods to retrieve `Trainer`s from the `TrainerRegistry` by the original trainer (*OT*) of a given `Pokemon`
  - `TrainerRegistry#getByOT(Pokemon)`
  - `TrainerRegistry#getByOT(Pokemon, Class<T>)`

**Deprecated:**

- `RCTApi#init()` use new methods instead (see *#16*)

## [0.10.4-beta] - 2024-12-30

**Fixed:**

- Issues with entity selector in battle command for formats with multiple participants

## [0.10.3-beta] - 2024-12-29

**Added:**

- `RCTApiCommands.register(String)` allows to register `RCTApi` commands with a different prefix
- `TrainerRegistry#getId(LivingEntity)` retrieves the trainer id for any given `LivingEntity`

**Changed:**

- `battle` command now supports entity selectors

## [0.10.2-beta] - 2024-12-23

**Fixed:**

- Battle music themes not playing in trainer battles (`battle.pvn`)

## [0.10.1-beta] - 2024-12-09

**Fixed:**

- Missing Cobblemon (version) dependency check
- Replaced redundant `PokemonEntityMixin` with event handler (which also fixed a crash caused by that mixin on startup)

## [0.10.0-beta] - 2024-12-09

**Added:**

- Added ai configs `RCTBattleAIConfig`, `SelfdotGen5AIConfig`, `StrongBattleAIConfig` and registered corresponding `JTO` parsers (`rct`, `sd5` and `cbl`)
- Generic `JTO` (json to object) flexible parser system

**Removed:**

- `AIType` enum (`TrainerModel.ai` is now defined as `JTO<BattleAI>`)

## [0.9.1-beta] - 2024-12-09

**Changed:**

- `RCTBattleAI` adjustments/fixes: improved switch evaluation + trainers now actually consider the use of (mostly healing) items

**Fixed:**

- Issues with `RCTBattleAI` and switch instructions potentially causing softlocks as well as some other corner cases that could cause a battle failure (this might not fixed all issues but it appeared rather stable now during my testings)

## [0.9.0-beta] - 2024-12-03

**Added:**

- Initial release (trainer management, battle formats, battle ai, trainer models)
