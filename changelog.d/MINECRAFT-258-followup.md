bump: patch

### Fixed
- The 180-degree arc could silently fail to hit a second target in
  single-player: `AttackEntityEvent` fires on both logical sides, and the
  arc sweep had no client-side guard and used a shared (not thread-confined)
  re-entrancy guard, letting the client and integrated-server threads race
  for the same swing.

### Changed
- Documented, rather than silently left unhandled, that a swing aimed at a
  block does not trigger the arc in this version.
