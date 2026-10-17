bump: minor

### Added
- 180-degree forward-arc melee: every battle-axe attack also hits every
  living entity within reach in front of the attacker, not just the single
  hit-scanned target, through the same vanilla attack path/protections as a
  direct hit (no PvP/friendly-fire override).
- Binding combat balance for all seven materials: attack damage one higher
  than the same-material sword (copper baseline interpolated between stone
  and iron), attack speed 0.4 slower, reach 0.5 longer.
- Seven real MagicaVoxel `.vox` battle-axe models (one per material),
  loaded through the shared `magicavoxel-loader` v0.1.3 library at common
  setup and rendered in-hand/in-inventory via a custom item renderer.
- The arc also triggers on a swing that hits no entity (not just a
  directly-targeted attack), via a small client-to-server notice.
- Every target in one swing (the directly-clicked one included) now
  computes damage from that swing's original attack strength, instead of
  later targets seeing a reset (weaker) value.
- Sweep reach now reads the attacker's live `ENTITY_INTERACTION_RANGE`
  attribute instead of a hardcoded constant, so it agrees with whatever
  reach vanilla used to find the primary target.

### Changed
- `BattleAxeMaterial`'s balance evidence (damage baseline) is now BINDING
  and feeds the item's real `ItemAttributeModifiers`, superseding
  MINECRAFT-294's non-binding-only scope hold.
