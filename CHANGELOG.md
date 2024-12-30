# Changelog

## [0.10.3-beta] - 2024-12-29

***Added***

- `RCTApiCommands.register(String)` allows to register `RCTApi` commands with a different prefix
- `TrainerRegistry#getId(LivingEntity)` retrieves the trainer id for any given `LivingEntity`

***Changed***

- `battle` command now supports entity selectors

## [0.10.2-beta] - 2024-12-23

***Fixed***

- Battle music themes not playing in trainer battles (`battle.pvn`)

## [0.10.1-beta] - 2024-12-09

***Fixed***

- Missing Cobblemon (version) dependency check
- Replaced redundant `PokemonEntityMixin` with event handler (which also fixed a crash caused by that mixin on startup)

## [0.10.0-beta] - 2024-12-09

***Added***

- Added ai configs `RCTBattleAIConfig`, `SelfdotGen5AIConfig`, `StrongBattleAIConfig` and registered corresponding `JTO` parsers (`rct`, `sd5` and `cbl`)
- Generic `JTO` (json to object) flexible parser system

***Removed***

- `AIType` enum (`TrainerModel.ai` is now defined as `JTO<BattleAI>`)

## [0.9.1-beta] - 2024-12-09

***Changed***

- `RCTBattleAI` adjustments/fixes: improved switch evaluation + trainers now actually consider the use of (mostly healing) items

***Fixed***

- Issues with `RCTBattleAI` and switch instructions potentially causing softlocks as well as some other corner cases that could cause a battle failure (this might not fixed all issues but it appeared rather stable now during my testings)

## [0.9.0-beta] - 2024-12-03

***Added***

- Initial release (trainer management, battle formats, battle ai, trainer models)
