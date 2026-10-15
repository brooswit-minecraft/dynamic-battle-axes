# dynamic-battle-axes

Seven-material battle-axe weapons.

NeoForge 1.21.1, Java 21. Mod ID: `dynamicbattleaxes`. MIT licensed.

## Current state

Registration and obtainable recipes for seven battle-axe items (wood, stone,
copper, iron, gold, diamond, netherite) — see
`BattleAxeMaterial`/`BattleAxeItem`/`BattleAxeTiers` under
`src/main/java/io/github/brooswitminecraft/dynamicbattleaxes/`. Each is a
plain `AxeItem` with no combat-specific overrides: no sweep arc, no
PvP/friendly-fire/team/protection semantics, and no new combat-balance
decisions — the per-material attack damage/speed numbers reuse vanilla's own
existing per-tier axe values verbatim (copper, which vanilla has no axe or
sword for, uses a non-binding stone/iron interpolation). All of that is
explicitly out of scope here; see MINECRAFT-294/MINECRAFT-285.

Six materials craft with a shaped recipe mirroring vanilla's own axe shape
(`XX`/`X#`/` #`); netherite upgrades from the diamond battle axe via a
smithing-table transform, mirroring vanilla's own netherite tool upgrades.

Intentionally not included (hard exclusions for this ticket): item
models/textures, MagicaVoxel models or asset packs, and any combat
balance/behavior beyond what's needed for the item to exist and function as
a tool.

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
