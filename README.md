# dynamic-battle-axes

Seven-material battle-axe weapons.

NeoForge 1.21.1, Java 21. Mod ID: `dynamicbattleaxes`. MIT licensed.

## Current state

Seven-material battle-axe weapon (wood, stone, copper, iron, gold, diamond,
netherite) — a frontline, crowd-clearing weapon. See
`BattleAxeMaterial`/`BattleAxeItem`/`BattleAxeTiers`/`BattleAxeCombat` under
`src/main/java/io/github/brooswitminecraft/dynamicbattleaxes/`.

- **180-degree arc**: every attack also hits every living entity within
  reach in front of the attacker (`BattleAxeCombat`), not just the single
  hit-scanned target, through the exact same vanilla attack path/protections
  (pvp rule, scoreboard friendly fire, invulnerability) a direct hit would
  use — no new bypass. See MINECRAFT-258's decision comment.
- **Balance**: each battle-axe does 1 more total damage than the
  same-material sword (copper interpolates stone/iron: 5.5), attacks 0.4
  slower, and reaches 0.5 further — via the item's real
  `ItemAttributeModifiers` (`BattleAxeItem.createAttributes`).
- **Models**: each material has its own real MagicaVoxel `.vox` model
  (`src/main/resources/assets/dynamicbattleaxes/vox/`, see
  `docs/battle-axe-model.md`), loaded individually through the shared
  `magicavoxel-loader` v0.1.3 library at common setup and rendered
  in-hand/in-inventory by a custom item renderer
  (`client/BattleAxeItemRenderer`).

Six materials craft with a shaped recipe mirroring vanilla's own axe shape
(`XX`/`X#`/` #`); netherite upgrades from the diamond battle axe via a
smithing-table transform, mirroring vanilla's own netherite tool upgrades.

## Building

Requires a JDK 21 with `javac` on `JAVA_HOME`.

```sh
./gradlew build
```

## Releasing

Every PR that touches `src/` must add exactly one fragment under
`changelog.d/` (see `changelog.d/README.md`). Pushing to `main` with new
fragments tags a GitHub release automatically; nothing is committed back by
hand.
