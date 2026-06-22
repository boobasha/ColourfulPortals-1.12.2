# Colourful Portals — 1.12.2

[![Language](https://img.shields.io/badge/语言-中文-blue?style=for-the-badge)](README.md)
[![Language](https://img.shields.io/badge/Language-English-brightgreen?style=for-the-badge)](README_EN.md)

> Current: **English** ｜ Click the blue button to switch to [中文](README.md)

A port of Tmtravlr's **Colourful Portals** (v1.4.3) from Minecraft **1.7.10** to **1.12.2 (Forge)**. All original textures, sounds and language files are reused (only a few texture filenames were lower-cased as 1.12.2 requires).

## About

Build a frame out of wool / stained clay / stained glass and fill it with **Colourful Water** to form a portal. Portals of the **same colour** link to each other and can teleport across dimensions. A single-block **standalone portal** can also be crafted.

## Features

- **Colourful Water** fluid + a full bucket progression (dyes → mixed → enchanted stain-proof bucket).
- **Framed portals**: wool / stained clay / stained glass, 16 colours, matching colours link (cross-dimension too).
- **Standalone portals**: a craftable single-block portal (16 colours, per frame material).
- **Coloured ender pearls** (normal / reflective): right-click or drop into a portal to create a random destination.
- Particles, teleport sound, config file, recipes, dungeon-chest loot.
- Ships with both English and Simplified Chinese language files.

## Install

Drop `ColourfulPortals-1.12.2-1.4.3.jar` into a 1.12.2 Forge `mods` folder.

## Building

Requires **JDK 8**. This repo's `gradle.properties` points `org.gradle.java.home` at `C:/Program Files/Zulu/zulu-8` — change it to your JDK 8 path (or delete that line and set `JAVA_HOME` yourself).

```bash
# Windows cmd / PowerShell (the launcher needs JAVA_HOME pointing at JDK 8):
set JAVA_HOME=C:\Program Files\Zulu\zulu-8
gradlew.bat build

# or Git Bash:
JAVA_HOME="/c/Program Files/Zulu/zulu-8" ./gradlew build
```

Output: `build/libs/ColourfulPortals-1.12.2-1.4.3.jar`

Other useful tasks:

```bash
gradlew build        # compile and package the jar
gradlew runClient    # launch a dev client
gradlew runServer    # launch a dev server
```

### Toolchain notes (why these exact versions)

- **ForgeGradle 2.3** (`2.3-20210802.170449-48`) on **Gradle 4.10.3**.
- **Forge `1.12.2-14.23.5.2847`** — deliberately *not* the final 2860. Forge 2854+ replaced the FG2-format `userdev` artifact on Maven with `userdev3` (the ForgeGradle 3 format), which ForgeGradle 2.3 cannot consume. 2847 is the last 1.12.2 build that still ships the FG2 `userdev`, so it resolves cleanly.
- China mirrors are configured (Tencent for the Gradle distribution, Aliyun Maven) for faster fetches.
- The first build downloads Forge and deobfuscates Minecraft, which takes a while and needs network access.

## Porting notes

The 1.7.10 → 1.12.2 jump rewrites the whole API surface: `cpw.mods.fml.*` → `net.minecraftforge.fml.*`; `int x,y,z` → `BlockPos`; metadata → `IBlockState` (a 0–15 `color` property); `IIcon` rendering → JSON models / blockstates; the fluid uses the `forge:fluid` model; dimension travel uses `transferPlayerToDimension` / `Entity.changeDimension` with a custom `ITeleporter`. The standalone portal's custom GL rendering (the 1.7.10 `ISimpleBlockRenderingHandler` no longer exists) was rewritten as a `TileEntitySpecialRenderer` plus a `TileEntityItemStackRenderer` for the item.

## License & credits

- Original mod by **Tmtravlr**.
- This repository is a community port and keeps the original mod's **GPL-3.0** license (see [LICENSE](LICENSE)).
