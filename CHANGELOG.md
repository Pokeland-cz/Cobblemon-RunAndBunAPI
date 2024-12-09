# Changelog

## [0.10.x-beta] - 2024-12-09

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
