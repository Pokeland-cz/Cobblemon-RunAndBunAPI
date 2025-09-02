# Changelog

## [0.13.8-beta] - 2025-09-02

**Changed:**

- *#84* Improved AI awareness of battle effects (like "Wish" being passed to a switched pokemon)

**Fixed:**

- *#83* Battle AI not taking all changes from transformed pokemon (ditto) into account, in some cases causing issues
- *#82* Fixed dyna- and gmax failing for some status moves
- *#81* Introduced *fixes* (mostly workarounds) to deal with various somewhat uncommon issues in battles (often resulting in softlocks or battles ending abruptly). For example: Pokemon fainting at the end of a turn on both sides (e.g. by moves like "Perish Song") and the player chosing a pokemon to switch very quickly. Note that these changes are mostly limited to battles started by this api for now (it is currently unclear if these kind of issues also occur in other types of battles like PvP).

## [0.13.7-beta] - 2025-07-05

**Fixed:**

- *#80* Issues with multiturn moves

## [0.13.6-beta] - 2025-06-17

**Changed:**

- *#79* Errors for invalid held items are now only logged for the last checked held item of a pokemon (in case multiple options have been configured)

## [0.13.5-beta] - 2025-06-14

**Changed:**

- *#78* Battle AI is now aware of hiddenpower types from pokemon
- *#77* Battle AI is now aware of tera types from terastallized pokemon

**Fixed:**

- *#76* Some battle issues related to moves that force opponent pokemon to switch

## [0.13.4-beta] - 2025-06-10

**Changed:**

- *#75* Lowered base chance for trainers to switch out a dynamaxed pokemon
- *#74* Minor adjustments to boost move evaluation (less likely for users with lower health)

**Fixed:**

- *#73* Usage of moves unknown to cobblemon causing battles to softlock

## [0.13.3-beta] - 2025-06-03

**Fixed:**

- *#72* Trainers attempting to activate ZPowers with wrong moves

## [0.13.2-beta] - 2025-06-02

**Fixed:**

- *#71* Hotfix for potential `UnsupportedOperationException` when attempting to start a battle

## [0.13.1-beta] - 2025-06-02

**Fixed:**

- *#70* Oversight causing battles to randomly crash (`NullPointerException` because "moveset" is null)

## [0.13.0-beta] - 2025-06-02

**Changed:**

- *#69* Allow cobblemons `BATTLE_STARTED_PRE` event to intercept battles started by this api
- *#68* Pokemon models may now alternatively accept a list of held items (if one item was not found the next item is checked)
- *#67* Trainer pokemon now support usage of gimmicks (zmoves, mega evolution, dynamax, etc.)
  - Added `gimmicks` property to `PokemonModel`: Allows to enable and define a terastalization type (`tera`), as well as to activate dyna- and gigantamax (`dynamax` and `gmax`), **latter two require the mega showdown mod to work**
  - Most gimmicks require specific items to be activated, which are not provided by this mod (you can use other mods like mega showdown for that)

## [0.12.1-beta] - 2025-05-24

**Changed:**

- *#66* General improvements for `RCTBattleAI`
  - Custom evaluations for hazard and field manipulating moves (tailwind, gravity, trickroom, spikes, stealthrock, toxicspikes and stickyweb)
  - Custom evaluations for moves that inflict major status conditions (like spore or glare)
  - Custom evaluations for sacrificial moves (like explosion or memento)
  - Custom evaluations for some common moves with very specific effects (like taunt or protect)
  - Custom evaluations for weather and terrain moves (like raindance or mistyterrain)
  - Some minor fixes and adjustments here and there (e.g. sleeptalk checking the opponents sleep status instead of the users)
- *#65* Refactored `BattleEffects` (former `PokeContext`) utility class for the battle ai

**Fixed:**

- *#64* Various potential battle errors with `RCTBattleAI` (e.g. battle error if trainer pokemon dies of entry hazards)
- *#63* `Text#getComponent()` now returns a component with an empty string (instead of the language key itself), this should fix issues with fallback mechanics if certain texts do not have a translation defined (e.g. trainer names)

## [0.12.0-beta] - 2025-05-13

**Added:**

- *#62* `RCTApi#configureGsonBuilder(GsonBuilder)`
- *#61* `RCTApi#gsonBuilder()`
- *#60* `Text` utility type for translatable text, which can be parsed from a JSON object with `literal` and/or `translatable` fields, or directly from a string, which serves as the `literal` value
  - Translation support for `PokemonModel#nickname` property (**will be ignored if `literal` is not set**)
  - Translation support for `TrainerModel#name` property

**Changed:**

- *#59* Return type of `PokemonModel#getName()` from `String` to `Text` (**potential breaking change**)
- *#58* Return type of `Trainer#getName()` from `String` to `Text` (**potential breaking change**)
- *#57* Return type of `TrainerModel#getName()` from `String` to `Text` (**potential breaking change**)
- *#56* Return type of `TrainerNPC#getName()` from `String` to `Text` (**potential breaking change**)
- *#55* Return type of `TrainerPlayer#getName()` from `String` to `Text` (**potential breaking change**)

## [0.11.1-beta] - 2025-05-05

**Changed:**

- *#54* Adjustments and fixes to `RCTBattleAI`
  - All moves have been further categorized (e.g. *HEAL* or *BUFF*) for a better generic evaluation and to circumvent issues with trainers targeting opponents with moves that have positive effects
  - Improved awareness of *screens*, *weather effects* and *terrains*
  - Improved awareness of certain moves with specific conditions (like *fake out* or *wish*)
  - Improved awareness of status conditions and other effects (like *levitate*, *seeded* or *drowsy*)
- *#53* Builder pattern for `RCTBattleAIConfig` and `BattleRules` (thanks Gitoido)

**Deprecated:**

- *#52* `TypeChart#getEffectiveness(ElementalType, ElementalType, ElementalType, Ability)`

## [0.11.0-beta] - 2025-04-12

**Added:**

- *#51* `BattleManager#startBattle()` with `UUID` return type as replacement for `BattleManager#start()`
- *#50* `nickname` property for `PokemonModel`s

**Changed:**

- *#49* Adjustments to `RCTBattleAI` *move* and *switch* evaluations (e.g. better awareness of shedinjas wonderguard) and other tweaks
- *#48* If the `name` of a `TrainerNPC` is unset or empty it will now fall back to the display name of the entity the trainer is attached to

**Fixed:**

- *#47* `RCTBattleAI` attempting to choose various illegal actions, causing battles to softlock in certain situations and other issues (e.g. switching out against pokemon with "arena trap" or while "mean look" is active)

**Deprecated:**

- *#46* `BattleManager#start()` and helper methods

**Removed:**

- *#45* Demo commands (now implemented in [tbcs](https://gitlab.com/srcmc/tbcs))
  - `BattleEndCommand`
  - `CommandsContext`
  - `RCTApiCommands`
  - other related classes
- *#44* `BattleState#BattleState(PokemonBattle, BattleRules)`
- *#43* `RCTApi#getInstance()`
- *#42* `RCTApi#init(TrainerRegistry, BattleManager)`
- *#41* `RCTApi#initInstance(String, TrainerRegistry)`
- *#40* `RCTApi#initInstance(String, TrainerRegistry, BattleManager)`

## [0.10.15-beta] - 2025-03-29

**Changed:**

- *#39* Serializable model and `JTO` classes

## [0.10.14-beta] - 2025-03-20

**Fixed:**

- *#38* Potential mod initialization issues when multiple mods register to this api (hopefully)

## [0.10.13-beta] - 2025-03-15

**Fixed:**

- *#37* Fixed potential issues with randomized pokemon stats when there are multiple mods that depend on this api

## [0.10.12-beta] - 2025-02-05

**Fixed:**

- *#36* Fix non-initialized IV and EV props in `PokemonModel` when using Cobblemon Pokemon class constructor (thanks Gitoido)

## [0.10.11-beta] - 2025-01-28

**Fixed:**

- *#35* EVs/IVs of trainer pokemon being reset/randomized at start of battles

## [0.10.10-beta] - 2025-01-26

**Added:**

- *#34* `BattleState#isEndForced()`: checks if battle was forcefully ended (i.e. draw)

**Changed:**

- *#33* `Events#BATTLE_ENDED` now fired after a battle was unregistered from the `BattleManager` and also if a battle was forcfully ended

**Fixed:**

- *#32* Incompatibility with Cobblemon 1.6.1 (min required version)

## [0.10.9-beta] - 2025-01-24

**Fixed:**

- *#31* Clients crashing when logging out from a server on fabric

## [0.10.8-beta] - 2025-01-22

**Added:**

- *#30* `BattleManager#of(PokemonBattle)`, `BattleManager#queryToEnd(PokemonBattle)` and `BattleManager#tick()`

**Fixed:**

- *#29* Command api mixing up winners and losers

## [0.10.7-beta] - 2025-01-21

**Added:**

- *#28* Simple Event API
  - Event `BATTLE_ENDED`
  - Event `BATTLE_STARTED`
  - Event `TRAINER_REGISTRED`
  - Event `TRAINER_UNREGISTRED`
  - `EventContext` shared between the `TrainerRegistry` and `BattleManager` of an `RCTApi` service (but distinct of those from other services)
- *#27* `BattleManager#getStates()` to retrieve all active battle states
- *#26* `CommandsContext` to allow registration of commands for different contexts without interfering with each other

**Changed:**

- *#25* Improved commands (api)
  - Better suggestions
  - Win commands support
- *#24* Some design adjustments to how `BattleManager` instances are handled (now distinct for each registered `RCTApi` service)
- *#23* The mod is now also a requirement for clients (for now)
- *#22* `BattleManager#end(UUID, boolean)` overload to forcefully end battles

**Fixed:**

- *#21* Possibility of starting battles against trainers not attached to an entity (potential softlock)
- *#20* Slight adjustments to mod initialization (hopefully fixes some *random* startup crashes on neoforge)
- *#19* Trainer pokemon being catchable and/or not recalled in some scenarios

**Removed:**

- *#18* `GEN_9_ROYAL` battle format for now (as it appears to not be implemented yet in Cobblemon)

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
