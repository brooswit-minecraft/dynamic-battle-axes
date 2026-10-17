# Vendored shared loader

`magicavoxel-loader-0.1.3.jar` is the official
[MagicVoxel Loader v0.1.3 release asset](https://github.com/brooswit-minecraft/magicavoxel-loader/releases/tag/v0.1.3),
from the authorized release. Its SHA256 is
`65899c6c9b30909291d5d76eee8cafab6f29cc638c4941a53bb6f45d33cef403`.
Gradle verifies this digest before compiling or packaging the consumer. v0.1.3
is used (not v0.1.2) because it carries the corrected NeoForge release
descriptor; the loader's own Java API is unchanged between the two.

Vendored (as dynamic-whips/MINECRAFT-307 already does for this same library)
rather than resolved from GitHub Packages, because this repository's push
credential lacks the `read:packages` scope GitHub Packages requires.
