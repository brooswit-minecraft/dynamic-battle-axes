# Battle-axe voxel models

Each material's editable source is
`src/main/resources/assets/dynamicbattleaxes/vox/<material>_battle_axe.vox.hex`.
Each is a reviewable hexadecimal representation of its own real MagicaVoxel
150 `.vox` file; the Gradle `materializeBattleAxeVox` task materializes the
unchanged bytes at `assets/dynamicbattleaxes/vox/<material>_battle_axe.vox`
during `processResources` and `jar`. This keeps the binary source editable in
ordinary text reviews while preserving the exact `.vox` payload each item
consumes at runtime -- the same convention `dynamic-whips` uses for
`kickball.vox.hex` (MINECRAFT-307).

Every model is an 8x8x2, 56-voxel single-headed axe silhouette (a handle,
palette index 1, topped by a wide head, palette index 2); all seven
materials currently share this geometry. Replacement assets must be
MagicaVoxel 150 files with the first `SIZE`/`XYZI` model using dimensions
1-16 on every axis, at most 1,024 voxels, and palette indices 1-255 (index 0
is invalid) -- the shared loader's own documented asset standard. Edit or
replace the hex payload at the same path and let CI package it; no manual
Minecraft model export is needed.

During common setup, Dynamic Battle Axes calls the shared
`io.github.brooswitminecraft:magicavoxel-loader:0.1.3` APIs
`VoxAssetLoader.load(InputStream)` and `MinecraftItemModelConverter.convert`
once per material, independently -- see
`DynamicBattleAxesMod.loadBattleAxeVoxelModels`. The client renderer
(`BattleAxeItemRenderer`) draws the resulting native 0-16 element bounds
through Minecraft's block-model renderer, mapping palette index 1 to a
wooden handle block and palette index 2 to a block representative of that
axe's own material (iron/gold/diamond/netherite/copper block, cobblestone
for stone, stripped oak wood for wood) -- the fitting, material-at-a-glance
artwork MINECRAFT-258 decided on, deliberately simple ("rough art is not a
blocker"). Each item presents as a normal Minecraft item (inventory icon,
stacking, tooltip); only its in-hand/in-inventory render goes through the
custom voxel renderer, same as the shared loader's own demo item.

The consuming mod depends on the hash-pinned release JAR vendored at
`libs/magicavoxel-loader-0.1.3.jar` (see `libs/README.md`); CI is the
authoritative consumer-build check (`./gradlew build`, which runs the real
asset-loading/conversion test in `BattleAxeVoxAssetTest`).
